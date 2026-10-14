package verkkis.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "supplier_addresses")
@PrimaryKeyJoinColumn(name = "id")
public class SupplierAddress extends Address {

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    public SupplierAddress() {
        super();
    }

    public SupplierAddress(Supplier supplier, String street, String city, String postalCode) {
        super(street, city, postalCode);
        this.supplier = supplier;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }
}