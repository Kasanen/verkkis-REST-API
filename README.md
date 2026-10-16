# Verkkis API

Authors: Elias Norta, Veijo Kasanen

## Demo project documentation

This project is a web shop and supplier admin backend. It covers product, customer, supplier, and order management in a commerce workflow.

## Usage completion view

From this perspective, the project is complete for the main backend flow:

- supplier management
- product catalog and stock handling
- customer and order processing
- discount/restock logic
- reporting for low stock and sales

This is not a full storefront or production security setup, but it matches the backend admin/web shop use case the project is designed to demonstrate.

## What is implemented

The project includes the main backend flow for a web shop:

- supplier CRUD and supplier product management
- product catalog, filtering, stock updates, and pricing logic
- customer and address handling
- order creation, cancellation, status updates, and order history
- category-based product browsing
- reports for low stock, sales, and top customers
- JPA relationships, inheritance, and database triggers for business rules

## Installation and Setup

### Prerequisites

- Java 21
- Maven
- MariaDB or MySQL running locally
- A database named `verkkis`

### Database configuration

The application configuration is in `src/main/resources/application.yaml`.

### Run the application

```bash
./mvnw spring-boot:run
```

The app starts on port `8081`.

## Project Structure

![Project Structure Diagram](/resource/diagram2.png)

```text
src/
├── main/
│   ├── java/
│   │   └── verkkis/
│   │       └── demo/
│   │           ├── DemoApplication.java
│   │           ├── controller/
│   │           │   └── SupplierController.java
│   │           ├── converter/
│   │           │   └── LowercaseEmailConverter.java
│   │           ├── model/
│   │           │   ├── Address.java
│   │           │   ├── Contact.java
│   │           │   ├── Customer.java
│   │           │   ├── CustomerAddress.java
│   │           │   ├── Order.java
│   │           │   ├── OrderItem.java
│   │           │   ├── Product.java
│   │           │   ├── ProductCategory.java
│   │           │   ├── Supplier.java
│   │           │   └── SupplierAddress.java
│   │           ├── repository/
│   │           │   ├── AddressRepository.java
│   │           │   ├── ContactRepository.java
│   │           │   ├── CustomerRepository.java
│   │           │   ├── CustomerAddressRepository.java
│   │           │   ├── OrderCriteriaRepository.java
│   │           │   ├── OrderRepository.java
│   │           │   ├── ProductCategoryRepository.java
│   │           │   ├── ProductCriteriaRepository.java
│   │           │   ├── ProductRepository.java
│   │           │   ├── SupplierAddressRepository.java
│   │           │   └── SupplierRepository.java
│   │           └── service/
│   │               └── ProductService.java
│   └── resources/
│       ├── application.yaml
│       ├── static/
│       └── templates/
└── test/
    └── java/
        └── verkkis/
            ├── InheritanceTest.java
            ├── LowercaseEmailConverterTest.java
            ├── ManyToManyTest.java
            ├── OneToOneTest.java
            ├── OrderCriteriaDeleteTest.java
            ├── OrderPrePersistTest.java
            ├── ProductRepositoryTest.java
            ├── SupplierProductsScenarioTest.java
            └── ...
```

## Database features

The `verkkis` schema includes a few database-level features that support the webshop logic.

SQL scripts used in project [sql](sql).

| Feature      | Implementation                                                                             | Purpose                                                       |
| ------------ | ------------------------------------------------------------------------------------------ | ------------------------------------------------------------- |
| Indexes      | `idx_orders_order_date`, `idx_orders_status`, `idx_customers_email`, `idx_suppliers_email` | Faster lookups and unique customer/supplier email checks      |
| Views        | `product_overview`, `monthly_sales_summary`                                                | Product listing and monthly sales summaries                   |
| Triggers     | `trg_orderitems_before_insert`, `trg_orderitems_after_insert`                              | Prevents invalid stock orders and updates stock automatically |
| Event        | `evt_clean_old_cancelled_orders`                                                           | Removes cancelled orders older than 90 days                   |
| Temporal     | `ALTER TABLE orders ADD SYSTEM VERSIONING`                                                 | Keeps order history for status tracking                       |
| Transactions | Order creation in one transaction                                                          | Keeps order + lines + stock updates consistent                |

## API Documentation

Base URL:

```http
http://localhost:8081
```

All requests use JSON and the app is organized around `/api` resources.

### Endpoint reference

All endpoints return JSON. Common response codes are `200 OK`, `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict`, and `500 Internal Server Error`.

Common query parameters:

- `page`, `size`
- `status` for orders
- `inStock` for products
- `threshold`, `limit` for reports
- `olderThanDays` for cleanup

| Resource   | Method | Endpoint                     | Query params              | Success JSON                       | Errors                     |
| ---------- | ------ | ---------------------------- | ------------------------- | ---------------------------------- | -------------------------- |
| Suppliers  | GET    | `/api/suppliers`             | `page`, `size`            | list of supplier objects           | `400`, `500`               |
| Suppliers  | GET    | `/api/suppliers/{id}`        | —                         | supplier object                    | `404`, `500`               |
| Suppliers  | POST   | `/api/suppliers`             | —                         | created supplier object            | `400`, `409`, `500`        |
| Suppliers  | PUT    | `/api/suppliers/{id}`        | —                         | updated supplier object            | `400`, `404`, `500`        |
| Suppliers  | DELETE | `/api/suppliers/{id}`        | —                         | deleted confirmation or empty body | `404`, `500`               |
| Products   | GET    | `/api/products`              | `inStock`, `page`, `size` | product list                       | `400`, `500`               |
| Products   | GET    | `/api/products/{id}`         | —                         | product object                     | `404`, `500`               |
| Products   | POST   | `/api/products`              | —                         | created product object             | `400`, `409`, `500`        |
| Products   | PUT    | `/api/products/{id}`         | —                         | updated product object             | `400`, `404`, `500`        |
| Products   | DELETE | `/api/products/{id}`         | —                         | deleted confirmation or empty body | `404`, `500`               |
| Customers  | GET    | `/api/customers`             | `page`, `size`            | customer list                      | `400`, `500`               |
| Customers  | GET    | `/api/customers/{id}`        | —                         | customer object                    | `404`, `500`               |
| Customers  | POST   | `/api/customers`             | —                         | created customer object            | `400`, `409`, `500`        |
| Customers  | PUT    | `/api/customers/{id}`        | —                         | updated customer object            | `400`, `404`, `500`        |
| Customers  | DELETE | `/api/customers/{id}`        | —                         | deleted confirmation or empty body | `404`, `500`               |
| Orders     | GET    | `/api/orders`                | `status`, `page`, `size`  | paged order summary list           | `400`, `500`               |
| Orders     | GET    | `/api/orders/{id}`           | —                         | order object with items            | `404`, `500`               |
| Orders     | GET    | `/api/orders/{id}/history`   | —                         | list of status history entries     | `404`, `500`               |
| Orders     | POST   | `/api/orders`                | —                         | created order object               | `400`, `409`, `500`        |
| Orders     | POST   | `/api/orders/{id}/cancel`    | —                         | cancelled order object             | `409`, `404`, `500`        |
| Orders     | PATCH  | `/api/orders/{id}/status`    | —                         | updated order object               | `400`, `409`, `404`, `500` |
| Orders     | DELETE | `/api/orders/cancelled`      | `olderThanDays`           | `{ "deletedOrders": n }`           | `400`, `500`               |
| Categories | GET    | `/api/categories`            | `page`, `size`            | category list                      | `400`, `500`               |
| Categories | GET    | `/api/categories/{id}`       | —                         | category object                    | `404`, `500`               |
| Reports    | GET    | `/api/reports/monthly-sales` | —                         | sales summary JSON                 | `500`                      |
| Reports    | GET    | `/api/reports/low-stock`     | `threshold`, `limit`      | low-stock list                     | `400`, `500`               |
| Reports    | GET    | `/api/reports/top-products`  | `limit`                   | top products list                  | `400`, `500`               |
| Reports    | GET    | `/api/reports/top-customers` | `limit`                   | top customers list                 | `400`, `500`               |

### Example JSON response

```json
{
  "id": 1,
  "customer": {
    "id": 5,
    "name": "Anna Smith",
    "email": "anna@example.com"
  },
  "orderDate": "2026-10-16T12:00:00",
  "deliveryDate": null,
  "status": "NEW",
  "shippingAddress": {
    "id": 2,
    "street": "Main Street 1",
    "city": "Helsinki"
  },
  "items": [
    {
      "productId": 10,
      "productName": "Laptop",
      "quantity": 1,
      "unitPrice": 899.99,
      "lineTotal": 899.99
    }
  ],
  "total": 899.99
}
```

### Typical request bodies

```json
{
  "name": "Nordic Audio",
  "email": "sales@nordicaudio.fi"
}
```

```json
{
  "percent": 15
}
```

```json
{
  "status": "SHIPPED"
}
```

### Example calls

```bash
curl http://localhost:8081/api/suppliers
curl http://localhost:8081/api/products?inStock=true
curl http://localhost:8081/api/orders
curl http://localhost:8081/api/reports/low-stock?threshold=5&limit=20
```

## Testing

Current tests cover:

- lowercasing email conversion
- one-to-one mappings
- many-to-many mappings
- joined inheritance for addresses
- pre-persist defaults for orders
- criteria delete for old cancelled orders
- supplier-product repository scenarios
- custom product stock updates

Run the tests with:

```bash
./mvnw test
```
