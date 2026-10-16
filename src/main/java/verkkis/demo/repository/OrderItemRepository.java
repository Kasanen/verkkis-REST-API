package verkkis.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import verkkis.demo.model.OrderItem;
import verkkis.demo.model.OrderItemId;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {

    boolean existsByProduct_Id(Integer productId);
}
