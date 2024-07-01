package ag.selm.catalogue.controller;

import ag.selm.catalogue.controller.payload.UpdateProductPayload;
import ag.selm.catalogue.entity.Product;
import ag.selm.catalogue.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRestControllerTest {

    @Mock
    ProductService service;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    ProductRestController controller;

    @Test
    void loadProduct_ReturnsProduct() {
        // given
        var product = new Product(1, "Product 1", "Details 1");
        when(this.service.findProduct(anyInt())).thenReturn(Optional.of(product));

        // when
        var result = this.controller.loadProduct(1);

        // then
        assertEquals(product, result);
    }

    @Test
    void loadProduct_ProductDoesNotExist_ThrowsNoSuchElementException() {
        // given
        when(this.service.findProduct(anyInt())).thenReturn(Optional.empty());

        // when
        // then
        var exception = assertThrows(NoSuchElementException.class,
                () -> this.controller.loadProduct(1));

        assertEquals("catalogue.errors.product.not_found", exception.getMessage());
    }

    @Test
    void findProduct_ReturnsProduct() {
        // given
        var product = new Product(1, "Product 1", "Details 1");

        // when
        var result = this.controller.findProduct(product);

        // then
        assertEquals(product, result);
    }

    @Test
    void updateProduct_ReturnsResponseEntityWithNoContent() throws BindException {
        // given
        var payload = new UpdateProductPayload("Updated title", "Updated details");
        var bindingResult = new MapBindingResult(Map.of(), "payload");

        // when
        var result = this.controller.updateProduct(1, payload, bindingResult);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.service).updateProduct(eq(1), eq(payload.title()), eq(payload.details()));
        verifyNoMoreInteractions(this.service);
    }

    @Test
    void updateProduct_RequestIsInvalidAndBindingResultIsBindException_ReturnsBadRequest() {
        // given
        var payload = new UpdateProductPayload("   ", null);
        var bindingResult = new BindException(Map.of(), "payload");
        var fieldError = new FieldError("payload", "title", "error");

        bindingResult.addError(fieldError);

        // when
        // then
        var exception = assertThrows(BindException.class,
                () -> this.controller.updateProduct(1, payload, bindingResult));

        assertEquals(List.of(fieldError), exception.getAllErrors());
        verifyNoInteractions(this.service);
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsBadRequest() {
        // given
        var payload = new UpdateProductPayload("   ", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        var fieldError = new FieldError("payload", "title", "error");

        bindingResult.addError(fieldError);

        // when
        // then
        var exception = assertThrows(BindException.class,
                () -> this.controller.updateProduct(1, payload, bindingResult));

        assertEquals(List.of(fieldError), exception.getAllErrors());
        verifyNoInteractions(this.service);
    }

    @Test
    void deleteProduct_ReturnsResponseEntityWithNoContent() {
        // given

        // when
        var result = this.controller.deleteProduct(1);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.service).deleteProduct(1);
        verifyNoMoreInteractions(this.service);
    }

    @Test
    void handleNoSuchElementException_ReturnsResponseEntityWithProblemDetail() {
        // given
        var exception = new NoSuchElementException("error");
        var locale = Locale.ENGLISH;
        when(this.messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenReturn("Product not found");

        // when
        var result = this.controller.handleNoSuchElementException(exception, locale);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertInstanceOf(ProblemDetail.class, result.getBody());
        assertEquals("Product not found", result.getBody().getDetail());

        verifyNoInteractions(this.service);
    }
}