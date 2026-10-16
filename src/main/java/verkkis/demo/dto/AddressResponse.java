package verkkis.demo.dto;

import verkkis.demo.model.Address;

public record AddressResponse(Integer id, String streetAddress, String postalCode, String city, String country) {

    public static AddressResponse from(Address a) {
        if (a == null) {
            return null;
        }
        return new AddressResponse(a.getId(), a.getStreetAddress(), a.getPostalCode(), a.getCity(), a.getCountry());
    }
}
