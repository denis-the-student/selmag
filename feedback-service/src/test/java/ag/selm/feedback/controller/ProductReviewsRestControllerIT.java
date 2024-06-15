package ag.selm.feedback.controller;

import ag.selm.feedback.entity.ProductReview;
import ag.selm.feedback.repository.ProductReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
class ProductReviewsRestControllerIT {

    UUID uuid1 = UUID.fromString("e905ca16-a1d5-4b7d-bee7-130be8e5500e");
    UUID uuid2 = UUID.fromString("09289cba-150e-4b4f-a5a5-2fe9f88fbd8e");
    UUID uuid3 = UUID.fromString("454f4451-3fc3-4c12-96ce-325dd7d61ccc");

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ProductReviewRepository productReviewRepository;

    @BeforeEach
    void setUp() {
        productReviewRepository.saveAll(List.of(
                new ProductReview(uuid1, 1, 5, "Review #1", "user-1"),
                new ProductReview(uuid2, 1, 3, "Review #1", "user-2"),
                new ProductReview(uuid3, 2, 5, "Review #1", "user-3")
        )).blockLast();
    }

    @AfterEach
    void tearDown() {
        productReviewRepository.deleteAll().block();
    }

    @Test
    @WithMockUser
    void findProductReviewsByProduct_ReturnsProductReviews() {
        // given
        // when
        webTestClient
                .get()
                .uri("/feedback-api/product-reviews/by-product-id/1")
                .exchange()
                // then
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json(String.format("""
                        [
                            {
                                "id": %s,
                                "productId": 1,
                                "rating": 5,
                                "review": "Review #1",
                                "userId": "user-1"
                            },
                            {
                                "id": %s,
                                "productId": 1,
                                "rating": 3,
                                "review": "Review #1",
                                "userId": "user-2"
                            }
                        ]""", uuid1, uuid2));
    }

    @Test
    void findProductReviewsByProduct_UserIsNotAuthenticated_ReturnsNotUnauthorized() {
        // given
        // when
        webTestClient
                .get()
                .uri("/feedback-api/product-reviews/by-product-id/1")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }

    @Test
    void createProductReview_ReturnsCreatedProductReview() {
        // given
        // when
        webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-tester")))
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 1,
                            "rating": 4,
                            "review": "Review #4"
                        }""")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        {
                            "productId": 1,
                            "rating": 4,
                            "review": "Review #4",
                            "userId": "user-tester"
                        }""").jsonPath("$.id").exists();
    }

    @Test
    void createProductReview_RequestIsInvalid_ReturnsBadRequest() {
        // given
        // when
        webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-tester")))
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en-US")
                .bodyValue(String.format("""
                                {
                                    "productId": null,
                                    "rating": -1,
                                    "review": "%s"
                                }""",
                        "a".repeat(1001)))

                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody().json("""
                        {
                            "errors": [
                                "Product not specified",
                                "Rating is below 1",
                                "Review must be no more than 1000 characters"
                            ]
                        }""");
    }

    @Test
    void createProductReview_UserIsNotAuthenticated_ReturnsNotUnauthorized() {
        // given
        // when
        webTestClient
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 1,
                            "rating": 4,
                            "review": "Review #4"
                        }""")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}