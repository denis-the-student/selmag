package ag.selm.customer.controller;

import ag.selm.customer.client.FavouriteProductsClient;
import ag.selm.customer.client.ProductReviewsClient;
import ag.selm.customer.client.ProductsClient;
import ag.selm.customer.client.exception.ClientBadRequestException;
import ag.selm.customer.controller.payload.NewProductReviewPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.web.reactive.result.view.CsrfRequestDataValueProcessor;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@RequestMapping("customer/products/{productId:\\d+}")
@Slf4j
public class ProductController {

    private final ProductsClient productsClient;
    private final FavouriteProductsClient favouriteProductsClient;
    private final ProductReviewsClient productReviewsClient;
    private final MessageSource messageSource;

    @GetMapping
    public Mono<String> getProductPage(@PathVariable("productId") int productId, Model model) {
        return loadProductData(productId, model).thenReturn("customer/products/product");
    }

    @PostMapping("add-to-favourites")
    public Mono<String> addProductToFavourites(@PathVariable("productId") int productId) {

        return this.favouriteProductsClient
                .addProductToFavourites(productId)
                .thenReturn("redirect:/customer/products/%d".formatted(productId))
                .doOnError(exception -> log.error(exception.getMessage(), exception))
                .onErrorResume(exception -> Mono.just("redirect:/customer/products/%d".formatted(productId)));
    }

    @PostMapping("remove-from-favourites")
    public Mono<String> removeProductFromFavourites(@PathVariable("productId") int productId) {

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
                    return loadProductData(productId, model).thenReturn(("customer/products/product"));
                });
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(NoSuchElementException exception,
                                               Model model, Locale locale) {
        model.addAttribute("error",
                this.messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale));
        return "errors/404";
    }

    private Mono<Void> loadProductData(int productId, Model model) {
        return Mono.zip(
                        productsClient.findProduct(productId)
                                .switchIfEmpty(Mono.error(new NoSuchElementException("customer.products.error.not_found"))),
                        favouriteProductsClient.findFavouriteProductByProductId(productId)
                                .map(favouriteProduct -> true)
                                .defaultIfEmpty(false),
                        productReviewsClient.findProductReviewsByProduct(productId)
                                .collectList()
                )
                .flatMap(tuple -> {
                    model.addAttribute("product", tuple.getT1());
                    model.addAttribute("inFavourite", tuple.getT2());
                    model.addAttribute("reviews", tuple.getT3());
                    return Mono.empty();
                });
    }

    @ModelAttribute
    public Mono<CsrfToken> loadCsrfToken(ServerWebExchange exchange) {
        Mono<CsrfToken> attribute = exchange.getAttribute(CsrfToken.class.getName());
        return attribute.doOnSuccess(token -> exchange.getAttributes()
                .put(CsrfRequestDataValueProcessor.DEFAULT_CSRF_ATTR_NAME, token));
    }
}
