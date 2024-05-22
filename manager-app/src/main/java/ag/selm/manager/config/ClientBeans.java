package ag.selm.manager.config;

import ag.selm.manager.client.CatalogueRestClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientBeans {

    @Bean
    CatalogueRestClientImpl catalogueRestClient(
            @Value("${selmag.service.catalogue.url:http://localhost:8081}") String baseUrl,
            @Value("${selmag.service.catalogue.username:}") String username,
            @Value("${selmag.service.catalogue.password:}") String password) {
        return new CatalogueRestClientImpl(
                RestClient.builder()
                        .baseUrl(baseUrl)
                        .requestInterceptor(new BasicAuthenticationInterceptor(username, password))
                        .build());
    }
}
