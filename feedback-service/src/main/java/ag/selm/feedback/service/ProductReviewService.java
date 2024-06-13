package ag.selm.feedback.service;

import ag.selm.feedback.entity.ProductReview;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductReviewService {

    Mono<ProductReview> createProductReview(
        int productId, Integer rating, String review, String userId);

    Flux<ProductReview> findProductReviewsByProduct(int productId);
}
