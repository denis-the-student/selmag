package ag.selm.customer.controller;

import ag.selm.customer.client.ProductsClient;
import ag.selm.customer.controller.payload.NewProductReviewPayload;
import ag.selm.customer.entity.Product;
import ag.selm.customer.service.FavouriteProductService;
import ag.selm.customer.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@RequestMapping("customer/products/{productId:\\d+}")
public class ProductController {

    private final ProductsClient productsClient;

    private final FavouriteProductService favouriteProductService;

    private final ProductReviewService productReviewService;

    @ModelAttribute("product")
    public Mono<Product> loadProduct(@PathVariable("productId") int productId,
                                     Model model) {

        model.addAttribute("inFavourite", false);

        return this.favouriteProductService
            .findFavouriteProductByProductId(productId)
            .doOnNext(favouriteProduct -> model
                .addAttribute("inFavourite", true))
            .then(this.productsClient.findProduct(productId))
            .switchIfEmpty(Mono.error(new NoSuchElementException("customer.products.error.not_found")));
    }

    @GetMapping
    public Mono<String> getProductPage(
        @ModelAttribute("product") Mono<Product> productMono, Model model) {

        return productMono.flatMap(product -> {
            int productId = product.id();

            return productReviewService
                .findProductReviewsByProduct(productId)
                .collectList()
                .doOnNext(productReviews -> model
                    .addAttribute("reviews", productReviews))
                .thenReturn("customer/products/product");
        });
    }

    @PostMapping("add-to-favourites")
    public Mono<String> addProductToFavourites(
        @ModelAttribute("product") Mono<Product> productMono) {

        return productMono
            .map(Product::id)
            .flatMap(productId -> this.favouriteProductService
                .addProductToFavourites(productId)
                .thenReturn("redirect:/customer/products/%d".formatted(productId)));
    }

    @PostMapping("remove-from-favourites")
    public Mono<String> removeProductFromFavourites(
        @ModelAttribute("product") Mono<Product> productMono) {

        return productMono
            .map(Product::id)
            .flatMap(productId -> this.favouriteProductService
                .removeProductFromFavourites(productId)
                .thenReturn("redirect:/customer/products/%d".formatted(productId)));
    }

    @PostMapping("create-review")
    public Mono<String> createReview(
        @ModelAttribute("product") Mono<Product> productMono, Model model,
        @Valid NewProductReviewPayload payload, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("payload", payload);
            model.addAttribute("errors",
                bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .toList());
            return Mono.just("customer/products/product");

        } else {
            return productMono.flatMap(product -> {
                int productId = product.id();
                return this.productReviewService
                    .createProductReview(productId, payload.rating(), payload.review())
                    .thenReturn("redirect:/customer/products/%d".formatted(productId));
            });
        }
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(NoSuchElementException exception,
                                               Model model) {
        model.addAttribute("error", exception.getMessage());
        return "errors/404";
    }
}
