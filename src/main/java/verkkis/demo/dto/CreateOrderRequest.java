package verkkis.demo.dto;

import java.util.List;

public record CreateOrderRequest(Integer customerId, Integer shippingAddressId, List<OrderLineRequest> items) {

    public record OrderLineRequest(Integer productId, Integer quantity) {
    }
}
