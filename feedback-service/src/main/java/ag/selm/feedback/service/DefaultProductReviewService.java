package ag.selm.feedback.service;

import ag.selm.feedback.entity.ProductReview;
import ag.selm.feedback.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultProductReviewService implements ProductReviewService {

    private final ProductReviewRepository productReviewRepository;

    @Override
    public Mono<ProductReview> createProductReview(
        int productId, Integer rating, String review) {

        return this.productReviewRepository.save(
            new ProductReview(UUID.randomUUID(), productId, rating, review));
    }

    @Override
    public Flux<ProductReview> findProductReviewsByProduct(int productId) {

        return this.productReviewRepository.findAllByProductId(productId);
    }
}
