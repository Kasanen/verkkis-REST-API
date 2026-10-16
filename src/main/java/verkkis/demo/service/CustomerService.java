package verkkis.demo.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import verkkis.demo.dto.AddressRequest;
import verkkis.demo.dto.AddressResponse;
import verkkis.demo.dto.CustomerRequest;
import verkkis.demo.dto.CustomerResponse;
import verkkis.demo.dto.OrderSummary;
import verkkis.demo.dto.PageResponse;
import verkkis.demo.exception.BadRequestException;
import verkkis.demo.exception.ConflictException;
import verkkis.demo.exception.NotFoundException;
import verkkis.demo.exception.Validation;
import verkkis.demo.model.Customer;
import verkkis.demo.model.CustomerAddress;
import verkkis.demo.repository.CustomerAddressRepository;
import verkkis.demo.repository.CustomerRepository;
import verkkis.demo.repository.OrderRepository;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository, CustomerAddressRepository addressRepository,
            OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
    }

    public PageResponse<CustomerResponse> search(String q, Pageable pageable) {
        String query = q == null || q.isBlank() ? null : q.trim();
        return PageResponse.of(customerRepository.search(query, pageable).map(c -> toResponse(c, false)));
    }

    public CustomerResponse get(Integer id) {
        return toResponse(find(id), true);
    }

    @Transactional
    public CustomerResponse register(CustomerRequest req) {
        Customer customer = new Customer();
        apply(customer, req);
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new ConflictException("A customer with email " + customer.getEmail() + " already exists");
        }
        return toResponse(customerRepository.save(customer), true);
    }

    @Transactional
    public CustomerResponse update(Integer id, CustomerRequest req) {
        Customer customer = find(id);
        apply(customer, req);
        if (customerRepository.existsByEmailAndIdNot(customer.getEmail(), id)) {
            throw new ConflictException("A customer with email " + customer.getEmail() + " already exists");
        }
        return toResponse(customer, true);
    }

    @Transactional
    public void delete(Integer id) {
        find(id);
        if (orderRepository.existsByCustomer_Id(id)) {
            throw new ConflictException("Customer " + id + " has orders and cannot be deleted");
        }
        addressRepository.deleteByCustomer_Id(id);
        customerRepository.deleteById(id);
    }

    // --- Osoitteet ---

    public List<AddressResponse> listAddresses(Integer customerId) {
        find(customerId);
        return addressRepository.findByCustomer_IdOrderById(customerId).stream().map(AddressResponse::from).toList();
    }

    @Transactional
    public AddressResponse addAddress(Integer customerId, AddressRequest req) {
        Customer customer = find(customerId);
        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        applyAddress(address, req);
        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(Integer customerId, Integer addressId, AddressRequest req) {
        CustomerAddress address = findAddress(customerId, addressId);
        applyAddress(address, req);
        return AddressResponse.from(address);
    }

    @Transactional
    public void deleteAddress(Integer customerId, Integer addressId) {
        CustomerAddress address = findAddress(customerId, addressId);
        if (orderRepository.existsByShippingAddress_Id(addressId)) {
            throw new ConflictException("Address " + addressId + " is used as a shipping address in orders");
        }
        addressRepository.delete(address);
    }

    // --- Tilaukset ---

    public List<OrderSummary> listOrders(Integer customerId) {
        find(customerId);
        return orderRepository.findSummariesByCustomer(customerId);
    }

    // --- Apumetodit ---

    private Customer find(Integer id) {
        return customerRepository.findById(id).orElseThrow(() -> new NotFoundException("Customer", id));
    }

    private CustomerAddress findAddress(Integer customerId, Integer addressId) {
        find(customerId);
        return addressRepository.findByIdAndCustomer_Id(addressId, customerId)
                .orElseThrow(() -> new NotFoundException("Address", addressId));
    }

    private CustomerResponse toResponse(Customer c, boolean withAddresses) {
        List<AddressResponse> addresses = withAddresses && c.getId() != null
                ? addressRepository.findByCustomer_IdOrderById(c.getId()).stream().map(AddressResponse::from).toList()
                : null;
        return new CustomerResponse(c.getId(), c.getFirstName(), c.getLastName(), c.getEmail(), c.getPhone(),
                addresses);
    }

    private void apply(Customer customer, CustomerRequest req) {
        if (req == null) {
            throw new BadRequestException("Request body is required");
        }
        customer.setFirstName(Validation.requireText(req.firstName(), "firstName", 100));
        customer.setLastName(Validation.requireText(req.lastName(), "lastName", 100));
        customer.setEmail(Validation.requireEmail(req.email(), "email"));
        customer.setPhone(Validation.optionalText(req.phone(), "phone", 30));
    }

    private void applyAddress(CustomerAddress address, AddressRequest req) {
        if (req == null) {
            throw new BadRequestException("Request body is required");
        }
        address.setStreetAddress(Validation.requireText(req.streetAddress(), "streetAddress", 255));
        address.setPostalCode(Validation.optionalText(req.postalCode(), "postalCode", 20));
        address.setCity(Validation.requireText(req.city(), "city", 100));
        address.setCountry(Validation.optionalText(req.country(), "country", 100));
    }
}
