package verkkis.demo.dto;

import java.math.BigDecimal;

public record TopProduct(Integer productId, String productName, String categoryName, long unitsSold,
        long orderCount, BigDecimal revenue) {
}
