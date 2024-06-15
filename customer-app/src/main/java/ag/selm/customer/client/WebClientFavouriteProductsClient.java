package ag.selm.customer.client;

import ag.selm.customer.client.exception.util.ErrorHandlingUtils;
import ag.selm.customer.controller.payload.FavouriteProductPayload;
import ag.selm.customer.entity.FavouriteProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class WebClientFavouriteProductsClient implements FavouriteProductsClient {

    private final WebClient webClient;

    @Override
    public Flux<FavouriteProduct> findAllFavouriteProducts() {
        return this.webClient
                .get()
                .retrieve()
                .bodyToFlux(FavouriteProduct.class);
    }

    @Override
    public Mono<FavouriteProduct> findFavouriteProductByProductId(int productId) {
        return this.webClient
                .get()
                .uri("/by-product-id/{productId}", productId)
                .retrieve()
                .bodyToMono(FavouriteProduct.class)
                .onErrorComplete(WebClientResponseException.NotFound.class);
    }

    @Override
    public Mono<FavouriteProduct> addProductToFavourites(int productId) {
        return this.webClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new FavouriteProductPayload(productId))
                .retrieve()
                .bodyToMono(FavouriteProduct.class)
                .onErrorMap(WebClientResponseException.class, exception ->
                        ErrorHandlingUtils.mapWebclientResponseExceptionToClientBadRequestException(
                                "Ошибка при добавлении в избранное", exception));
    }

    @Override
    public Mono<Void> removeProductFromFavourites(int productId) {
        return this.webClient
                .delete()
                .uri("/by-product-id/{productId}", productId)
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}
