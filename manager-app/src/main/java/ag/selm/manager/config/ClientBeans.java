package ag.selm.manager.config;

import ag.selm.manager.client.CatalogueRestClientImpl;
import ag.selm.manager.security.OAuthClientHttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientBeans {

    @Bean
    CatalogueRestClientImpl catalogueRestClient(
            @Value("${selmag.service.catalogue.url:http://localhost:8081}") String baseUrl,
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository,
            @Value("${selmag.service.catalogue.registration-id:keycloak}") String registrationId) {

        return new CatalogueRestClientImpl(
                RestClient.builder()
                        .baseUrl(baseUrl)
                        .requestInterceptor(new OAuthClientHttpRequestInterceptor(
                                new DefaultOAuth2AuthorizedClientManager(
                                        clientRegistrationRepository,
                                        authorizedClientRepository),
                                registrationId))
                        .build());
    }
}
