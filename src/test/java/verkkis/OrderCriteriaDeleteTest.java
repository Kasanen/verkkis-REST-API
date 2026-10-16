package verkkis;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import verkkis.demo.DemoApplication;
import verkkis.demo.model.Customer;
import verkkis.demo.model.Order;
import verkkis.demo.model.OrderItem;
import verkkis.demo.model.Product;
import verkkis.demo.repository.OrderCriteriaRepository;
import verkkis.demo.repository.OrderRepository;
import verkkis.demo.repository.ProductRepository;

@SpringBootTest(classes = DemoApplication.class)
class OrderCriteriaDeleteTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderCriteriaRepository orderCriteriaRepository;
    @Autowired
    private verkkis.demo.repository.CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void deletesOnlyOldCancelledOrders() {
        var customer = customerRepository.save(new Customer("Testi", "Asiakas", "testi@example.com"));

        Order oldCancelled = new Order(customer, null, LocalDateTime.now().minusDays(100), "CANCELLED");
        Order recentCancelled = new Order(customer, null, LocalDateTime.now().minusDays(10), "CANCELLED");
        Order oldDelivered = new Order(customer, null, LocalDateTime.now().minusDays(100), "DELIVERED");

        // Vanhalla peruutetulla tilauksella on myös tilausrivi: rivi poistetaan ennen tilausta
        Product product = productRepository.save(new Product("Kaiutin", new BigDecimal("79.90"), 10));
        oldCancelled.addItem(new OrderItem(oldCancelled, product, 2, product.getPrice()));

        orderRepository.save(oldCancelled);
        orderRepository.save(recentCancelled);
        orderRepository.save(oldDelivered);

        entityManager.flush();

        int deletedCount = orderCriteriaRepository.deleteOldCancelledOrders(90);

        entityManager.clear(); 

        assertThat(deletedCount).isEqualTo(1);
        assertThat(orderRepository.findById(oldCancelled.getId())).isEmpty();
        assertThat(orderRepository.findById(recentCancelled.getId())).isPresent();
        assertThat(orderRepository.findById(oldDelivered.getId())).isPresent();
    }
}