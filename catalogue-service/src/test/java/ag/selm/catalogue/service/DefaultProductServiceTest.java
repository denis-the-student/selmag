package ag.selm.catalogue.service;

import ag.selm.catalogue.entity.Product;
import ag.selm.catalogue.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultProductServiceTest {
    
    @Mock
    ProductRepository repository;
    
    @InjectMocks
    DefaultProductService service;

    @Test
    void findAllProducts_WithNoFilter_ReturnsProducts() {
        // given
        var product1 = new Product(1, "Product 1", "Details 1");
        var product2 = new Product(2, "Product 2", "Details 2");

        when(repository.findAll()).thenReturn(List.of(product1, product2));
        
        // when
        var result = service.findAllProducts(null);

        // then
        assertEquals(List.of(product1, product2) , result);

        verifyNoMoreInteractions(repository);
    }

    @Test
    void findAllProducts_WithFilter_ReturnsProducts() {
        // given
        var product1 = new Product(1, "Product 1", "Details 1");
        var product2 = new Product(2, "Product 2", "Details 2");

        when(repository.findAllByTitleLikeIgnoreCase(anyString()))
                .thenReturn(List.of(product1, product2));

        // when
        var result = service.findAllProducts("prod");

        // then
        assertEquals(List.of(product1, product2) , result);

        verifyNoMoreInteractions(repository);
    }

    @Test
    void findProductsByIds_WithNoFilter_ReturnsProducts() {
        // given
        var product1 = new Product(1, "Product 1", "Details 1");
        var product2 = new Product(2, "Product 2", "Details 2");

        when(repository.findAllById(List.of(1, 2)))
                .thenReturn(List.of(product1, product2));

        // when
        var result = service.findProductsByIds(List.of(1, 2), null);

        // then
        assertEquals(List.of(product1, product2) , result);

        verifyNoMoreInteractions(repository);
    }

    @Test
    void findProductsByIds_WithFilter_ReturnsProducts() {
        // given
        var product1 = new Product(1, "Product 1", "Details 1");
        var product2 = new Product(2, "Product 2", "Details 2");

        when(repository.findAllByIdInAndTitleLikeIgnoreCase(List.of(1, 2), "%prod%"))
                .thenReturn(List.of(product1, product2));

        // when
        var result = service.findProductsByIds(List.of(1, 2), "prod");

        // then
        assertEquals(List.of(product1, product2) , result);

        verifyNoMoreInteractions(repository);
    }

    @Test
    void createProduct_ReturnsCreatedProduct() {
        // given
        var title = "Product 1";
        var details = "Details 1";
        var product1 = new Product(1, title, details);

        when(repository.save(new Product(null, title, details)))
                .thenReturn(product1);

        // when
        var result = service.createProduct(title, details);

        // then
        assertEquals(product1, result);

        verify(repository).save(new Product(null, title, details));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findProduct_ReturnsNotEmptyOptional() {
        // given
        var product1 = new Product(1, "Product 1", "Details 1");
        when(repository.findById(anyInt())).thenReturn(Optional.of(product1));

        // when
        var result = service.findProduct(1);

        // then
        assertEquals(product1, result.orElseThrow());

        verifyNoMoreInteractions(repository);
    }

    @Test
    void findProduct_ProductDoesNotExist_ReturnsEmptyOptional() {
        // given
        when(repository.findById(anyInt())).thenReturn(Optional.empty());

        // when
        var result = service.findProduct(1);

        // then
        assertEquals(Optional.empty(), result);

        verifyNoMoreInteractions(repository);
    }

    @Test
    void updateProduct_ProductExists_UpdatesProduct() {
        // given
        var product = new Product(1, "Old title", "Old details");
        var newTitle = "Updated title";
        var newDetails = "Updated details";
        when(repository.findById(anyInt())).thenReturn(Optional.of(product));

        // when
        service.updateProduct(1, newTitle, newDetails);

        // then
        assertEquals(newTitle, product.getTitle());
        assertEquals(newDetails, product.getDetails());

        verify(repository).findById(anyInt());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void updateProduct_ProductDoesNotExist_UpdatesProduct() {
        // given
        var newTitle = "Updated title";
        var newDetails = "Updated details";
        when(repository.findById(anyInt())).thenReturn(Optional.empty());

        // when
        // then
        assertThrows(NoSuchElementException.class,
                () -> service.updateProduct(1, newTitle, newDetails));

        verify(repository).findById(anyInt());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteProduct() {
        // given
        // when
        service.deleteProduct(1);

        // then
        verify(repository).deleteById(anyInt());
        verifyNoMoreInteractions(repository);
    }
}