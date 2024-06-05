package ag.selm.customer.service;

import ag.selm.customer.entity.FavouriteProduct;
import ag.selm.customer.repository.FavouriteProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultFavouriteProductService implements FavouriteProductService {

    private final FavouriteProductRepository repository;

    @Override
    public Mono<FavouriteProduct> addProductToFavourites(int productId) {
        return this.repository.save(
            new FavouriteProduct(UUID.randomUUID(), productId));
    }

    @Override
    public Mono<Void> removeProductFromFavourites(int productId) {
        return this.repository.deleteByProductId(productId);
    }

    @Override
    public Mono<FavouriteProduct> findFavouriteProductByProductId(int productId) {
        return this.repository.findByProductId(productId);
    }

    @Override
    public Flux<FavouriteProduct> findAllFavouriteProducts() {
        return this.repository.findAll();
    }
}
