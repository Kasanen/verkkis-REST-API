package verkkis;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import verkkis.demo.DemoApplication;
import verkkis.demo.model.Address;
import verkkis.demo.model.Customer;
import verkkis.demo.model.CustomerAddress;
import verkkis.demo.model.Supplier;
import verkkis.demo.model.SupplierAddress;
import verkkis.demo.repository.AddressRepository;
import verkkis.demo.repository.CustomerAddressRepository;
import verkkis.demo.repository.CustomerRepository;
import verkkis.demo.repository.SupplierAddressRepository;
import verkkis.demo.repository.SupplierRepository;

@SpringBootTest(classes = DemoApplication.class)
class InheritanceTest {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerAddressRepository customerAddressRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private SupplierAddressRepository supplierAddressRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void polymorphicQueryReturnsBothSubtypes() {
        Customer customer = customerRepository.save(new Customer("Matti Meikäläinen", "matti@example.com"));
        Supplier supplier = supplierRepository.save(new Supplier("Nordic Audio", "sales@nordicaudio.fi"));

        customerAddressRepository.save(new CustomerAddress(customer, "Testikatu 1", "Helsinki", "00100"));
        supplierAddressRepository.save(new SupplierAddress(supplier, "Teollisuuskatu 5", "Espoo", "02100"));

        entityManager.flush();
        entityManager.clear();

        List<Address> allAddresses = addressRepository.findAll();

        assertThat(allAddresses).hasSize(2);
        assertThat(allAddresses).anyMatch(a -> a instanceof CustomerAddress);
        assertThat(allAddresses).anyMatch(a -> a instanceof SupplierAddress);
    }
}