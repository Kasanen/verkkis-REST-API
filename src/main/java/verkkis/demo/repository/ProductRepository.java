package verkkis.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import verkkis.demo.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findBySupplierId(Integer supplierId);

    @Modifying
    @Transactional
    @Query("""
                UPDATE Product p
                SET p.stockQuantity = p.stockQuantity + 50
                WHERE p.supplier.id = :supplierId
            """)
    int increaseStockForSupplier(@Param("supplierId") Integer supplierId);
}