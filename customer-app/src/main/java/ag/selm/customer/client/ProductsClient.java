package ag.selm.customer.client;

import ag.selm.customer.entity.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ProductsClient {

    Flux<Product> findAllProducts(String filter);

    Flux<Product> findProductsByIds(List<Integer> ids, String filter);

    Mono<Product> findProduct(int productId);
}
