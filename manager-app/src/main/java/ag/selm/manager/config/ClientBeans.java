package ag.selm.manager.config;

import ag.selm.manager.client.ProductsRestClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientBeans {

    @Bean
    ProductsRestClientImpl productsRestClient(
            @Value("${selmag.service.catalogue.url:http://localhost:8081}") String catalogueBaseUrl) {
        return new ProductsRestClientImpl(
                RestClient.builder()
                        .baseUrl(catalogueBaseUrl)
                        .build());
    }
}
