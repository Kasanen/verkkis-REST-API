package verkkis.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummary(
        Integer id,
        Integer customerId,
        LocalDateTime orderDate,
        LocalDateTime deliveryDate,
        String status,
        Long lineCount,
        Long totalQuantity,
        BigDecimal total) {
}
