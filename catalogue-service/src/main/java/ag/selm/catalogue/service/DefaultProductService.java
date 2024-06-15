package ag.selm.catalogue.service;

import ag.selm.catalogue.entity.Product;
import ag.selm.catalogue.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultProductService implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Iterable<Product> findAllProducts(String filter) {
        if (filter != null && !filter.isBlank()) {
            return productRepository.findAllByTitleLikeIgnoreCase("%" + filter + "%");
        } else {
            return productRepository.findAll();
        }
    }

    @Override
    public Iterable<Product> findProductsByIds(List<Integer> ids, String filter) {
        if (filter != null && !filter.isBlank()) {
            return productRepository.findAllByIdInAndTitleLikeIgnoreCase(ids, "%" + filter + "%");
        } else {
            return productRepository.findAllById(ids);
        }
    }

    @Override
    @Transactional
    public Product createProduct(String title, String details) {
        return productRepository.save(new Product(null, title, details));
    }

    @Override
    public Optional<Product> findProduct(int productId) {
        return productRepository.findById(productId);
    }

    @Override
    @Transactional
    public void updateProduct(Integer id, String title, String details) {
        productRepository.findById(id)
                .ifPresentOrElse(product -> {
                    product.setTitle(title);
                    product.setDetails(details);
                }, () -> {
                    throw new NoSuchElementException();
                });
    }

    @Override
    @Transactional
    public void deleteProduct(Integer productId) {
        productRepository.deleteById(productId);
    }
}
