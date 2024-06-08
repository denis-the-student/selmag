package ag.selm.catalogue.repository;

import ag.selm.catalogue.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Iterable<Product> findAllByTitleLikeIgnoreCase(String filter);

    Iterable<Product> findProductsByIdIn (List<Integer> ids);

    Iterable<Product> findProductsByIdInAndTitleLikeIgnoreCase (List<Integer> ids, String filter);
}
