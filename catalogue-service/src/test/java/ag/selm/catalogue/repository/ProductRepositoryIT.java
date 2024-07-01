package ag.selm.catalogue.repository;

import ag.selm.catalogue.entity.Product;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Sql(scripts = "/sql/products.sql",
    config = @SqlConfig(encoding = "UTF-8"))
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryIT {

    @Autowired
    ProductRepository repository;

    @Test
    void findAllByTitleLikeIgnoreCase_ReturnsFilteredProductsList() {
        // given
        var filter = "%шоколадка%";

        // when
        var products = this.repository.findAllByTitleLikeIgnoreCase(filter);
        products.forEach(System.out::println); // Выводим продукты для отладки

        // then
        assertEquals(
            List.of(new Product(2, "Шоколадка", "Очень вкусная шоколадка")),
            products);
    }
}