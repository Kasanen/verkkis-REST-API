package verkkis.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_addresses")
@PrimaryKeyJoinColumn(name = "id")   // liittää tämän taulun addresses-taulun id:hen
public class CustomerAddress extends Address {

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public CustomerAddress() {
        super();
    }

    public CustomerAddress(Customer customer, String street, String city, String postalCode) {
        super(street, city, postalCode);
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}