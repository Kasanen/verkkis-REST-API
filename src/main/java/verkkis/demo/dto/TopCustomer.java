package verkkis.demo.dto;

import java.math.BigDecimal;

public record TopCustomer(Integer customerId, String name, String email, long orderCount, BigDecimal totalSpent) {
}
