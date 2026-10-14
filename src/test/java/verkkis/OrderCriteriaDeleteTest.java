package verkkis;

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
import verkkis.demo.repository.OrderCriteriaRepository;
import verkkis.demo.repository.OrderRepository;

@SpringBootTest(classes = DemoApplication.class)
class OrderCriteriaDeleteTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderCriteriaRepository orderCriteriaRepository;
    @Autowired
    private verkkis.demo.repository.CustomerRepository customerRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void deletesOnlyOldCancelledOrders() {
        var customer = customerRepository.save(new Customer("Testi Asiakas", "testi@example.com"));

        Order oldCancelled = new Order(customer, null, LocalDateTime.now().minusDays(100), "CANCELLED");
        Order recentCancelled = new Order(customer, null, LocalDateTime.now().minusDays(10), "CANCELLED");
        Order oldDelivered = new Order(customer, null, LocalDateTime.now().minusDays(100), "DELIVERED");

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