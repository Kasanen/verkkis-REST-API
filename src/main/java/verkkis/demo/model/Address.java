package verkkis.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Osoitteiden yhteinen yläluokka (periytyminen).
 *
 * Tietokannassa asiakas- ja toimittajaosoitteet ovat omissa tauluissaan
 * (customeraddresses, supplieraddresses), joissa kummassakin on kaikki osoitekentät.
 * Siksi yläluokka on @MappedSuperclass: kentät ja niiden sarakemäärittelyt periytyvät
 * aliluokille, mutta yläluokalle ei ole omaa taulua.
 */
@MappedSuperclass
public abstract class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(name = "street_address", nullable = false, length = 255)
    protected String streetAddress;

    @Column(name = "postal_code", length = 20)
    protected String postalCode;

    @Column(name = "city", nullable = false, length = 100)
    protected String city;

    @Column(name = "country", length = 100)
    protected String country;

    protected Address() {
    }

    protected Address(String streetAddress, String postalCode, String city, String country) {
        this.streetAddress = streetAddress;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
