package ag.selm.customer.controller.config;

import ag.selm.customer.client.WebClientFavouriteProductsClient;
import ag.selm.customer.client.WebClientProductReviewsClient;
import ag.selm.customer.client.WebClientProductsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.Mockito.mock;

@Configuration
public class TestBeans {

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrationRepository() {
        return mock();
    }

    @Bean
    public ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return mock();
    }

    @Bean
    @Primary
    public WebClientProductsClient mockWebClientProductsClient(
            @Value("${selmag.service.catalogue.products.url}") String baseUrl) {

        return new WebClientProductsClient(
                WebClient.builder()
                        .baseUrl(baseUrl)
                        .build());
    }

    @Bean
    @Primary
    public WebClientFavouriteProductsClient mockWebClientFavouriteProductsClient(
            @Value("${selmag.service.feedback.favourite-products.url}") String baseUrl) {

        return new WebClientFavouriteProductsClient(
                WebClient.builder()
                        .baseUrl(baseUrl)
                        .build());
    }

    @Bean
    @Primary
    public WebClientProductReviewsClient mockWebClientProductReviewsClient(
            @Value("${selmag.service.feedback.product-reviews.url}") String baseUrl) {

        return new WebClientProductReviewsClient(
                WebClient.builder()
                        .baseUrl(baseUrl)
                        .build());
    }
}
