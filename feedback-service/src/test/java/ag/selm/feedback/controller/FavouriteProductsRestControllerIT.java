package ag.selm.feedback.controller;

import ag.selm.feedback.entity.FavouriteProduct;
import ag.selm.feedback.repository.FavouriteProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
public class FavouriteProductsRestControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    FavouriteProductRepository favouriteProductRepository;

    UUID uuid1 = UUID.fromString("67bc59b5-4db8-40a4-a42a-9fb097cbf988");
    UUID uuid2 = UUID.fromString("12ed83c0-fd59-48e2-ad76-553020567aeb");

    @BeforeEach
    void setUp() {
        favouriteProductRepository.saveAll(List.of(
                new FavouriteProduct(uuid1, 1, "user-1"),
                new FavouriteProduct(uuid2, 2, "user-1")
        )).blockLast();
    }

    @AfterEach
    void tearDown() {
        favouriteProductRepository.deleteAll().block();
    }

    @Test
    void findAllFavouriteProducts_ReturnsFavouriteProducts() {
        // given
        // when
        webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-1")))
                .get()
                .uri("/feedback-api/favourite-products")
                .exchange()
                // then
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        [
                            {
                                "id": "67bc59b5-4db8-40a4-a42a-9fb097cbf988",
                                "productId": 1,
                                "userId": "user-1"
                            },
                            {
                                "id": "12ed83c0-fd59-48e2-ad76-553020567aeb",
                                "productId": 2,
                                "userId": "user-1"
                            }
                        ]""");
    }

    @Test
    void findAllFavouriteProducts_UserNotAuthorized_ReturnsUnauthorized() {
        // given
        // when
        webTestClient
                .get()
                .uri("/feedback-api/favourite-products")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }

    @Test
    void findFavouriteProductByProductId_ReturnsFavouriteProduct() {
        // given
        // when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-1")))
                .get()
                .uri("/feedback-api/favourite-products/by-product-id/2")
                .exchange()
                // then
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        {
                            "id": "12ed83c0-fd59-48e2-ad76-553020567aeb",
                            "productId": 2,
                            "userId": "user-1"
                        }""");
    }

    @Test
    void findFavouriteProductByProductId_UserIsNotAuthenticated_ReturnsUnauthorized() {
        // given
        // when
        this.webTestClient
                .get()
                .uri("/feedback-api/favourite-products/by-product-id/2")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }

    @Test
    void addProductToFavourites_ReturnsFavouriteProduct() {
        // given
        // when
        webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-1")))
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 3
                        }""")
                .exchange()
                // then
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        {
                            "productId": 3,
                            "userId": "user-1"
                        }""").jsonPath("$.id").exists();
    }

    @Test
    void addProductToFavourites_RequestIsInvalid_ReturnsFavouriteProduct() {
        // given
        // when
        webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-1")))
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en-Us")
                .bodyValue("""
                        {
                            "productId": null
                        }""")
                .exchange()
                // then
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody().json("""
                        {
                            "errors": ["Product not specified"]
                        }""");
    }

    @Test
    void addProductToFavourites_UserNotAuthorized_ReturnsUnauthorized() {
        // given
        // when
        webTestClient
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 3
                        }""")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }

    @Test
    void removeProductFromFavourites_ReturnsNoContent() {
        // given
        // when
        webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-1")))
                .delete()
                .uri("/feedback-api/favourite-products/by-product-id/1")
                .exchange()
                // then
                .expectStatus().isNoContent();
    }

    @Test
    void removeProductFromFavourites_UserNotAuthorized_ReturnsUnauthorized() {
        // given
        // when
        webTestClient
                .delete()
                .uri("/feedback-api/favourite-products/by-product-id/1")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }
}
