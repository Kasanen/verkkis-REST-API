package verkkis.demo.dto;

public record CategoryResponse(Integer id, String name, String description, Long productCount, Long inStockCount) {
}
