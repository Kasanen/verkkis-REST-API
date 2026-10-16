package verkkis.demo.dto;

import java.math.BigDecimal;

import verkkis.demo.model.Product;

public record ProductResponse(
        Integer id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        boolean inStock,
        CategoryRef category,
        SupplierRef supplier) {

    public static ProductResponse from(Product p) {
        CategoryRef category = p.getCategory() == null ? null
                : new CategoryRef(p.getCategory().getId(), p.getCategory().getName());
        SupplierRef supplier = p.getSupplier() == null ? null
                : new SupplierRef(p.getSupplier().getId(), p.getSupplier().getName());
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(),
                p.getStockQuantity(), p.getStockQuantity() > 0, category, supplier);
    }
}
