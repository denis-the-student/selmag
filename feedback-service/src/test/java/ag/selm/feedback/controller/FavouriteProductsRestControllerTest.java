package ag.selm.feedback.controller;

import ag.selm.feedback.controller.payload.FavouriteProductPayload;
import ag.selm.feedback.entity.FavouriteProduct;
import ag.selm.feedback.service.FavouriteProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavouriteProductsRestControllerTest {

    JwtAuthenticationToken token = new JwtAuthenticationToken(
            Jwt.withTokenValue("token")
                    .header("foo", "bar")
                    .claim("sub", "user-test-id")
                    .build());

    FavouriteProduct favProduct1 = new FavouriteProduct(
            UUID.fromString("67bc59b5-4db8-40a4-a42a-9fb097cbf988"), 1, "user-test-id");
    FavouriteProduct favProduct2 = new FavouriteProduct(
            UUID.fromString("12ed83c0-fd59-48e2-ad76-553020567aeb"), 2, "user-test-id");

    @Mock
    FavouriteProductService service;

    @InjectMocks
    FavouriteProductsRestController controller;

    @Test
    void findAllFavouriteProducts_ReturnsFavouriteProducts() {
        // given
        when(service.findFavouriteProducts(anyString())).thenReturn(
                Flux.fromIterable(List.of(favProduct1, favProduct2)));

        // when
        StepVerifier.create(controller.findAllFavouriteProducts(Mono.just(token)))
                // then
                .expectNext(favProduct1, favProduct2)
                .verifyComplete();

        verifyNoMoreInteractions(service);
    }

    @Test
    void findFavouriteProductByProductId_ReturnsFavouriteProduct() {
        // given
        when(service.findFavouriteProductByProduct(anyInt(), anyString()))
                .thenReturn(Mono.just(favProduct1));

        // when
        StepVerifier.create(controller.findFavouriteProductByProductId(1, Mono.just(token)))
                // then
                .expectNext(favProduct1)
                .verifyComplete();

        verifyNoMoreInteractions(service);
    }

    @Test
    void addProductToFavourites_ReturnsFavouriteProduct() {
        // given
        FavouriteProductPayload payload = new FavouriteProductPayload(favProduct1.productId());

        when(service.addProductToFavourites(anyInt(), anyString()))
                .thenReturn(Mono.just(favProduct1));

        // when
        StepVerifier.create(controller.addProductToFavourites(Mono.just(payload),
                        UriComponentsBuilder.fromUriString("http://localhost"), Mono.just(token)))
                // then
                .expectNext(ResponseEntity.created(
                        URI.create(String.format("http://localhost/feedback-api/favourite-products/%s", favProduct1.id())))
                        .body(favProduct1))
                .verifyComplete();

        verify(service).addProductToFavourites(anyInt(), anyString());
        verifyNoMoreInteractions(service);
    }

    @Test
    void removeProductFromFavourites_ReturnsNoContent() {
        // given
        when(service.removeProductFromFavourites(anyInt(), anyString()))
                .thenReturn(Mono.empty());

        // when
        StepVerifier.create(controller.removeProductFromFavourites(1, Mono.just(token)))
                .expectNext(ResponseEntity.noContent().build())
                .verifyComplete();

        // then
        verify(service).removeProductFromFavourites(anyInt(), anyString());
    }
}