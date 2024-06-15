package ag.selm.manager.config;

import ag.selm.manager.client.RestClientProductsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

import static org.mockito.Mockito.mock;

@Configuration
public class TestingBeans {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return mock(ClientRegistrationRepository.class);
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return mock(OAuth2AuthorizedClientRepository.class);
    }

    @Bean
    @Primary
    public RestClientProductsClient testProductsClient(
            @Value("${selmag.service.catalogue.url:http://localhost:54321}") String baseUrl) {
        var client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        var requestFactory = new JdkClientHttpRequestFactory(client);

        return new RestClientProductsClient(RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .build());
    }
}
