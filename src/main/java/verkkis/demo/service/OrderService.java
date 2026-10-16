package verkkis.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import verkkis.demo.dto.AddressResponse;
import verkkis.demo.dto.CreateOrderRequest;
import verkkis.demo.dto.CustomerRef;
import verkkis.demo.dto.OrderHistoryEntry;
import verkkis.demo.dto.OrderLineResponse;
import verkkis.demo.dto.OrderResponse;
import verkkis.demo.dto.OrderSummary;
import verkkis.demo.dto.PageResponse;
import verkkis.demo.exception.BadRequestException;
import verkkis.demo.exception.ConflictException;
import verkkis.demo.exception.NotFoundException;
import verkkis.demo.exception.Validation;
import verkkis.demo.model.Customer;
import verkkis.demo.model.CustomerAddress;
import verkkis.demo.model.Order;
import verkkis.demo.model.OrderItem;
import verkkis.demo.model.OrderStatus;
import verkkis.demo.model.Product;
import verkkis.demo.repository.CustomerAddressRepository;
import verkkis.demo.repository.CustomerRepository;
import verkkis.demo.repository.OrderCriteriaRepository;
import verkkis.demo.repository.OrderRepository;
import verkkis.demo.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final int MAX_LINES = 50;
    private static final int MAX_QUANTITY_PER_LINE = 1000;

    private final OrderRepository orderRepository;
    private final OrderCriteriaRepository orderCriteriaRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    public OrderService(OrderRepository orderRepository, OrderCriteriaRepository orderCriteriaRepository,
            CustomerRepository customerRepository, CustomerAddressRepository addressRepository,
            ProductRepository productRepository, EntityManager entityManager) {
        this.orderRepository = orderRepository;
        this.orderCriteriaRepository = orderCriteriaRepository;
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.entityManager = entityManager;
    }

    /**
     * Luo tilauksen yhtenä transaktiona:
     * 1. lukitsee tilattavat tuoterivit (SELECT ... FOR UPDATE),
     * 2. tarkistaa varastosaldot,
     * 3. tallentaa tilauksen ja rivit tilaushetken hinnoilla.
     * Varastosaldon vähentää tietokannan trigger orderitems_after_insert.
     * Jos mikä tahansa vaihe epäonnistuu, koko transaktio perutaan (rollback).
     */
    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest req) {
        if (req == null) {
            throw new BadRequestException("Request body is required");
        }
        if (req.customerId() == null) {
            throw new BadRequestException("customerId is required");
        }
        if (req.shippingAddressId() == null) {
            throw new BadRequestException("shippingAddressId is required");
        }
        Map<Integer, Integer> quantities = mergeLines(req.items());

        Customer customer = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new BadRequestException("Customer " + req.customerId() + " does not exist"));
        CustomerAddress address = addressRepository.findByIdAndCustomer_Id(req.shippingAddressId(), customer.getId())
                .orElseThrow(() -> new BadRequestException(
                        "Address " + req.shippingAddressId() + " does not belong to customer " + customer.getId()));

        Map<Integer, Product> products = productRepository.findAllByIdForUpdate(quantities.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Integer> missing = quantities.keySet().stream().filter(id -> !products.containsKey(id)).toList();
        if (!missing.isEmpty()) {
            throw new BadRequestException("Unknown product ids: " + missing);
        }

        List<String> shortages = new ArrayList<>();
        quantities.forEach((productId, qty) -> {
            Product p = products.get(productId);
            if (p.getStockQuantity() < qty) {
                shortages.add("product " + productId + " (requested " + qty + ", in stock " + p.getStockQuantity() + ")");
            }
        });
        if (!shortages.isEmpty()) {
            throw new ConflictException("Insufficient stock: " + String.join(", ", shortages));
        }

        Order order = new Order(customer, address, null, null);
        quantities.forEach((productId, qty) -> {
            Product p = products.get(productId);
            order.addItem(new OrderItem(order, p, qty, p.getPrice()));
        });
        orderRepository.saveAndFlush(order);

        // Triggeri muutti saldot kannassa: päivitetään muistissa olevat tuotteet vastaamaan sitä
        products.values().forEach(entityManager::refresh);

        return toResponse(order);
    }

    public OrderResponse get(Integer id) {
        return toResponse(findWithDetails(id));
    }

    public List<OrderHistoryEntry> getHistory(Integer id) {
        findWithDetails(id);
        return orderRepository.findHistoryById(id);
    }

    public PageResponse<OrderSummary> list(String status, Pageable pageable) {
        String statusFilter = null;
        if (status != null && !status.isBlank()) {
            OrderStatus parsed = OrderStatus.parse(status);
            if (parsed == null) {
                throw new BadRequestException("Unknown status '" + status + "'");
            }
            statusFilter = parsed.name();
        }
        return PageResponse.of(orderRepository.findSummaries(statusFilter, pageable));
    }

    /** Asiakkaan peruutus: sallittu tiloissa NEW ja PAID. Palauttaa tuotteet varastoon. */
    @Transactional
    public OrderResponse cancel(Integer id) {
        Order order = findWithDetails(id);
        OrderStatus current = OrderStatus.parse(order.getStatus());
        if (current == null || !current.isCancellable()) {
            throw new ConflictException("Order " + id + " with status " + order.getStatus() + " cannot be cancelled");
        }
        for (OrderItem item : order.getItems()) {
            productRepository.addStock(item.getProduct().getId(), item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED.name());
        return toResponse(order);
    }

    /** Ylläpidon tilamuutos (NEW -> PAID -> SHIPPED -> DELIVERED). */
    @Transactional
    public OrderResponse updateStatus(Integer id, String newStatus) {
        OrderStatus next = OrderStatus.parse(newStatus);
        if (next == null) {
            throw new BadRequestException("status must be one of NEW, PAID, SHIPPED, DELIVERED, CANCELLED");
        }
        if (next == OrderStatus.CANCELLED) {
            return cancel(id);
        }
        Order order = findWithDetails(id);
        OrderStatus current = OrderStatus.parse(order.getStatus());
        if (current == null || !current.canMoveTo(next)) {
            throw new ConflictException("Cannot change order " + id + " from " + order.getStatus() + " to " + next);
        }
        order.setStatus(next.name());
        if (next == OrderStatus.DELIVERED) {
            order.setDeliveryDate(LocalDateTime.now());
        }
        return toResponse(order);
    }

    @Transactional
    public int deleteOldCancelled(Integer olderThanDays) {
        int days = Validation.requirePositive(olderThanDays, "olderThanDays");
        return orderCriteriaRepository.deleteOldCancelledOrders(days);
    }

    private Order findWithDetails(Integer id) {
        return orderRepository.findWithDetailsById(id).orElseThrow(() -> new NotFoundException("Order", id));
    }

    private Map<Integer, Integer> mergeLines(List<CreateOrderRequest.OrderLineRequest> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }
        if (lines.size() > MAX_LINES) {
            throw new BadRequestException("Order can contain at most " + MAX_LINES + " lines");
        }
        // Sama tuote kahdesti -> yhdistetään yhdeksi riviksi (pääavain on order_id + product_id)
        Map<Integer, Integer> merged = new LinkedHashMap<>();
        for (CreateOrderRequest.OrderLineRequest line : lines) {
            if (line == null || line.productId() == null) {
                throw new BadRequestException("Each item needs a productId");
            }
            int qty = Validation.requirePositive(line.quantity(), "quantity");
            int total = merged.merge(line.productId(), qty, Integer::sum);
            if (total > MAX_QUANTITY_PER_LINE) {
                throw new BadRequestException("Quantity per product can be at most " + MAX_QUANTITY_PER_LINE);
            }
        }
        return merged;
    }

    private OrderResponse toResponse(Order o) {
        Customer c = o.getCustomer();
        CustomerRef customer = new CustomerRef(c.getId(), c.getFirstName() + " " + c.getLastName(), c.getEmail());
        List<OrderLineResponse> lines = o.getItems().stream()
                .map(i -> new OrderLineResponse(i.getProduct().getId(), i.getProduct().getName(), i.getQuantity(),
                        i.getUnitPrice(), i.getLineTotal()))
                .toList();
        return new OrderResponse(o.getId(), customer, o.getOrderDate(), o.getDeliveryDate(), o.getStatus(),
                AddressResponse.from(o.getShippingAddress()), lines, o.getTotal());
    }
}
