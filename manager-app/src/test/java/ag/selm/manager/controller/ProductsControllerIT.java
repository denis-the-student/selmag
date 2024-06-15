package ag.selm.manager.controller;

import ag.selm.manager.controller.payload.NewProductPayload;
import ag.selm.manager.entity.Product;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.extern.slf4j.Slf4j;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
@DisplayName("Интеграционные тесты ProductsController")
@Slf4j
public class ProductsControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("getProductsList() вернёт страницу со списком товаров")
    @WithMockUser(roles = "MANAGER")
    void getProductsListPage_ReturnsProductsListPage() throws Exception {
        // given
        List<Product> products = List.of(
                new Product(1, "Product 1", "Details 1"),
                new Product(2, "Product 2", "Details 2"));

        var requestBuilder = get("/catalogue/products/list")
                .queryParam("filter", "testFilter");

        WireMock.stubFor(WireMock
                .get(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("testFilter"))
                .willReturn(WireMock.okJson("""
                        [
                            {"id":  1, "title":  "Product 1", "details": "Details 1"},
                            {"id":  2, "title":  "Product 2", "details": "Details 2"}
                        ]""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/list"),
                        model().attribute("filter", "testFilter"),
                        model().attribute("products", products));

        WireMock.verify(WireMock
                .getRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("testFilter")));
    }

    @Test
    @WithMockUser
    void getProductsListPage_UserIsNotAuthorized_ShouldReturnForbidden() throws Exception {
        // given
        var requestBuilder = get("/catalogue/products/list")
                .queryParam("filter", "testFilter");

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getNewProductPage_ShouldReturnNewProductPage() throws Exception {
        // given
        // when
        this.mockMvc.perform(get("/catalogue/products/create"))
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/new_product"));
    }

    @Test
    @WithMockUser
    void getProductList_UserIsNotAuthorized_ShouldReturnForbidden() throws Exception {
        // given
        // when
        this.mockMvc.perform(get("/catalogue/products/create"))
                // then
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createProduct_shouldRedirectToProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders
                .post("/catalogue/products/create")
                .param("title", "New product")
                .param("details", "New details")
                .with(csrf());

        WireMock.stubFor(WireMock
                .post(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title":  "New product",
                            "details": "New details"
                        }"""))
                .willReturn(WireMock
                        .created()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": 1,
                                    "title": "New product",
                                    "details": "New details"
                                }""")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().is3xxRedirection(),
                        header().string(HttpHeaders.LOCATION, "/catalogue/products/1")
                );

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title":  "New product",
                            "details": "New details"
                        }""")));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createProduct_RequestIsInValid_ShouldReturnNewProductPageWithErrors() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders
                .post("/catalogue/products/create")
                .param("title", "  ")
                .with(csrf());

        WireMock.stubFor(WireMock
                .post(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title":  "  ",
                            "details": null
                        }"""))
                .willReturn(WireMock
                        .badRequest()
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
                        view().name("catalogue/products/new_product"),
                        model().attribute("payload", new NewProductPayload("  ", null)),
                        model().attribute("errors", List.of("error 1", "error 2"))
                );

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title":  "  ",
                            "details": null
                        }""")));
    }

    @Test
    @WithMockUser
    void createProduct_UserIsNotAuthorized_ShouldReturnForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders
                .post("/catalogue/products/create")
                .param("title", "New product")
                .param("details", "New details")
                .with(csrf());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
