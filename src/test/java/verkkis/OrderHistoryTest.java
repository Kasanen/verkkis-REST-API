package verkkis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import verkkis.demo.DemoApplication;
import verkkis.demo.dto.OrderHistoryEntry;
import verkkis.demo.model.Customer;
import verkkis.demo.model.Order;
import verkkis.demo.repository.CustomerRepository;
import verkkis.demo.repository.OrderRepository;

@SpringBootTest(classes = DemoApplication.class)
public class OrderHistoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @Transactional
    void orderHistoryContainsCurrentOrder() {
        Customer customer = customerRepository.save(new Customer("Test", "Customer", "history@example.com"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus("NEW");
        Order saved = orderRepository.saveAndFlush(order);

        OrderHistoryEntry entry = orderRepository.findHistoryById(saved.getId()).stream().findFirst().orElseThrow();

        assertThat(entry.id()).isEqualTo(saved.getId());
        assertThat(entry.status()).isEqualTo("NEW");
    }
}
