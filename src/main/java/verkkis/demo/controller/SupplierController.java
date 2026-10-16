package verkkis.demo.controller;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import verkkis.demo.dto.BulkUpdateResponse;
import verkkis.demo.dto.DiscountRequest;
import verkkis.demo.dto.PageResponse;
import verkkis.demo.dto.ProductResponse;
import verkkis.demo.dto.SupplierRequest;
import verkkis.demo.dto.SupplierResponse;
import verkkis.demo.dto.SupplierSummary;
import verkkis.demo.exception.BadRequestException;
import verkkis.demo.service.ProductService;
import verkkis.demo.service.ProductService.ProductFilter;
import verkkis.demo.service.SupplierService;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;
    private final ProductService productService;

    public SupplierController(SupplierService supplierService, ProductService productService) {
        this.supplierService = supplierService;
        this.productService = productService;
    }

    // GET /api/suppliers — kaikki toimittajat tuotemäärineen
    @GetMapping
    public List<SupplierSummary> list() {
        return supplierService.list();
    }

    // GET /api/suppliers/{id} — toimittaja osoitteineen
    @GetMapping("/{id}")
    public SupplierResponse get(@PathVariable Integer id) {
        return supplierService.get(id);
    }

    // GET /api/suppliers/{id}/products — toimittajan tuotteet sivutettuna
    @GetMapping("/{id}/products")
    public PageResponse<ProductResponse> products(@PathVariable Integer id,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        supplierService.requireExists(id);
        return productService.search(new ProductFilter(null, null, id, null, null, null), pageable);
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> create(@RequestBody SupplierRequest request) {
        SupplierResponse created = supplierService.create(request);
        return ResponseEntity.created(URI.create("/api/suppliers/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public SupplierResponse update(@PathVariable Integer id, @RequestBody SupplierRequest request) {
        return supplierService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/suppliers/{id}/discount { "percent": 15 } — alennus kaikkiin toimittajan tuotteisiin
    @PostMapping("/{id}/discount")
    public BulkUpdateResponse discount(@PathVariable Integer id, @RequestBody DiscountRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        return supplierService.applyDiscount(id, request.percent());
    }

    // POST /api/suppliers/{id}/restock — +50 kpl jokaiseen toimittajan tuotteeseen
    @PostMapping("/{id}/restock")
    public BulkUpdateResponse restock(@PathVariable Integer id) {
        return supplierService.restock(id);
    }
}
