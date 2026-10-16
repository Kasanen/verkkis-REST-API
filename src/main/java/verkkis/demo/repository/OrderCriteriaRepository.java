package verkkis.demo.repository;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import verkkis.demo.model.Order;
import verkkis.demo.model.OrderItem;

@Repository
public class OrderCriteriaRepository {

    private final EntityManager entityManager;

    public OrderCriteriaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Poistaa yli daysOld päivää vanhat peruutetut tilaukset.
     * orderitems viittaa orders-tauluun ilman ON DELETE CASCADEa, joten tilausrivit
     * poistetaan ensin samassa transaktiossa. Palauttaa poistettujen tilausten määrän.
     */
    @Transactional
    public int deleteOldCancelledOrders(int daysOld) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysOld);

        // 1) Tilausrivit: DELETE FROM orderitems WHERE order_id IN (SELECT id FROM orders WHERE ...)
        CriteriaDelete<OrderItem> deleteItems = cb.createCriteriaDelete(OrderItem.class);
        Root<OrderItem> itemRoot = deleteItems.from(OrderItem.class);
        Subquery<Integer> oldCancelledIds = deleteItems.subquery(Integer.class);
        Root<Order> sub = oldCancelledIds.from(Order.class);
        oldCancelledIds.select(sub.get("id"))
                .where(oldCancelledPredicate(cb, sub, threshold));
        deleteItems.where(itemRoot.get("order").get("id").in(oldCancelledIds));
        entityManager.createQuery(deleteItems).executeUpdate();

        // 2) Itse tilaukset
        CriteriaDelete<Order> delete = cb.createCriteriaDelete(Order.class);
        Root<Order> root = delete.from(Order.class);
        delete.where(oldCancelledPredicate(cb, root, threshold));

        return entityManager.createQuery(delete).executeUpdate();
    }

    private Predicate oldCancelledPredicate(CriteriaBuilder cb, Root<Order> root, LocalDateTime threshold) {
        Predicate isCancelled = cb.equal(root.get("status"), "CANCELLED");
        Predicate isOld = cb.lessThan(root.get("orderDate"), threshold);
        return cb.and(isCancelled, isOld);
    }
}
