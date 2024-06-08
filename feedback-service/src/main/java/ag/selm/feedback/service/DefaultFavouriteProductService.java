package ag.selm.feedback.service;

import ag.selm.feedback.entity.FavouriteProduct;
import ag.selm.feedback.repository.FavouriteProductRepository;
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
        return this.repository.findByProductId(productId).hasElement()
            .flatMap(hasElement -> {
                if (hasElement) {
                    return Mono.empty();
                }
                else {
                    return this.repository.save(
                        new FavouriteProduct(UUID.randomUUID(), productId));
                }
            });
    }

    @Override
    public Mono<Void> removeProductFromFavourites(int productId) {
        return this.repository.deleteByProductId(productId);
    }

    @Override
    public Mono<FavouriteProduct> findFavouriteProductByProduct(int productId) {
        return this.repository.findByProductId(productId);
    }

    @Override
    public Flux<FavouriteProduct> findFavouriteProducts() {
        return this.repository.findAll();
    }
}
