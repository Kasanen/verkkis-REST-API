package verkkis.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import verkkis.demo.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);

    // Etuliitehaku (LIKE 'x%') hyödyntää indeksejä idx_customers_last_name ja idx_customers_email
    @Query("""
            SELECT c FROM Customer c
            WHERE :q IS NULL
               OR LOWER(c.lastName) LIKE LOWER(CONCAT(:q, '%'))
               OR c.email LIKE LOWER(CONCAT(:q, '%'))
            """)
    Page<Customer> search(@Param("q") String q, Pageable pageable);
}
