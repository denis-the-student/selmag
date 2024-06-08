package ag.selm.customer.controller;

import ag.selm.customer.client.FavouriteProductsClient;
import ag.selm.customer.client.ProductReviewsClient;
import ag.selm.customer.client.ProductsClient;
import ag.selm.customer.client.exception.ClientBadRequestException;
import ag.selm.customer.controller.payload.NewProductReviewPayload;
import ag.selm.customer.entity.Product;
import ag.selm.customer.entity.ProductReview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@RequestMapping("customer/products/{productId:\\d+}")
@Slf4j
public class ProductController {

    private final ProductsClient productsClient;
    private final FavouriteProductsClient favouriteProductsClient;
    private final ProductReviewsClient productReviewsClient;

    @ModelAttribute(value = "product", binding = false)
    public Mono<Product> loadProduct(@PathVariable("productId") int productId) {

        return this.productsClient.findProduct(productId)
            .switchIfEmpty(Mono.error(new NoSuchElementException("customer.products.error.not_found")));
    }

    @ModelAttribute("inFavourite")
    public Mono<Boolean> isProductInFavourite(@PathVariable("productId") int id) {

        return this.favouriteProductsClient
            .findFavouriteProductByProductId(id)
            .map(favouriteProduct -> true)
            .defaultIfEmpty(false);
    }

    @ModelAttribute("reviews")
    public Mono<List<ProductReview>> loadProductReviews(@PathVariable("productId") int productId) {

        return this.productReviewsClient
            .findProductReviewsByProduct(productId)
            .collectList();
    }

    @GetMapping
    public Mono<String> getProductPage() {

        return Mono.just("customer/products/product");
    }

    @PostMapping("add-to-favourites")
    public Mono<String> addProductToFavourites(
        @PathVariable("productId") int productId) {

        return this.favouriteProductsClient
            .addProductToFavourites(productId)
            .thenReturn("redirect:/customer/products/%d".formatted(productId))
            .doOnError(exception -> log.error(exception.getMessage(), exception))
            .onErrorResume(exception ->
                Mono.just("redirect:/customer/products/%d".formatted(productId)));
    }

    @PostMapping("remove-from-favourites")
    public Mono<String> removeProductFromFavourites(
        @PathVariable("productId") int productId) {

        return this.favouriteProductsClient
            .removeProductFromFavourites(productId)
            .thenReturn("redirect:/customer/products/%d".formatted(productId));
    }

    @PostMapping("create-review")
    public Mono<String> createReview(@PathVariable("productId") Integer productId,
                                     Model model, NewProductReviewPayload payload) {

        return this.productReviewsClient
            .createProductReview(productId, payload.rating(), payload.review())
            .thenReturn("redirect:/customer/products/%d".formatted(productId))
            .onErrorResume(ClientBadRequestException.class, exception -> {
                model.addAttribute("payload", payload);
                model.addAttribute("errors", exception.getErrors());
                return Mono.just("customer/products/product");
            });
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(NoSuchElementException exception,
                                               Model model) {
        model.addAttribute("error", exception.getMessage());
        return "errors/404";
    }
}
