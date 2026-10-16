package verkkis.demo.dto;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        Integer categoryId,
        Integer supplierId) {
}
