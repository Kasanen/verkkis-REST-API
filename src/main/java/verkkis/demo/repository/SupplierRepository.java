package verkkis.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import verkkis.demo.dto.SupplierSummary;
import verkkis.demo.model.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    // Yhdistää suppliers- ja products-taulut: toimittajat tuotemäärineen
    @Query("""
            SELECT new verkkis.demo.dto.SupplierSummary(
                s.id, s.name, s.contactName, s.phone, s.email, COUNT(p))
            FROM Supplier s LEFT JOIN Product p ON p.supplier = s
            GROUP BY s.id, s.name, s.contactName, s.phone, s.email
            ORDER BY s.name
            """)
    List<SupplierSummary> findAllWithProductCount();
}
