package ag.selm.customer.controller;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 54321)
class ProductsControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @WithMockUser
    void getProductsListPage_ReturnsProductsPage() {
        // given
        stubFor(get(urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", equalTo("filtered"))
                .willReturn(okJson("""
                        [
                        {
                            "id": 1,
                            "title": "Filtered product 1",
                            "details": "Details 1"
                        },
                        {
                            "id": 2,
                            "title": "Filtered product 2",
                            "details": "Details 2"
                        }]""")));

        // when
        webTestClient
                .get()
                .uri("/customer/products/list?filter=filtered")
                .exchange()
                // then
                .expectStatus().isOk();
    }

    @Test
    void getProductsListPage_UserIsNotAuthenticated__RedirectsToLoginPage() {
        // given
        // when
        webTestClient
                .get()
                .uri("/customer/products/list?filter=filtered")
                .exchange()
                // then
                .expectStatus().is3xxRedirection();
    }

    @Test
    @WithMockUser
    void getFavouriteProductsPage_UserHasFavouriteProducts_ReturnsFavouriteProductsPage() {
        // given
        stubFor(get(urlPathMatching("/feedback-api/favourite-products"))
                .willReturn(okJson("""
                        [
                        {
                            "id": "cfad4cf0-d3f0-4ff5-afa1-09d38e69668c",
                            "product-id": 1
                        },
                        {
                            "id": "78bad88b-208d-44a2-8664-643d72938e59",
                            "product-id": 2
                        }]""")));

        stubFor(get(urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", equalTo("filtered"))
                .willReturn(okJson("""
                        [
                        {
                            "id": 1,
                            "title": "Filtered product 1",
                            "details": "Details 1"
                        },
                        {
                            "id": 2,
                            "title": "Filtered product 2",
                            "details": "Details 2"
                        }]""")));

        // when
        webTestClient
                .get()
                .uri("/customer/products/favourites?filter=filtered")
                .exchange()
                // then
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser
    void getFavouriteProductsPage_UserDoesNotHaveFavouriteProducts_ReturnsFavouriteProductsPage() {
        // given
        stubFor(get(urlPathMatching("/feedback-api/favourite-products"))
                .willReturn(okJson("[]")));

        // when
        webTestClient
                .get()
                .uri("/customer/products/favourites?filter=filtered")
                .exchange()
                // then
                .expectStatus().isOk();
    }

    @Test
    void getFavouriteProductsPage_UserIsNotAuthenticated_RedirectsToLoginPage() {
        // given
        // when
        webTestClient
                .get()
                .uri("/customer/products/favourites?filter=filtered")
                .exchange()
                // then
                .expectStatus().is3xxRedirection();
    }
}