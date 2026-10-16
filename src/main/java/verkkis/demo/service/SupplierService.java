package verkkis.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import verkkis.demo.dto.AddressResponse;
import verkkis.demo.dto.BulkUpdateResponse;
import verkkis.demo.dto.SupplierRequest;
import verkkis.demo.dto.SupplierResponse;
import verkkis.demo.dto.SupplierSummary;
import verkkis.demo.exception.BadRequestException;
import verkkis.demo.exception.ConflictException;
import verkkis.demo.exception.NotFoundException;
import verkkis.demo.exception.Validation;
import verkkis.demo.model.Supplier;
import verkkis.demo.repository.ProductCriteriaRepository;
import verkkis.demo.repository.ProductRepository;
import verkkis.demo.repository.SupplierAddressRepository;
import verkkis.demo.repository.SupplierRepository;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierAddressRepository supplierAddressRepository;
    private final ProductRepository productRepository;
    private final ProductCriteriaRepository productCriteriaRepository;
    private final ProductService productService;

    public SupplierService(SupplierRepository supplierRepository, SupplierAddressRepository supplierAddressRepository,
            ProductRepository productRepository, ProductCriteriaRepository productCriteriaRepository,
            ProductService productService) {
        this.supplierRepository = supplierRepository;
        this.supplierAddressRepository = supplierAddressRepository;
        this.productRepository = productRepository;
        this.productCriteriaRepository = productCriteriaRepository;
        this.productService = productService;
    }

    public List<SupplierSummary> list() {
        return supplierRepository.findAllWithProductCount();
    }

    public SupplierResponse get(Integer id) {
        return toResponse(find(id));
    }

    public void requireExists(Integer id) {
        if (!supplierRepository.existsById(id)) {
            throw new NotFoundException("Supplier", id);
        }
    }

    @Transactional
    public SupplierResponse create(SupplierRequest req) {
        Supplier supplier = new Supplier();
        apply(supplier, req);
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse update(Integer id, SupplierRequest req) {
        Supplier supplier = find(id);
        apply(supplier, req);
        return toResponse(supplier);
    }

    @Transactional
    public void delete(Integer id) {
        requireExists(id);
        long products = productRepository.countBySupplier_Id(id);
        if (products > 0) {
            throw new ConflictException("Supplier " + id + " still has " + products + " products");
        }
        supplierAddressRepository.deleteBySupplier_Id(id);
        supplierRepository.deleteById(id);
    }

    /** Alennus kaikille toimittajan tuotteille yhdellä UPDATE-lauseella (Criteria API). */
    @Transactional
    public BulkUpdateResponse applyDiscount(Integer id, BigDecimal percent) {
        requireExists(id);
        if (percent == null || percent.signum() <= 0 || percent.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new BadRequestException("percent must be greater than 0 and at most 90");
        }
        BigDecimal multiplier = BigDecimal.ONE.subtract(percent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        int updated = productCriteriaRepository.applyDiscountForSupplier(id, multiplier);
        return new BulkUpdateResponse(updated, "Prices reduced by " + percent.stripTrailingZeros().toPlainString() + " %");
    }

    @Transactional
    public BulkUpdateResponse restock(Integer id) {
        requireExists(id);
        int updated = productService.increaseStockForSupplier(id);
        return new BulkUpdateResponse(updated, "Stock increased by 50 for each product");
    }

    private Supplier find(Integer id) {
        return supplierRepository.findById(id).orElseThrow(() -> new NotFoundException("Supplier", id));
    }

    private SupplierResponse toResponse(Supplier s) {
        List<AddressResponse> addresses = s.getId() == null ? List.of()
                : supplierAddressRepository.findBySupplier_IdOrderById(s.getId()).stream()
                        .map(AddressResponse::from).toList();
        long productCount = s.getId() == null ? 0 : productRepository.countBySupplier_Id(s.getId());
        return new SupplierResponse(s.getId(), s.getName(), s.getContactName(), s.getPhone(), s.getEmail(),
                productCount, addresses);
    }

    private void apply(Supplier supplier, SupplierRequest req) {
        if (req == null) {
            throw new BadRequestException("Request body is required");
        }
        supplier.setName(Validation.requireText(req.name(), "name", 255));
        supplier.setContactName(Validation.optionalText(req.contactName(), "contactName", 100));
        supplier.setPhone(Validation.optionalText(req.phone(), "phone", 30));
        supplier.setEmail(Validation.optionalEmail(req.email(), "email"));
    }
}
