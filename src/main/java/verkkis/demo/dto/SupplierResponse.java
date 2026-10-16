package verkkis.demo.dto;

import java.util.List;

public record SupplierResponse(
        Integer id,
        String name,
        String contactName,
        String phone,
        String email,
        long productCount,
        List<AddressResponse> addresses) {
}
