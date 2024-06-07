package ag.selm.customer.config;

import ag.selm.customer.client.WebClientFavouriteProductsClient;
import ag.selm.customer.client.WebClientProductReviewsClient;
import ag.selm.customer.client.WebClientProductsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientBeans {

    @Bean
    WebClientProductsClient webClientProductsClient(
        @Value("${selmag.service.catalogue.url:http://localhost:8081/catalogue-api/products}") String baseUrl) {

        return new WebClientProductsClient(
            WebClient
                .builder()
                .baseUrl(baseUrl)
                .build());
    }

    @Bean
    WebClientProductReviewsClient webClientProductReviewsClient(
            @Value("${selmag.service.feedback.product-reviews.url:http://localhost:8084/feedback-api/product-reviews}") String baseUrl) {

        return new WebClientProductReviewsClient(
                WebClient
                        .builder()
                        .baseUrl(baseUrl)
                        .build());
    }

    @Bean
    WebClientFavouriteProductsClient webClientFavouriteProductsClient(
            @Value("${selmag.service.feedback.favourite-products.url:http://localhost:8084/feedback-api/favourite-products}") String baseUrl) {

        return new WebClientFavouriteProductsClient(
                WebClient
                        .builder()
                        .baseUrl(baseUrl)
                        .build());
    }
}
