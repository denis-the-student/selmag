package ag.selm.feedback.controller;

import ag.selm.feedback.controller.payload.FavouriteProductPayload;
import ag.selm.feedback.entity.FavouriteProduct;
import ag.selm.feedback.service.FavouriteProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("feedback-api/favourite-products")
public class FavouriteProductsRestController {

    private final FavouriteProductService favouriteProductService;

    @GetMapping
    public Flux<FavouriteProduct> findAllFavouriteProducts(
            Mono<JwtAuthenticationToken> authTokenMono) {

        return authTokenMono.flatMapMany(token -> this.favouriteProductService
                .findFavouriteProducts(token.getToken().getSubject()));
    }

    @GetMapping("/by-product-id/{productId:\\d+}")
    public Mono<FavouriteProduct> findFavouriteProductByProductId(
            @PathVariable("productId") int productId,
            Mono<JwtAuthenticationToken> authTokenMono) {

        return authTokenMono.flatMap(token -> this.favouriteProductService
                .findFavouriteProductByProduct(productId, token.getToken().getSubject()));
    }

    @PostMapping
    public Mono<ResponseEntity<FavouriteProduct>> addProductToFavourites(
            @Valid @RequestBody Mono<FavouriteProductPayload> payloadMono,
            UriComponentsBuilder uriComponentsBuilder,
            Mono<JwtAuthenticationToken> authTokenMono) {

        return Mono.zip(payloadMono, authTokenMono)
                .flatMap(tuple -> this.favouriteProductService.addProductToFavourites(
                        tuple.getT1().productId(), tuple.getT2().getToken().getSubject()))

                .map(favouriteProduct -> ResponseEntity.created(uriComponentsBuilder
                                .replacePath("feedback-api/favourite-products/{id}")
                                .build(favouriteProduct.id()))
                        .body(favouriteProduct));
    }

    @DeleteMapping("/by-product-id/{productId:\\d+}")
    public Mono<ResponseEntity<Void>> removeProductFromFavourites(
            @PathVariable() int productId,
            Mono<JwtAuthenticationToken> authTokenMono) {

        return authTokenMono.flatMap(token -> this.favouriteProductService.
                removeProductFromFavourites(productId, token.getToken().getSubject())
                .thenReturn(ResponseEntity.noContent().build()));
    }
}
