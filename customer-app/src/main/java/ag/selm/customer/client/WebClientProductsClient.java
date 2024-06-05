package ag.selm.customer.client;

import ag.selm.customer.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public Mono<Product> findProduct(int productId) {
        return webClient
            .get()
            .uri("/{productId}", productId)
            .retrieve()
            .bodyToMono(Product.class)
            .onErrorComplete(WebClientResponseException.NotFound.class);
    }


}