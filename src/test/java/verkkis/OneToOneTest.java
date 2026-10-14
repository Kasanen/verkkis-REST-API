package verkkis;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import verkkis.demo.DemoApplication;
import verkkis.demo.model.Contact;
import verkkis.demo.model.Customer;
import verkkis.demo.repository.ContactRepository;
import verkkis.demo.repository.CustomerRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest(classes = DemoApplication.class)
class OneToOneTest {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private ContactRepository contactRepository;
    @Autowired private EntityManager entityManager;

    @Test
    @Transactional
    void customerHasExactlyOnePrimaryContact() {
        Customer customer = customerRepository.save(new Customer("Matti Meikäläinen", "matti@example.com"));

        Contact contact = new Contact(customer, "+358401234567");
        contactRepository.save(contact);

        customer.setPrimaryContact(contact);
        customerRepository.save(customer);

        entityManager.flush();
        entityManager.clear();

        Customer fetched = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(fetched.getPrimaryContact()).isNotNull();
        assertThat(fetched.getPrimaryContact().getPhone()).isEqualTo("+358401234567");
    }
}