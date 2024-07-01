package ag.selm.customer.config;

import ag.selm.customer.client.WebClientFavouriteProductsClient;
import ag.selm.customer.client.WebClientProductReviewsClient;
import ag.selm.customer.client.WebClientProductsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientBeans {

    @Bean
    WebClient.Builder selmagServicesWebClientBuilder(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
            @Value("${selmag.service.registration-id:keycloak}") String registrationId) {

        ServerOAuth2AuthorizedClientExchangeFilterFunction filter =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                        clientRegistrationRepository, authorizedClientRepository);

        filter.setDefaultClientRegistrationId(registrationId);

        return WebClient.builder().filter(filter);
    }

    @Bean
    WebClientProductsClient webClientProductsClient(
            @Value("${selmag.service.catalogue.products.url:http://localhost:8081/catalogue-api/products}") String baseUrl,
            WebClient.Builder selmagServicesWebClientBuilder) {

        return new WebClientProductsClient(
                selmagServicesWebClientBuilder
                        .baseUrl(baseUrl)
                        .build());
    }

    @Bean
    WebClientProductReviewsClient webClientProductReviewsClient(
            @Value("${selmag.service.feedback.product-reviews.url:http://localhost:8084/feedback-api/product-reviews}") String baseUrl,
            WebClient.Builder selmagServicesWebClientBuilder) {

        return new WebClientProductReviewsClient(
                selmagServicesWebClientBuilder
                        .baseUrl(baseUrl)
                        .build());
    }

    @Bean
    WebClientFavouriteProductsClient webClientFavouriteProductsClient(
            @Value("${selmag.service.feedback.favourite-products.url:http://localhost:8084/feedback-api/favourite-products}") String baseUrl,
            WebClient.Builder selmagServicesWebClientBuilder) {

        return new WebClientFavouriteProductsClient(
                selmagServicesWebClientBuilder
                        .baseUrl(baseUrl)
                        .build());
    }
}
