package verkkis.demo.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;
import verkkis.demo.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    List<Product> findBySupplierId(Integer supplierId);

    long countBySupplier_Id(Integer supplierId);

    long countByCategory_Id(Integer categoryId);

    // Haetaan kategoria ja toimittaja samalla kyselyllä (ei N+1-ongelmaa)
    @Override
    @EntityGraph(attributePaths = {"category", "supplier"})
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "supplier"})
    Optional<Product> findWithDetailsById(Integer id);

    // SELECT ... FOR UPDATE: lukitsee tuoterivit tilauksen ajaksi, jotta kaksi
    // samanaikaista tilausta eivät voi myydä samaa varastoa kahteen kertaan
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id")
    List<Product> findAllByIdForUpdate(@Param("ids") Collection<Integer> ids);

    @Modifying
    @Transactional
    @Query("""
                UPDATE Product p
                SET p.stockQuantity = p.stockQuantity + 50
                WHERE p.supplier.id = :supplierId
            """)
    int increaseStockForSupplier(@Param("supplierId") Integer supplierId);

    // Atominen saldon palautus (esim. tilauksen peruutus): UPDATE ... SET stock = stock + :amount
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :amount WHERE p.id = :id")
    int addStock(@Param("id") Integer id, @Param("amount") int amount);

    // Natiivi poisto: Hibernate siivoaisi muuten myös N:M-liitostaulun (product_category_mapping),
    // jota ei ole alkuperäisessä skeemassa. Jos liitostaulu luodaan, sen ON DELETE CASCADE hoitaa rivit.
    @Modifying
    @Query(value = "DELETE FROM products WHERE id = :id", nativeQuery = true)
    int deleteProductById(@Param("id") Integer id);
}
