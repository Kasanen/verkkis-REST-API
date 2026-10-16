package verkkis.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import verkkis.demo.dto.OrderHistoryEntry;
import verkkis.demo.dto.OrderSummary;
import verkkis.demo.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Koko tilaus riveineen, tuotteineen, asiakkaineen ja osoitteineen yhdellä kyselyllä
    @EntityGraph(attributePaths = {"customer", "shippingAddress", "items", "items.product"})
    Optional<Order> findWithDetailsById(Integer id);

    boolean existsByCustomer_Id(Integer customerId);

    boolean existsByShippingAddress_Id(Integer addressId);

    // Tilausten yhteenveto: rivimäärä ja loppusumma lasketaan orderitems-taulusta
    @Query("""
            SELECT new verkkis.demo.dto.OrderSummary(
                o.id, o.customer.id, o.orderDate, o.deliveryDate, o.status,
                COUNT(i), COALESCE(SUM(i.quantity), 0), COALESCE(SUM(i.unitPrice * i.quantity), 0))
            FROM Order o LEFT JOIN o.items i
            WHERE o.customer.id = :customerId
            GROUP BY o.id, o.customer.id, o.orderDate, o.deliveryDate, o.status
            ORDER BY o.orderDate DESC
            """)
    List<OrderSummary> findSummariesByCustomer(@Param("customerId") Integer customerId);

    @Query(value = """
            SELECT new verkkis.demo.dto.OrderSummary(
                o.id, o.customer.id, o.orderDate, o.deliveryDate, o.status,
                COUNT(i), COALESCE(SUM(i.quantity), 0), COALESCE(SUM(i.unitPrice * i.quantity), 0))
            FROM Order o LEFT JOIN o.items i
            WHERE :status IS NULL OR o.status = :status
            GROUP BY o.id, o.customer.id, o.orderDate, o.deliveryDate, o.status
            """,
            countQuery = "SELECT COUNT(o) FROM Order o WHERE :status IS NULL OR o.status = :status")
    Page<OrderSummary> findSummaries(@Param("status") String status, Pageable pageable);

    @Query("SELECT new verkkis.demo.dto.OrderHistoryEntry(o.id, o.status, o.orderDate) FROM Order o WHERE o.id = :id ORDER BY o.orderDate DESC")
    List<OrderHistoryEntry> findHistoryById(@Param("id") Integer id);
}
