package ag.selm.customer.controller;

import ag.selm.customer.client.FavouriteProductsClient;
import ag.selm.customer.client.ProductsClient;
import ag.selm.customer.entity.FavouriteProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/products/")
public class ProductsController {

    private final ProductsClient productsClient;

    private final FavouriteProductsClient favouriteProductsClient;

    @GetMapping("list")
    public Mono<String> getProductsListPage(
            Model model,
            @RequestParam(name = "filter", required = false) String filter) {

        model.addAttribute("filter", filter);

        return productsClient.findAllProducts(filter)
                .collectList()
                .doOnNext(products -> model.addAttribute("products", products))
                .thenReturn("customer/products/list");
    }

    @GetMapping("favourites")
    public Mono<String> getFavouriteProductsPage(
            Model model,
            @RequestParam(name = "filter", required = false) String filter) {

        return favouriteProductsClient.findAllFavouriteProducts()
                .map(FavouriteProduct::productId)
                .collectList()
                .flatMap(favouriteProductIds -> {
                    if (favouriteProductIds.isEmpty()) {
                        return Mono.just("customer/products/favourites");
                    } else {
                        return productsClient.findProductsByIds(favouriteProductIds, filter)
                                .collectList()
                                .doOnNext(products -> model.addAttribute("products", products))
                                .thenReturn("customer/products/favourites");
                    }
                });
    }
}
