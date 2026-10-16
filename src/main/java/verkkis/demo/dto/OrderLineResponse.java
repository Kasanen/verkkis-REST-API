package verkkis.demo.dto;

import java.math.BigDecimal;

public record OrderLineResponse(Integer productId, String productName, Integer quantity, BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
