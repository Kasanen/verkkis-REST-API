package verkkis.demo.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import verkkis.demo.dto.CreateOrderRequest;
import verkkis.demo.dto.OrderHistoryEntry;
import verkkis.demo.dto.OrderResponse;
import verkkis.demo.dto.OrderSummary;
import verkkis.demo.dto.PageResponse;
import verkkis.demo.dto.StatusUpdateRequest;
import verkkis.demo.exception.BadRequestException;
import verkkis.demo.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST /api/orders — tilauksen tekeminen (transaktio)
    @PostMapping
    public ResponseEntity<OrderResponse> place(@RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.placeOrder(request);
        return ResponseEntity.created(URI.create("/api/orders/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Integer id) {
        return orderService.get(id);
    }

    @GetMapping("/{id}/history")
    public List<OrderHistoryEntry> history(@PathVariable Integer id) {
        return orderService.getHistory(id);
    }

    // GET /api/orders?status=NEW&page=0&size=20 — ylläpidon tilauslistaus
    @GetMapping
    public PageResponse<OrderSummary> list(@RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.list(status, pageable);
    }

    // POST /api/orders/{id}/cancel — asiakas peruu tilauksen, tuotteet palautetaan varastoon
    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Integer id) {
        return orderService.cancel(id);
    }

    // PATCH /api/orders/{id}/status { "status": "SHIPPED" }
    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Integer id, @RequestBody StatusUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        return orderService.updateStatus(id, request.status());
    }

    // DELETE /api/orders/cancelled?olderThanDays=90 — vanhojen peruttujen tilausten siivous
    @DeleteMapping("/cancelled")
    public Map<String, Integer> deleteOldCancelled(@RequestParam(defaultValue = "90") Integer olderThanDays) {
        return Map.of("deletedOrders", orderService.deleteOldCancelled(olderThanDays));
    }
}
