package ag.selm.catalogue.controller;

import ag.selm.catalogue.controller.payload.NewProductPayload;
import ag.selm.catalogue.entity.Product;
import ag.selm.catalogue.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductsRestControllerTest {

    @Mock
    ProductService service;

    @InjectMocks
    ProductsRestController controller;

    @Test
    void findProducts_WithoutIds_ReturnsProductsList() {
        // given
        var product1 = new Product(1, "Product 1", "Details 1");
        var product2 = new Product(2, "Product 2", "Details 2");
        var products = List.of(product1, product2);

        when(this.service.findAllProducts(anyString())).thenReturn(products);

        // when
        var result = this.controller.findProducts(null, "testFilter");

        // then
        assertEquals(products, result);
    }

    @Test
    void findProducts_WithIds_ReturnsProductsList() {
        // given
        var product1 = new Product(1, "Product 1", "Details 1");
        var product2 = new Product(2, "Product 2", "Details 2");
        var products = List.of(product1, product2);
        var ids = List.of(1, 2);

        when(this.service.findProductsByIds(ids, null)).thenReturn(products);

        // when
        var result = this.controller.findProducts(ids, null);

        // then
        assertEquals(products, result);
    }

    @Test
    void createProduct_ReturnsResponseEntity() throws BindException {
        // given
        var payload = new NewProductPayload("New product", "Details");
        var createdProduct = new Product(1, payload.title(), payload.details());
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        var uriComponentsBuilder = UriComponentsBuilder.newInstance();

        when(this.service.createProduct(payload.title(), payload.details()))
                .thenReturn(createdProduct);

        // when
        var result = this.controller
                .createProduct(payload, bindingResult, uriComponentsBuilder);

        // then
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(URI.create("/catalogue-api/products/1"), result.getHeaders().getLocation());
        assertEquals(createdProduct, result.getBody());
        verifyNoMoreInteractions(this.service);
    }

    @Test
    void createProduct_RequestIsInvalidAndBindResultIsBindException_ReturnsBadRequest() {
        // given
        var payload = new NewProductPayload("   ", null);
        var uriComponentsBuilder = UriComponentsBuilder.newInstance();
        var bindException = new BindException(payload, "payload");
        var fieldError = new FieldError("payload", "title", "error");

        bindException.addError(fieldError);

        // when
        // then
        var exception = assertThrows(BindException.class,
                () -> this.controller.createProduct(payload, bindException, uriComponentsBuilder));

        assertEquals(List.of(fieldError), exception.getAllErrors());
        verifyNoInteractions(this.service);
    }

    @Test
    void createProduct_RequestIsInvalid_ReturnsBadRequestException() {
        // given
        var payload = new NewProductPayload("  ", "Details");
        var uriComponentsBuilder = UriComponentsBuilder.newInstance();
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        var fieldError = new FieldError("payload", "title", "error");

        bindingResult.addError(fieldError);

        // when
        // then
        var exception = assertThrows(BindException.class, () -> this.controller
                .createProduct(payload, bindingResult, uriComponentsBuilder));

        assertEquals(List.of(fieldError), exception.getAllErrors());
        verifyNoInteractions(this.service);
    }
}
