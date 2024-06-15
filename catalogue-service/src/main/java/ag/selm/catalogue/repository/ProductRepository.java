package ag.selm.catalogue.repository;

import ag.selm.catalogue.entity.Product;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductRepository extends CrudRepository<Product, Integer> {

    Iterable<Product> findAllByTitleLikeIgnoreCase(String filter);

    Iterable<Product> findAllByIdInAndTitleLikeIgnoreCase(List<Integer> ids, String filter);
}
