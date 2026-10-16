package verkkis.demo.dto;

import java.util.List;

public record CustomerResponse(
        Integer id,
        String firstName,
        String lastName,
        String email,
        String phone,
        List<AddressResponse> addresses) {
}
