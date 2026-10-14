package verkkis.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import verkkis.demo.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public int increaseStockForSupplier(Integer supplierId) {
        return productRepository.increaseStockForSupplier(supplierId);
    }
}