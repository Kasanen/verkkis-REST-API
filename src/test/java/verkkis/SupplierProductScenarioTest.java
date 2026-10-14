package verkkis;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import verkkis.demo.DemoApplication;
import verkkis.demo.model.Product;
import verkkis.demo.model.ProductCategory;
import verkkis.demo.model.Supplier;
import verkkis.demo.repository.ProductCategoryRepository;
import verkkis.demo.repository.ProductRepository;
import verkkis.demo.repository.SupplierRepository;

@SpringBootTest(classes = DemoApplication.class)
class SupplierProductsScenarioTest {

    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ProductCategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    @Test
    @Transactional
    void fetchAllProductsForOneSupplier() {
        Supplier supplier = supplierRepository.save(new Supplier("Nordic Audio", "sales@nordicaudio.fi"));
        ProductCategory category = categoryRepository.save(new ProductCategory("Äänentoisto"));

        Product kaiutin = new Product("Kaiutin", new BigDecimal("79.90"), 50);
        kaiutin.setSupplier(supplier);
        kaiutin.addCategory(category);
        productRepository.save(kaiutin);

        List<Product> productsForSupplier = productRepository.findBySupplierId(supplier.getId());

        assertThat(productsForSupplier).isNotEmpty();
        assertThat(productsForSupplier)
                .allMatch(p -> p.getSupplier().getId().equals(supplier.getId()));
    }
}