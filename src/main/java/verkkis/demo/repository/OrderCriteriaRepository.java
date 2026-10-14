package verkkis.demo.repository;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import verkkis.demo.model.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class OrderCriteriaRepository {

    private final EntityManager entityManager;

    public OrderCriteriaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public int deleteOldCancelledOrders(int daysOld) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<Order> delete = cb.createCriteriaDelete(Order.class);
        Root<Order> root = delete.from(Order.class);

        LocalDateTime threshold = LocalDateTime.now().minusDays(daysOld);

        Predicate isCancelled = cb.equal(root.get("status"), "CANCELLED");
        Predicate isOld = cb.lessThan(root.get("orderDate"), threshold);

        delete.where(cb.and(isCancelled, isOld));

        return entityManager.createQuery(delete).executeUpdate();
    }
}