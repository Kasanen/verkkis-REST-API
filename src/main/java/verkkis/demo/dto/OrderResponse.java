package verkkis.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Integer id,
        CustomerRef customer,
        LocalDateTime orderDate,
        LocalDateTime deliveryDate,
        String status,
        AddressResponse shippingAddress,
        List<OrderLineResponse> items,
        BigDecimal total) {
}
