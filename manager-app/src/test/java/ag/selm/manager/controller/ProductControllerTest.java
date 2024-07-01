package ag.selm.manager.controller;

import ag.selm.manager.client.ProductsClient;
import ag.selm.manager.client.exception.ClientBadRequestException;
import ag.selm.manager.controller.payload.UpdateProductPayload;
import ag.selm.manager.entity.Product;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты ProductController")
class ProductControllerTest {

    @Mock
    private ProductsClient client;

    @Mock
    private MessageSource messageSource;

    @Mock
    private Model model;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private ProductController controller;

    @Test
    void loadProduct_ReturnsProduct() {
        // given
        var product = new Product(1, "Product №1", "Details of product №1");
        when(this.client.findProduct(1)).thenReturn(Optional.of(product));

        // when
        var result = this.controller.loadProduct(1);

        // then
        assertEquals(product, result);
        verifyNoMoreInteractions(this.client);
    }


    @Test
    void loadProduct_ProductDoesNotExist_ThrowsNoSuchElementException() {
        // given
        when(this.client.findProduct(anyInt())).thenReturn(Optional.empty());

        // when
        try {
            this.controller.loadProduct(1);
        // then
        } catch (NoSuchElementException exception) {
            assertEquals("catalogue.errors.product.not_found", exception.getMessage());
        }

        verifyNoMoreInteractions(this.client);
    }

    @Test
    void getProductPage_ReturnsProductPage() {
        // given

        // when
        var result = this.controller.getProductPage();

        // then
        assertEquals("catalogue/products/product", result);
    }

    @Test
    void getProductEditPage_ReturnsEditPage() {
        // given

        // when
        var result = this.controller.getProductEditPage();

        // then
        assertEquals("catalogue/products/edit", result);
    }

    @Test
    void updateProduct_RedirectsToProductPage() {
        // given
        var payload = new UpdateProductPayload("title", "details");

        // when
        var result = this.controller.updateProduct(1, payload, model, response);

        // then
        assertEquals("redirect:/catalogue/products/1", result);
        verify(this.client).updateProduct(1, payload.title(), payload.details());
        verifyNoMoreInteractions(this.client);
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsEditPage() {
        // given
        var payload = new UpdateProductPayload("  ", "details");
        var exception = new ClientBadRequestException(new Throwable(), List.of("error 1", "error 2"));
        doThrow(exception).when(this.client).updateProduct(anyInt(), anyString(), anyString());

        // when
        var result = this.controller.updateProduct(1, payload, this.model, this.response);

        // then
        assertEquals("catalogue/products/edit", result);
        verify(this.response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(this.model).addAttribute("payload", payload);
        verify(this.model).addAttribute("errors", exception.getErrors());
        verify(this.client).updateProduct(eq(1), eq(payload.title()), eq(payload.details()));
        verifyNoMoreInteractions(this.client);

    }

    @Test
    void deleteProduct_SuccessfulDeletion_RedirectsToList() {
        // given

        // when
        var result = this.controller.deleteProduct(1);

        // then
        assertEquals("redirect:/catalogue/products/list", result);
        verify(this.client).deleteProduct(1);
        verifyNoMoreInteractions(this.client);
    }

    @Test
    void handleNoSuchElementException_ReturnsErrorPage() {
        // given
        var exception = new NoSuchElementException("error");
        var locale = Locale.ENGLISH;
        when(this.messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenReturn("Product not found");

        // when
        var result = this.controller
                .handleNoSuchElementException(exception, this.model, this.response, locale);

        assertEquals("errors/404", result);
        verify(this.response).setStatus(HttpStatus.NOT_FOUND.value());
        verify(model).addAttribute("error", "Product not found");
        verifyNoMoreInteractions(this.messageSource);
        verifyNoInteractions(this.client);
    }
}
