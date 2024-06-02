package ag.selm.manager.controller;

import ag.selm.manager.entity.Product;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
@DisplayName("Интеграционные тесты ProductsController")
public class ProductsControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("getProductsList() вернёт страницу со списком товаров")
    void getProductsList_ReturnsProductsListPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders
                .get("/catalogue/products/list")
                .queryParam("filter", "товар")
                .with(user("zalupa").roles("MANAGER"));

        WireMock.stubFor(WireMock
                .get(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("товар"))
                .willReturn(WireMock.ok("""
                                [
                                {"id":  1, "title":  "Товар №1", "details": "Описание товара №1"},
                                {"id":  2, "title":  "Шоколадка", "details": "Описание шоколадки"},
                                {"id":  3, "title":  "Товар №3", "details": "Описание товара №3"}
                                ]
                                """)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/list"),
                        model().attribute("filter", "товар"),
                        model().attribute("products", List.of(
                                new Product(1, "Товар №1", "Описание товара №1"),
                                new Product(2, "Шоколадка", "Описание шоколадки"),
                                new Product(3, "Товар №3", "Описание товара №3")
                        )));

        WireMock.verify(WireMock
                .getRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("товар")));
    }

    @Test
    @DisplayName("getNewProductPage() вернёт страницу создания товара")
    void getNewProductPage_ReturnsNewProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders
                .get("/catalogue/products/create")
                .with(user("zalupa").roles("MANAGER"));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/new_product")
                );
    }
}
