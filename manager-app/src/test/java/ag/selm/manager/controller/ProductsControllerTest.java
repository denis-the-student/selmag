package ag.selm.manager.controller;

import ag.selm.manager.client.BadRequestException;
import ag.selm.manager.client.ProductsClient;
import ag.selm.manager.controller.payload.NewProductPayload;
import ag.selm.manager.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты ProductsController")
class ProductsControllerTest {

    @Mock
    ProductsClient restClient;

    @InjectMocks
    ProductsController controller;

    @Test
    @DisplayName("createProduct() создаст новый товар и перенаправит на страницу товара")
    void createProduct_RequestIsValid_ReturnsDirectionToProductPage() {
        // given
        var payload = new NewProductPayload("Новый товар", "Описание нового товара");
        var model = new ConcurrentModel();

        doReturn(new Product(1, "Новый товар", "Описание нового товара"))
                .when(this.restClient)
                .createProduct("Новый товар", "Описание нового товара");

        // when
        var result = this.controller.createProduct(payload, model);

        // then
        assertEquals("redirect:/catalogue/products/1", result);

        verify(this.restClient).createProduct("Новый товар", "Описание нового товара");
        verifyNoMoreInteractions(this.restClient);
    }

    @Test
    @DisplayName("createProduct() вернёт страницу с ошибками, если запрос не валиден")
    void createProduct_RequestIsInvalid_ReturnsProductFormWithErrors() {
        // given
        var payload = new NewProductPayload("  ", null);
        var model = new ConcurrentModel();

        doThrow(new BadRequestException(List.of("Ошибка 1", "Ошибка 2")))
                .when(this.restClient)
                .createProduct("  ", null);

        // when
        var result = this.controller.createProduct(payload, model);

        // then
        assertEquals("catalogue/products/new_product", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Ошибка 1", "Ошибка 2"), model.getAttribute("errors"));

        verify(this.restClient).createProduct("  ", null);
        verifyNoMoreInteractions(this.restClient);
    }
}