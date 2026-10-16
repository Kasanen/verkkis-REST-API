package verkkis.demo.dto;

import java.math.BigDecimal;

public record MonthlySales(String month, long totalOrders, long totalItemsSold, BigDecimal totalRevenue) {
}
