package ag.selm.customer.controller;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@SpringBootTest
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 54321)
public class ProductControllerIT {

    UUID uuid = UUID.fromString("7fbd2c3e-962d-42cb-acbc-cb3809ac06b3");

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        stubFor(get("/catalogue-api/products/1")
                .willReturn(okJson("""
                        {
                            "id": 1,
                            "title": "Product 1",
                            "Details": "Product details 1"
                        }""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    @WithMockUser
    void addProductToFavourites_RedirectsToProductPage() {
        // given
        stubFor(post("/feedback-api/favourite-products")
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                            "productId": 1
                        }"""))
                .willReturn(created()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(String.format("""
                                {
                                    "id": %s,
                                    "productId": 1
                                }""", uuid))));

        // when
        webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/add-to-favourites")
                .exchange()
                // then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/customer/products/1");

        verify(postRequestedFor(urlPathMatching("/feedback-api/favourite-products"))
                .withRequestBody(equalToJson("""
                        {
                            "productId": 1
                        }""")));
    }

    @Test
    @WithMockUser
    void addProductToFavourites_ProductDoesNotExist_ReturnsNotFoundPage() {
        // given
        // when
        webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/2/add-to-favourites")
                .exchange()
                // then
                .expectStatus().isNotFound();

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products/2")));
    }
}
