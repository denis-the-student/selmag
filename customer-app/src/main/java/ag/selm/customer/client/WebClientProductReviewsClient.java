package ag.selm.customer.client;

import ag.selm.customer.client.exception.util.ErrorHandlingUtils;
import ag.selm.customer.controller.payload.NewProductReviewPayload;
import ag.selm.customer.entity.ProductReview;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class WebClientProductReviewsClient implements ProductReviewsClient {

    private final WebClient webClient;

    @Override
    public Flux<ProductReview> findProductReviewsByProduct(Integer productId) {
        return this.webClient
            .get()
            .uri("/by-product-id/{productId}", productId)
            .retrieve()
            .bodyToFlux(ProductReview.class);
    }

    @Override
    public Mono<ProductReview> createProductReview(Integer productId, Integer rating,
                                                   String review) {

        return this.webClient
            .post()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new NewProductReviewPayload(productId, rating, review))
            .retrieve()
            .bodyToMono(ProductReview.class)
            .onErrorMap(WebClientResponseException.class,
                ErrorHandlingUtils::mapWebclientResponseExceptionToClientBadRequestException);
    }


}
