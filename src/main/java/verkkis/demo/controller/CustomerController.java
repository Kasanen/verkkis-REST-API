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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import verkkis.demo.dto.AddressRequest;
import verkkis.demo.dto.AddressResponse;
import verkkis.demo.dto.CustomerRequest;
import verkkis.demo.dto.CustomerResponse;
import verkkis.demo.dto.OrderSummary;
import verkkis.demo.dto.PageResponse;
import verkkis.demo.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // GET /api/customers?q=virta — haku sukunimen tai sähköpostin alulla
    @GetMapping
    public PageResponse<CustomerResponse> search(@RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return customerService.search(q, pageable);
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Integer id) {
        return customerService.get(id);
    }

    // POST /api/customers — rekisteröityminen
    @PostMapping
    public ResponseEntity<CustomerResponse> register(@RequestBody CustomerRequest request) {
        CustomerResponse created = customerService.register(request);
        return ResponseEntity.created(URI.create("/api/customers/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Integer id, @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Osoitteet ---

    @GetMapping("/{id}/addresses")
    public List<AddressResponse> addresses(@PathVariable Integer id) {
        return customerService.listAddresses(id);
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<AddressResponse> addAddress(@PathVariable Integer id, @RequestBody AddressRequest request) {
        AddressResponse created = customerService.addAddress(id, request);
        return ResponseEntity.created(URI.create("/api/customers/" + id + "/addresses/" + created.id())).body(created);
    }

    @PutMapping("/{id}/addresses/{addressId}")
    public AddressResponse updateAddress(@PathVariable Integer id, @PathVariable Integer addressId,
            @RequestBody AddressRequest request) {
        return customerService.updateAddress(id, addressId, request);
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Integer id, @PathVariable Integer addressId) {
        customerService.deleteAddress(id, addressId);
        return ResponseEntity.noContent().build();
    }

    // --- Tilaushistoria ---

    @GetMapping("/{id}/orders")
    public List<OrderSummary> orders(@PathVariable Integer id) {
        return customerService.listOrders(id);
    }
}
