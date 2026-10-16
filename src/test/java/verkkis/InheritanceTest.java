package verkkis;

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
import verkkis.demo.repository.CustomerAddressRepository;
import verkkis.demo.repository.CustomerRepository;
import verkkis.demo.repository.SupplierAddressRepository;
import verkkis.demo.repository.SupplierRepository;

/**
 * Address on @MappedSuperclass: asiakas- ja toimittajaosoite perivät samat kentät,
 * mutta tallentuvat omiin tauluihinsa (customeraddresses, supplieraddresses).
 */
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
    private EntityManager entityManager;

    @Test
    @Transactional
    void subclassesInheritAddressFieldsAndUseOwnTables() {
        Customer customer = customerRepository.save(new Customer("Matti", "Meikäläinen", "matti@example.com"));
        Supplier supplier = supplierRepository.save(new Supplier("Nordic Audio", "sales@nordicaudio.fi"));

        CustomerAddress ca = customerAddressRepository.save(
                new CustomerAddress(customer, "Testikatu 1", "00100", "Helsinki", "Finland"));
        SupplierAddress sa = supplierAddressRepository.save(
                new SupplierAddress(supplier, "Teollisuuskatu 5", "02100", "Espoo", "Finland"));

        entityManager.flush();
        entityManager.clear();

        CustomerAddress fetchedCustomerAddress = customerAddressRepository.findById(ca.getId()).orElseThrow();
        SupplierAddress fetchedSupplierAddress = supplierAddressRepository.findById(sa.getId()).orElseThrow();

        assertThat(fetchedCustomerAddress).isInstanceOf(Address.class);
        assertThat(fetchedSupplierAddress).isInstanceOf(Address.class);
        assertThat(fetchedCustomerAddress.getCity()).isEqualTo("Helsinki");
        assertThat(fetchedSupplierAddress.getStreetAddress()).isEqualTo("Teollisuuskatu 5");
        assertThat(fetchedCustomerAddress.getCustomer().getId()).isEqualTo(customer.getId());
        assertThat(fetchedSupplierAddress.getSupplier().getId()).isEqualTo(supplier.getId());

        // Kumpikin aliluokka on omassa taulussaan
        Number customerRows = (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM customeraddresses").getSingleResult();
        Number supplierRows = (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM supplieraddresses").getSingleResult();
        assertThat(customerRows.intValue()).isEqualTo(1);
        assertThat(supplierRows.intValue()).isEqualTo(1);
    }
}
