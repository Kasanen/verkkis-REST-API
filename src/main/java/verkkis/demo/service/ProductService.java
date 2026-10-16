package verkkis.demo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.JoinType;
import verkkis.demo.dto.PageResponse;
import verkkis.demo.dto.ProductRequest;
import verkkis.demo.dto.ProductResponse;
import verkkis.demo.exception.BadRequestException;
import verkkis.demo.exception.ConflictException;
import verkkis.demo.exception.NotFoundException;
import verkkis.demo.exception.Validation;
import verkkis.demo.model.Product;
import verkkis.demo.model.ProductCategory;
import verkkis.demo.model.Supplier;
import verkkis.demo.repository.OrderItemRepository;
import verkkis.demo.repository.ProductCategoryRepository;
import verkkis.demo.repository.ProductRepository;
import verkkis.demo.repository.SupplierRepository;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository, ProductCategoryRepository categoryRepository,
            SupplierRepository supplierRepository, OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /** Hakufiltterit tuotelistaukselle. Kaikki ovat valinnaisia. */
    public record ProductFilter(String q, Integer categoryId, Integer supplierId,
            BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock) {
    }

    public PageResponse<ProductResponse> search(ProductFilter f, Pageable pageable) {
        if (f.minPrice() != null && f.maxPrice() != null && f.minPrice().compareTo(f.maxPrice()) > 0) {
            throw new BadRequestException("minPrice cannot be greater than maxPrice");
        }

        // Rakennetaan WHERE-ehto dynaamisesti Criteria API:lla annettujen filttereiden perusteella
        List<Specification<Product>> specs = new ArrayList<>();
        if (f.q() != null && !f.q().isBlank()) {
            String pattern = "%" + f.q().trim().toLowerCase() + "%";
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }
        if (f.categoryId() != null) {
            specs.add((root, query, cb) -> cb.equal(root.join("category", JoinType.LEFT).get("id"), f.categoryId()));
        }
        if (f.supplierId() != null) {
            specs.add((root, query, cb) -> cb.equal(root.join("supplier", JoinType.LEFT).get("id"), f.supplierId()));
        }
        if (f.minPrice() != null) {
            specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), f.minPrice()));
        }
        if (f.maxPrice() != null) {
            specs.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), f.maxPrice()));
        }
        if (Boolean.TRUE.equals(f.inStock())) {
            specs.add((root, query, cb) -> cb.greaterThan(root.get("stockQuantity"), 0));
        }

        Specification<Product> spec = specs.isEmpty() ? Specification.unrestricted() : Specification.allOf(specs);
        return PageResponse.of(productRepository.findAll(spec, pageable).map(ProductResponse::from));
    }

    public ProductResponse get(Integer id) {
        return ProductResponse.from(productRepository.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Product", id)));
    }

    @Transactional
    public ProductResponse create(ProductRequest req) {
        Product product = new Product();
        apply(product, req);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Integer id, ProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
        apply(product, req);
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product", id);
        }
        // Tilauksissa esiintyvää tuotetta ei poisteta, jotta tilaushistoria säilyy
        if (orderItemRepository.existsByProduct_Id(id)) {
            throw new ConflictException("Product " + id + " appears in orders and cannot be deleted");
        }
        productRepository.deleteProductById(id);
    }

    /** Lisää kaikkien toimittajan tuotteiden saldoa 50 kpl (JPQL bulk update). */
    @Transactional
    public int increaseStockForSupplier(Integer supplierId) {
        return productRepository.increaseStockForSupplier(supplierId);
    }

    private void apply(Product product, ProductRequest req) {
        if (req == null) {
            throw new BadRequestException("Request body is required");
        }
        product.setName(Validation.requireText(req.name(), "name", 255));
        product.setDescription(Validation.optionalText(req.description(), "description", 65535));
        product.setPrice(Validation.requirePrice(req.price(), "price"));
        product.setStockQuantity(Validation.requireNonNegative(
                req.stockQuantity() == null ? 0 : req.stockQuantity(), "stockQuantity"));

        ProductCategory category = null;
        if (req.categoryId() != null) {
            category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new BadRequestException("Category " + req.categoryId() + " does not exist"));
        }
        product.setCategory(category);

        Supplier supplier = null;
        if (req.supplierId() != null) {
            supplier = supplierRepository.findById(req.supplierId())
                    .orElseThrow(() -> new BadRequestException("Supplier " + req.supplierId() + " does not exist"));
        }
        product.setSupplier(supplier);
    }
}
