package verkkis.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import verkkis.demo.dto.CategoryResponse;
import verkkis.demo.model.ProductCategory;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {

    // Kategoriat tuotemäärineen (vain varastossa olevat lasketaan erikseen)
    @Query("""
            SELECT new verkkis.demo.dto.CategoryResponse(
                c.id, c.name, c.description, COUNT(p),
                COALESCE(SUM(CASE WHEN p.stockQuantity > 0 THEN 1 ELSE 0 END), 0))
            FROM ProductCategory c LEFT JOIN Product p ON p.category = c
            GROUP BY c.id, c.name, c.description
            ORDER BY c.name
            """)
    List<CategoryResponse> findAllWithProductCounts();

    boolean existsByNameIgnoreCase(String name);
}
