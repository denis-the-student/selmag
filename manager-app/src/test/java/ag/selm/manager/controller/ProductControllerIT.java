package ag.selm.manager.controller;

import ag.selm.manager.controller.payload.UpdateProductPayload;
import ag.selm.manager.entity.Product;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Locale;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
@DisplayName("Интеграционные тесты ProductController")
public class ProductControllerIT {

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                        "id": 1,
                        "title": "Product 1",
                        "details": "Details 1"
                        }
                        """)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void loadProduct_ReturnsProduct() throws Exception {
        // given
        var loadedProduct = new Product(1, "Product 1", "Details 1");

        // when
        this.mockMvc.perform(get("/catalogue/products/1"))
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/product"),
                        model().attribute("product", loadedProduct)
                );
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void loadProduct_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));

        // when
        this.mockMvc.perform(get("/catalogue/products/1").locale(Locale.ENGLISH))
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Product not found")
                );
    }

    @Test
    @WithMockUser
    void loadProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given

        // when
        this.mockMvc.perform(get("/catalogue/products/1"))
                // then
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getProductEditPage_ReturnsEditPage() throws Exception {
        // given
        // when
        this.mockMvc.perform(get("/catalogue/products/1/edit"))
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/edit")
                );
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getProductEditPage_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));

        // when
        this.mockMvc.perform(get("/catalogue/products/1/edit").locale(Locale.ENGLISH))
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Product not found")
                );
    }

    @Test
    @WithMockUser
    void getProductEditPage_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        // when
        this.mockMvc.perform(get("/catalogue/products/1/edit"))
                // then
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateProduct_RedirectsToProductPage() throws Exception {
        // given
        WireMock.stubFor(WireMock.patch("/catalogue-api/products/1")
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "Updated product",
                            "details": "Updated details"
                        }"""))
                .willReturn(WireMock.noContent()));

        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "Updated product")
                .param("details", "Updated details")
                .with(csrf());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/catalogue/products/1")
                );

        WireMock.verify(WireMock.patchRequestedFor(
                        WireMock.urlPathMatching("/catalogue-api/products/1"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "Updated product",
                            "details": "Updated details"
                        }
                        """)));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateProduct_RequestIsInvalid_ReturnsProductEditPageWithErrors() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "  ")
                .with(csrf());

        WireMock.stubFor(WireMock.patch("/catalogue-api/products/1")
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "  ",
                            "details": null
                        }"""))
                .willReturn(WireMock.badRequest()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody("""
                                {
                                    "errors": ["error 1", "error 2"]
                                }""")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("catalogue/products/edit"),
                        model().attribute("product", new Product(1, "Product 1", "Details 1")),
                        model().attribute("errors", List.of("error 1", "error 2")),
                        model().attribute("payload", new UpdateProductPayload("  ", null))
                );

        WireMock.verify(WireMock.patchRequestedFor(WireMock.urlPathMatching("/catalogue-api/products/1"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "  ",
                            "details": null
                        }""")));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateProduct_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));

        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .locale(Locale.ENGLISH)
                .with(csrf());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Product not found")
                );
    }

    @Test
    @WithMockUser
    void updateProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        // when
        this.mockMvc.perform(post("/catalogue/products/1/edit"))
                // then
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void deleteProduct_RedirectsToProductListPage() throws Exception {
        // given
        WireMock.stubFor(WireMock.delete("/catalogue-api/products/1")
                .willReturn(WireMock.noContent()));

        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/delete")
                .with(csrf());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/catalogue/products/list")
                );

        WireMock.verify(WireMock.deleteRequestedFor(
                WireMock.urlPathMatching("/catalogue-api/products/1")));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void deleteProduct_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));

        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/delete")
                .locale(Locale.ENGLISH)
                .with(csrf());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Product not found")
                );
    }

    @Test
    @WithMockUser
    void deleteProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        // when
        this.mockMvc.perform(post("/catalogue/products/1/delete"))
                // then
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
