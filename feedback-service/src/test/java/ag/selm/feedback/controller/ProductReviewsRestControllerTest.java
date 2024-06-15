package ag.selm.feedback.controller;

import ag.selm.feedback.controller.payload.NewProductReviewPayload;
import ag.selm.feedback.entity.ProductReview;
import ag.selm.feedback.service.ProductReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductReviewsRestControllerTest {

    UUID uuid1 = UUID.fromString("e905ca16-a1d5-4b7d-bee7-130be8e5500e");
    UUID uuid2 = UUID.fromString("09289cba-150e-4b4f-a5a5-2fe9f88fbd8e");

    @Mock
    ProductReviewService service;

    @InjectMocks
    ProductReviewsRestController controller;


    @Test
    void findProductReviewsByProduct_ReturnsProductReviews() {
        // given
        var productReview1 = new ProductReview(uuid1, 1, 5, "Отлично", "user-test1");
        var productReview2 = new ProductReview(uuid2, 1, 3, "Сойдёт", "user-test2");

        when(service.findProductReviewsByProduct(anyInt()))
                .thenReturn(Flux.fromIterable(List.of(productReview1, productReview2)));

        // when
        StepVerifier.create(controller.findProductReviewsByProduct(1))
                // then
                .expectNext(productReview1, productReview2)
                .verifyComplete();

        verifyNoMoreInteractions(service);
    }

    @Test
    void createProductReview_ReturnsCreatedProductReview() {
        // given
        var productReview = new ProductReview(uuid1, 1, 5, "Отлично", "user-test-1");
        var payload = new NewProductReviewPayload(productReview.getProductId(),
                productReview.getRating(), productReview.getReview());

        when(service.createProductReview(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(Mono.just(productReview));

        // when
        StepVerifier.create(controller.createProductReview(Mono.just(payload),
                        UriComponentsBuilder.fromUriString("http://localhost"), Mono.just(new JwtAuthenticationToken(
                                Jwt.withTokenValue("token")
                                        .header("foo", "bar")
                                        .claim("sub", "user-test-1")
                                        .build()))))
                // then
                .expectNext(ResponseEntity.created(
                        URI.create(String.format("http://localhost/feedback-api/product-reviews/%s", uuid1)))
                        .body(productReview))
                .verifyComplete();

        verify(service).createProductReview(anyInt(), anyInt(), anyString(), anyString());
        verifyNoMoreInteractions(service);
    }
}