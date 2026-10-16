package verkkis.demo.dto;

import java.math.BigDecimal;

public record LowStockProduct(Integer productId, String productName, BigDecimal price, int stockQuantity,
        String categoryName, String supplierName, String supplierEmail) {
}
