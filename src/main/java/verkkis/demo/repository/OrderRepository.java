package verkkis.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import verkkis.demo.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
}