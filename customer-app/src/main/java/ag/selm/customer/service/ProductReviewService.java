package ag.selm.customer.service;

import ag.selm.customer.entity.ProductReview;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductReviewService {

    Mono<ProductReview> createProductReview(
        int productId, Integer rating, String review);

    Flux<ProductReview> findProductReviewsByProduct(int productId);
}
