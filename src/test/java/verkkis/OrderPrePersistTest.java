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
import verkkis.demo.repository.CustomerRepository;
import verkkis.demo.repository.OrderRepository;

@SpringBootTest(classes = DemoApplication.class)
public class OrderPrePersistTest {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void defaultsAreSetOnPersist() {
        Customer customer = customerRepository.save(new Customer("Testi Asiakas", "testi@example.com"));

        Order order = new Order();
        order.setCustomer(customer);
        orderRepository.save(order);

        entityManager.flush();
        entityManager.clear();

        Order fetched = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(fetched.getStatus()).isEqualTo("NEW");
        assertThat(fetched.getOrderDate()).isNotNull();
        assertThat(fetched.getOrderDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
