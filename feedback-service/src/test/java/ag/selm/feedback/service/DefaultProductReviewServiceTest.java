package ag.selm.feedback.service;

import ag.selm.feedback.entity.ProductReview;
import ag.selm.feedback.repository.ProductReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultProductReviewServiceTest {

    @Mock
    ProductReviewRepository repository;

    @InjectMocks
    DefaultProductReviewService service;

    @Test
    void createProductReview_ReturnsProductReview() {
        // given
        var id = UUID.fromString("67bc59b5-4db8-40a4-a42a-9fb097cbf988");
        var productId = 1;
        var rating = 4;
        var review = "Хороший товар";
        var userId = "user-id";

        var productReview = new ProductReview(id, productId, rating, review, userId);

        when(repository.save(any(ProductReview.class))).thenReturn(Mono.just(productReview));

        // when
        StepVerifier.create(service.createProductReview(productId, rating, review, userId))
                // then
                .expectNext(productReview)
                .verifyComplete();

        verify(repository).save(any(ProductReview.class));
    }

    @Test
    void findProductReviewsByProduct() {
        // given
        var productId = 1;

        var id1 = UUID.fromString("67bc59b5-4db8-40a4-a42a-9fb097cbf988");
        var rating1 = 4;
        var review1 = "Хороший товар";
        var userId1 = "user-1";
        var productReview1 = new ProductReview(id1, productId, rating1, review1, userId1);

        var id2 = UUID.fromString("67bc59b5-4db8-40a4-a42a-9fb097cbf988");
        var rating2 = 2;
        var review2 = "Плохой товар";
        var userId2 = "user-2";
        var productReview2 = new ProductReview(id2, productId, rating2, review2, userId2);

        when(repository.findAllByProductId(anyInt())).thenReturn(Flux.just(productReview1, productReview2));

        // when
        StepVerifier.create(service.findProductReviewsByProduct(1))
                // then
                .expectNext(productReview1, productReview2)
                .verifyComplete();
    }
}