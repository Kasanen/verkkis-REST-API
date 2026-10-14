package verkkis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import verkkis.demo.DemoApplication;
import verkkis.demo.model.Product;
import verkkis.demo.model.Supplier;
import verkkis.demo.repository.ProductRepository;
import verkkis.demo.repository.SupplierRepository;

@SpringBootTest(classes = DemoApplication.class)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Test
    @Transactional
    void testIncreaseStockForSupplier() {
        Supplier supplier = supplierRepository.save(new Supplier("Nordic Audio", "sales@nordicaudio.fi"));
        Product product = new Product("Kaiutin", new java.math.BigDecimal("79.90"), 50);
        product.setSupplier(supplier);
        productRepository.save(product);

        int updatedRows = productRepository.increaseStockForSupplier(supplier.getId());

        assertEquals(1, updatedRows);
    }
}