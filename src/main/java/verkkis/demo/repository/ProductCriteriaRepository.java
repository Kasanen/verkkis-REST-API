package verkkis.demo.repository;

import verkkis.demo.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Repository
public class ProductCriteriaRepository {

    private final EntityManager entityManager;

    public ProductCriteriaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public int applyDiscountForSupplier(Integer supplierId, BigDecimal discountMultiplier) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<Product> update = cb.createCriteriaUpdate(Product.class);
        Root<Product> root = update.from(Product.class);

        Path<BigDecimal> pricePath = root.get("price");
        update.set(pricePath, cb.prod(pricePath, discountMultiplier));
        update.where(cb.equal(root.get("supplier").get("id"), supplierId));

        return entityManager.createQuery(update).executeUpdate();
    }
}