package ag.selm.feedback.controller;

import ag.selm.feedback.controller.payload.NewProductReviewPayload;
import ag.selm.feedback.entity.ProductReview;
import ag.selm.feedback.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("feedback-api/product-reviews")
public class ProductReviewsRestController {

    private final ProductReviewService productReviewService;

    @GetMapping("/by-product-id/{productId:\\d+}")
    public Flux<ProductReview> findProductReviewsByProduct(
            @PathVariable("productId") int productId) {

        return this.productReviewService.findProductReviewsByProduct(productId);
    }

    @PostMapping
    public Mono<ResponseEntity<ProductReview>> createProductReview(
            @Valid @RequestBody Mono<NewProductReviewPayload> payloadMono,
            UriComponentsBuilder uriComponentsBuilder) {

        return payloadMono
                .flatMap(payload -> this.productReviewService.createProductReview(
                        payload.productId(), payload.rating(), payload.review()))
                .map(productReview -> ResponseEntity
                        .created(uriComponentsBuilder
                                .replacePath("feedback-api/product-reviews/{productId}")
                                .build(productReview.getId()))
                        .body(productReview));
    }
}
