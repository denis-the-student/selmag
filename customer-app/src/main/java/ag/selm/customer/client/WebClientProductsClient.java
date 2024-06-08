package ag.selm.customer.client;

import ag.selm.customer.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class WebClientProductsClient implements ProductsClient {

    private final WebClient webClient;

    @Override
    public Flux<Product> findAllProducts(String filter) {
        return webClient
            .get()
            .uri("?filter={filter}", filter)
            .retrieve()
            .bodyToFlux(Product.class);
    }

    @Override
    public Flux<Product> findProductsByIds(List<Integer> ids, String filter) {
        return webClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/by-ids")
                .queryParam("ids", ids)
                .queryParamIfPresent("filter", Optional.ofNullable(filter))
                .build())
            .retrieve()
            .bodyToFlux(Product.class);
    }

    @Override
    public Mono<Product> findProduct(int productId) {
        return webClient
            .get()
            .uri("/{productId}", productId)
            .retrieve()
            .bodyToMono(Product.class)
            .onErrorComplete(WebClientResponseException.NotFound.class);
    }


}
