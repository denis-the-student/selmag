package ag.selm.customer.controller;

import ag.selm.customer.client.FavouriteProductsClient;
import ag.selm.customer.client.ProductsClient;
import ag.selm.customer.entity.FavouriteProduct;
import ag.selm.customer.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsControllerTest {

    @Mock
    ProductsClient productsClient;

    @Mock
    FavouriteProductsClient favouriteProductsClient;

    @Mock
    Model model;

    @InjectMocks
    ProductsController controller;

    @Test
    void getProductsListPage_ReturnsProductsListPage() {
        Product product1 = new Product(1, "Filtered product 1", "Details 1");
        Product product2 = new Product(2, "Filtered product 2", "Details 2");

        // given
        when(productsClient.findAllProducts(anyString()))
                .thenReturn(Flux.just(product1, product2));

        // when
        StepVerifier.create(controller.getProductsListPage(model, "filtered"))
                // then
                .expectNext("customer/products/list")
                .verifyComplete();

        verify(model).addAttribute("filter", "filtered");
        verify(model).addAttribute("products", List.of(product1, product2));
        verifyNoMoreInteractions(productsClient);
        verifyNoInteractions(favouriteProductsClient);
    }

    @Test
    void getFavouriteProductsPage_UserHasFavouriteProducts_ReturnsFavouriteProductsPage() {
        // given
        UUID uuid1 = UUID.fromString("cfad4cf0-d3f0-4ff5-afa1-09d38e69668c");
        UUID uuid2 = UUID.fromString("78bad88b-208d-44a2-8664-643d72938e59");

        Product product1 = new Product(1, "Filtered product 1", "Details 1");
        Product product2 = new Product(2, "Filtered product 2", "Details 2");

        FavouriteProduct favProduct1 = new FavouriteProduct(uuid1, 1);
        FavouriteProduct favProduct2 = new FavouriteProduct(uuid2, 2);

        when(favouriteProductsClient.findAllFavouriteProducts())
                .thenReturn(Flux.just(favProduct1, favProduct2));
        when(productsClient.findProductsByIds(List.of(1, 2), "filtered"))
                .thenReturn(Flux.just(product1, product2));

        // when
        StepVerifier.create(controller.getFavouriteProductsPage(model, "filtered"))
                // then
                .expectNext("customer/products/favourites")
                .verifyComplete();

        verify(model).addAttribute("products", List.of(product1, product2));
        verifyNoMoreInteractions(favouriteProductsClient);
        verifyNoMoreInteractions(productsClient);
    }

    @Test
    void getFavouriteProductsPage_UserDoesntHaveFavouriteProducts_ReturnsFavouriteProductsPage() {
        // given
        when(favouriteProductsClient.findAllFavouriteProducts())
                .thenReturn(Flux.empty());

        // when
        StepVerifier.create(controller.getFavouriteProductsPage(model, "filtered"))
                // then
                .expectNext("customer/products/favourites")
                .verifyComplete();

        verifyNoMoreInteractions(favouriteProductsClient);
        verifyNoInteractions(productsClient);
    }
}