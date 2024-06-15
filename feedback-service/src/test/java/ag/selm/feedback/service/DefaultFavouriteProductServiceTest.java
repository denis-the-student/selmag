package ag.selm.feedback.service;

import ag.selm.feedback.entity.FavouriteProduct;
import ag.selm.feedback.repository.FavouriteProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultFavouriteProductServiceTest {

    @Mock
    FavouriteProductRepository repository;

    @InjectMocks
    DefaultFavouriteProductService service;

    @Test
    void addProductToFavourites_WhenFavouriteProductDoesNotExist_ReturnsCreatedFavouriteProduct() {
        var id = UUID.fromString("67bc59b5-4db8-40a4-a42a-9fb097cbf988");
        var productId = 1;
        var userId = "user-id";

        // given
        when(repository.findByProductIdAndUserId(anyInt(), anyString())).thenReturn(Mono.empty());
        when(repository.save(any(FavouriteProduct.class))).thenReturn(
                Mono.just(new FavouriteProduct(id, productId, userId)));

        // when
        StepVerifier.create(service.addProductToFavourites(productId, userId))
                // then
                .expectNextMatches(favouriteProduct -> favouriteProduct.id() == id &&
                        favouriteProduct.productId() == productId &&
                        favouriteProduct.userId().equals(userId))
                .verifyComplete();

        verify(repository).findByProductIdAndUserId(anyInt(), anyString());
        verify(repository).save(any(FavouriteProduct.class));
    }

    @Test
    void addProductToFavourites_WhenFavouriteProductAlreadyExists_DoesNothing() {
        var id = UUID.fromString("67bc59b5-4db8-40a4-a42a-9fb097cbf988");
        var productId = 1;
        var userId = "user-id";

        // given
        when(repository.findByProductIdAndUserId(anyInt(), anyString())).thenReturn(
                Mono.just(new FavouriteProduct(id, productId, userId)));

        // when
        StepVerifier.create(service.addProductToFavourites(productId, userId))
                // then
                .verifyComplete();

        verify(repository).findByProductIdAndUserId(anyInt(), anyString());
        verify(repository, never()).save(any(FavouriteProduct.class));
    }

    @Test
    void removeProductFromFavourites_ReturnsEmptyMono() {
        // given
        var productId = 1;
        var userId = "user-id";

        when(repository.deleteByProductIdAndUserId(anyInt(), anyString())).thenReturn(Mono.empty());

        // when
        StepVerifier.create(service.removeProductFromFavourites(productId, userId))
                // then
                .verifyComplete();

        verify(repository).deleteByProductIdAndUserId(anyInt(), anyString());
    }

    @Test
    void findFavouriteProductByProduct_ReturnsFavouriteProduct() {
        // given
        var id = UUID.fromString("67bc59b5-4db8-40a4-a42a-9fb097cbf988");
        var productId = 1;
        var userId = "user-id";
        var favProduct = new FavouriteProduct(id, productId, userId);

        when(repository.findByProductIdAndUserId(anyInt(), anyString()))
                .thenReturn(Mono.just(favProduct));

        // when
        StepVerifier.create(service.findFavouriteProductByProduct(productId, userId))
                // then
                .expectNext(favProduct)
                .verifyComplete();
    }

    @Test
    void findFavouriteProducts_ReturnsFavouriteProducts() {
        // given
        var userId = "user-id";

        var id1 = UUID.fromString("67bc59b5-4db8-40a4-a42a-9fb097cbf988");
        var productId1 = 1;
        var favProduct1 = new FavouriteProduct(id1, productId1, userId);

        var id2 = UUID.fromString("12ed83c0-fd59-48e2-ad76-553020567aeb");
        var productId2 = 2;
        var favProduct2 = new FavouriteProduct(id2, productId2, userId);

        when(repository.findAllByUserId(userId)).thenReturn(Flux.just(favProduct1, favProduct2));

        // when
        StepVerifier.create(service.findFavouriteProducts(userId))
                // then
                .expectNext(favProduct1, favProduct2)
                .verifyComplete();
    }
}