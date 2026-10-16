package verkkis;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import verkkis.demo.DemoApplication;
import verkkis.demo.model.Product;
import verkkis.demo.model.ProductCategory;
import verkkis.demo.repository.ProductCategoryRepository;
import verkkis.demo.repository.ProductRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest(classes = DemoApplication.class)
class ManyToManyTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductCategoryRepository categoryRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void productCanHaveMultipleCategories() {
        ProductCategory audio = categoryRepository.save(new ProductCategory("Äänentoisto"));
        ProductCategory outdoor = categoryRepository.save(new ProductCategory("Ulkoiluvarusteet"));

        Product product = new Product("Bluetooth-kaiutin", new BigDecimal("79.90"), 50);
        product.addCategory(audio);
        product.addCategory(outdoor);
        productRepository.save(product);

        entityManager.flush();
        entityManager.clear();

        Product fetched = productRepository.findById(product.getId()).orElseThrow();
        assertThat(fetched.getCategories()).hasSize(2);

        ProductCategory fetchedAudio = categoryRepository.findById(audio.getId()).orElseThrow();
        assertThat(fetchedAudio.getProducts()).hasSize(1);
    }
}
