package ag.selm.manager.controller;

import ag.selm.manager.client.ProductsClient;
import ag.selm.manager.client.exception.ClientBadRequestException;
import ag.selm.manager.controller.payload.NewProductPayload;
import ag.selm.manager.entity.Product;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты ProductsController")
class ProductsControllerTest {

    @Mock
    ProductsClient client;

    @Mock
    Model model;

    @Mock
    HttpServletResponse response;

    @InjectMocks
    ProductsController controller;

    @Test
    void getProductsListPage_ShouldReturnProductListPage() {
        // given
        List<Product> products = List.of(
                new Product(1, "Product 1", "Details 1"),
                new Product(2, "Product 2", "Details 2"));
        when(this.client.findAllProducts(anyString())).thenReturn(products);

        // when
        var viewName = this.controller.getProductsListPage(model, "testFilter");

        // then
        assertEquals("catalogue/products/list", viewName);
        verify(this.model).addAttribute("products", products);
        verify(this.model).addAttribute("filter", "testFilter");
    }

    @Test
    void getNewProductPage_ShouldReturnNewProductPage() {
        // given

        // when
        var viewName = this.controller.getNewProductPage();

        // then
        assertEquals("catalogue/products/new_product", viewName);
    }

    @Test
    @DisplayName("createProduct() создаст новый товар и перенаправит на страницу товара")
    void createProduct_ShouldRedirectToProductPage() {
        // given
        var payload = new NewProductPayload("New Product", "New Details");
        var createdProduct = new Product(1, payload.title(), payload.details());

        when(this.client.createProduct(eq(payload.title()), eq(payload.details())))
                .thenReturn(createdProduct);

        // when
        String viewName = this.controller.createProduct(payload, this.model, response);

        // then
        assertEquals("redirect:/catalogue/products/1", viewName);
        verify(this.client).createProduct(eq(payload.title()), eq(payload.details()));
        verifyNoMoreInteractions(this.client);
    }


    @Test
    @DisplayName("createProduct() вернёт страницу с ошибками, если запрос не валиден")
    void createProduct_RequestIsInvalid_ShouldReturnNewProductPageWithErrors() {
        // given
        var payload = new NewProductPayload("  ", null);
        var exception = new ClientBadRequestException(new Throwable(), List.of("Ошибка 1", "Ошибка 2"));

        when(this.client.createProduct(eq(payload.title()), eq(payload.details())))
                .thenThrow(exception);

        // when
        var viewName = this.controller.createProduct(payload, model, response);

        // then
        assertEquals("catalogue/products/new_product", viewName);
        verify(this.response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(this.client).createProduct(eq(payload.title()), eq(payload.details()));
        verifyNoMoreInteractions(this.client);
        verify(model).addAttribute("payload", payload);
        verify(model).addAttribute("errors", exception.getErrors());
    }

}