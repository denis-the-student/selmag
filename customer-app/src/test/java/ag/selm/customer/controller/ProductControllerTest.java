package ag.selm.customer.controller;

import ag.selm.customer.client.FavouriteProductsClient;
import ag.selm.customer.client.ProductReviewsClient;
import ag.selm.customer.client.ProductsClient;
import ag.selm.customer.client.exception.ClientBadRequestException;
import ag.selm.customer.controller.payload.NewProductReviewPayload;
import ag.selm.customer.entity.FavouriteProduct;
import ag.selm.customer.entity.Product;
import ag.selm.customer.entity.ProductReview;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    Model model;

    @Mock
    ProductsClient productsClient;

    @Mock
    FavouriteProductsClient favouriteProductsClient;

    @Mock
    ProductReviewsClient productReviewsClient;

    @InjectMocks
    ProductController controller;

    @Test
    void loadProduct_ReturnsProductMono() {
        // given
        var productId = 1;
        var product = new Product(productId, "Product 1", "Details 1");

        when(productsClient.findProduct(anyInt())).thenReturn(Mono.just(product));

        // when
        StepVerifier.create(controller.loadProduct(productId))
                // then
                .expectNext(product)
                .verifyComplete();

        verifyNoMoreInteractions(productsClient);
        verifyNoInteractions(favouriteProductsClient, productReviewsClient);
    }

    @Test
    void loadProduct_ProductDoesNotExist_ReturnsMonoWithNoSuchElementException() {
        // given
        var productId = 1;

        when(productsClient.findProduct(anyInt())).thenReturn(Mono.empty());

        // when
        StepVerifier.create(controller.loadProduct(productId))
                // then
                .expectErrorMatches(exception -> exception instanceof NoSuchElementException &&
                        exception.getMessage().equals("customer.products.error.not_found"))
                .verify();

        verifyNoMoreInteractions(productsClient);
        verifyNoInteractions(favouriteProductsClient, productReviewsClient);
    }

    @Test
    void getProductPage_ReturnsProductPage() {
        // given
        var productId = 1;
        var favProductId = UUID.fromString("633d61d4-87ce-4494-bab5-28cf7dd9cab8");
        var reviewId = UUID.fromString("c4467822-d273-4c66-9e16-2b1c072abce8");
        var rating = 5;
        var review = "Отличный товар";

        var favProduct = new FavouriteProduct(favProductId, productId);
        var productReview = new ProductReview(reviewId, productId, rating, review);

        when(favouriteProductsClient.findFavouriteProductByProductId(productId))
                .thenReturn(Mono.just(favProduct));
        when(productReviewsClient.findProductReviewsByProduct(productId))
                .thenReturn(Flux.just(productReview));

        // when
        StepVerifier.create(controller.getProductPage(productId, model))
                // then
                .expectNext("customer/products/product")
                .verifyComplete();

        verify(favouriteProductsClient).findFavouriteProductByProductId(productId);
        verify(productReviewsClient).findProductReviewsByProduct(productId);
        verify(model).addAttribute("inFavourite", true);
        verify(model).addAttribute("reviews", List.of(productReview));
    }

    @Test
    void addProductToFavourites_RedirectsToProductPage() {
        // given
        var productId = 1;
        var favProductId = UUID.fromString("633d61d4-87ce-4494-bab5-28cf7dd9cab8");
        var favProduct = new FavouriteProduct(favProductId, productId);

        when(favouriteProductsClient.addProductToFavourites(anyInt()))
                .thenReturn(Mono.just(favProduct));

        // when
        StepVerifier.create(controller.addProductToFavourites(productId))
                // then
                .expectNext(String.format("redirect:/customer/products/%d", productId))
                .verifyComplete();

        verify(favouriteProductsClient).addProductToFavourites(anyInt());
        verifyNoMoreInteractions(favouriteProductsClient);
        verifyNoInteractions(productsClient, productReviewsClient);
    }

    @Test
    void addProductToFavourites_RequestIsInvalid_RedirectsToProductPage() {
        // given
        var productId = 1;

        when(favouriteProductsClient.addProductToFavourites(anyInt()))
                .thenReturn(Mono.error(new ClientBadRequestException(
                        "Ошибка при добавлении в избранное", null, List.of("add to favourites error"))));

        // when
        StepVerifier.create(controller.addProductToFavourites(productId))
                // then
                .expectNext(String.format("redirect:/customer/products/%d", productId))
                .verifyComplete();

        verify(favouriteProductsClient).addProductToFavourites(anyInt());
        verifyNoMoreInteractions(favouriteProductsClient);
        verifyNoInteractions(productsClient, productReviewsClient);
    }

    @Test
    void removeProductFromFavourites_RedirectsToProductPage() {
        // given
        var productId = 1;

        when(favouriteProductsClient.removeProductFromFavourites(anyInt()))
                .thenReturn(Mono.empty());

        // when
        StepVerifier.create(controller.removeProductFromFavourites(productId))
                // then
                .expectNext(String.format("redirect:/customer/products/%d", productId))
                .verifyComplete();

        verify(favouriteProductsClient).removeProductFromFavourites(anyInt());
        verifyNoMoreInteractions(favouriteProductsClient);
        verifyNoInteractions(productsClient, productReviewsClient);
    }

    @Test
    void createReview_RedirectsToProductPage() {
        // given
        var productId = 1;
        var reviewId = UUID.fromString("c4467822-d273-4c66-9e16-2b1c072abce8");
        var rating = 5;
        var review = "Отличный товар";
        var productReview = new ProductReview(reviewId, productId, rating, review);
        var payload = new NewProductReviewPayload(productId, rating, review);

        when(productReviewsClient.createProductReview(anyInt(), anyInt(), anyString()))
                        .thenReturn(Mono.just(productReview));

        // when
        StepVerifier.create(controller.createReview(productId, model, payload))
        // then
                .expectNext(String.format("redirect:/customer/products/%d", productId))
                .verifyComplete();

        verify(productReviewsClient).createProductReview(anyInt(), anyInt(), anyString());
        verifyNoMoreInteractions(productReviewsClient);
        verifyNoInteractions(productsClient, favouriteProductsClient);
    }

    @Test
    void createReview_RequestIsInvalid_RedirectsToProductPage() {
        // given
        var productId = 1;
        var rating = 5;
        var review = "Отличный товар";
        var payload = new NewProductReviewPayload(productId, rating, review);
        var error = new ClientBadRequestException(
                "Ошибка при создании отзыва", null, List.of("review creation error"));

        when(productReviewsClient.createProductReview(anyInt(), anyInt(), anyString()))
                .thenReturn(Mono.error(error));

        when(favouriteProductsClient.findFavouriteProductByProductId(anyInt())).thenReturn(Mono.empty());
        when(productReviewsClient.findProductReviewsByProduct(anyInt())).thenReturn(Flux.empty());

        // when
        StepVerifier.create(controller.createReview(productId, model, payload))
                // then
                .expectNext("customer/products/product")
                .verifyComplete();

        verify(productReviewsClient).createProductReview(anyInt(), anyInt(), anyString());
        verify(favouriteProductsClient).findFavouriteProductByProductId(anyInt());
        verify(model).addAttribute("inFavourite", false);
        verify(model).addAttribute("reviews", List.of());
        verify(model).addAttribute("payload", payload);
        verify(model).addAttribute("errors", error.getErrors());
        verifyNoMoreInteractions(productReviewsClient, favouriteProductsClient);
        verifyNoInteractions(productsClient);
    }


    @Test
    void handleNoSuchElementException_Returns404Page() {
        // given
        var exception = new NoSuchElementException("message");
        var response = new MockServerHttpResponse();

        // when
        var result = controller.handleNoSuchElementException(exception, model, response);

        // then
        assertEquals("errors/404", result);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(model).addAttribute("error", exception.getMessage());
    }

}