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

The project currently includes:

- Supplier CRUD over HTTP.
- Repository-based CRUD for customers, orders, addresses, contacts, product categories, and products.
- Custom repository operations for product stock updates, supplier-wide discounts, and deleting old cancelled orders.
- JPA inheritance for address entities.
- One-to-one, one-to-many, and many-to-many relationships.
- Email normalization through a JPA attribute converter.
- Persistence tests that cover the implemented repository behavior.

There is no authentication layer, no authorization layer, and no separate frontend in this project.

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

The `verkkis` MariaDB schema is managed with the scripts. These parts are invisible in the API but are part of the solution.

| Feature          | Implementation                                                                                    | Purpose                                                                                                                                                                                                          |
| ---------------- | ------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Indexes**      | `idx_orders_order_date`, `idx_orders_status`; unique `idx_customers_email`, `idx_suppliers_email` | Faster date and status queries and email lookups; unique emails prevent duplicates. Effect checked with `EXPLAIN` and `mysqlslap`. The optimizer skips an index when a query matches a large share of the table. |
| **Views**        | `product_overview`, `monthly_sales_summary`                                                       | `product_overview` joins products with category and supplier for the web-shop product list. `monthly_sales_summary` gives orders, items and revenue per month, excluding cancelled orders.                       |
| **Triggers**     | `trg_orderitems_before_insert`, `trg_orderitems_after_insert`                                     | The BEFORE trigger rejects an order line if stock is insufficient (`SQLSTATE 45000`). The AFTER trigger reduces `products.stock_quantity`. The application must not reduce stock itself.                         |
| **Event**        | `evt_clean_old_cancelled_orders`, runs daily                                                      | Deletes `CANCELLED` orders older than 90 days. Requires `event_scheduler=ON`. Tested on a copy table.                                                                                                            |
| **Temporal**     | `ALTER TABLE orders ADD SYSTEM VERSIONING`                                                        | Keeps every version of an order (transaction time), so status history can be queried with `FOR SYSTEM_TIME ALL` / `AS OF` and is served by `GET /orders/{id}/history`.                                           |
| **Transactions** | Order creation in one transaction                                                                 | Order, order lines and stock changes are saved together or not at all.                                                                                                                                           |

## API Documentation

Base URL:

```http
http://localhost:8081
```

All requests use JSON and the app is organized around `/api` resources.

### Endpoint quick reference

| Resource           | Method | Endpoint                                    | Purpose                       |
| ------------------ | ------ | ------------------------------------------- | ----------------------------- |
| Suppliers          | GET    | `/api/suppliers`                            | List all suppliers            |
| Suppliers          | GET    | `/api/suppliers/{id}`                       | Get one supplier              |
| Suppliers          | GET    | `/api/suppliers/{id}/products`              | Supplier products with paging |
| Suppliers          | POST   | `/api/suppliers`                            | Create supplier               |
| Suppliers          | PUT    | `/api/suppliers/{id}`                       | Update supplier               |
| Suppliers          | DELETE | `/api/suppliers/{id}`                       | Delete supplier               |
| Suppliers          | POST   | `/api/suppliers/{id}/discount`              | Apply percentage discount     |
| Suppliers          | POST   | `/api/suppliers/{id}/restock`               | Restock supplier products     |
| Products           | GET    | `/api/products`                             | Search/filter products        |
| Products           | GET    | `/api/products/{id}`                        | Get product by id             |
| Products           | POST   | `/api/products`                             | Create product                |
| Products           | PUT    | `/api/products/{id}`                        | Update product                |
| Products           | DELETE | `/api/products/{id}`                        | Delete product                |
| Customers          | GET    | `/api/customers`                            | Search customers              |
| Customers          | GET    | `/api/customers/{id}`                       | Get customer                  |
| Customers          | POST   | `/api/customers`                            | Register customer             |
| Customers          | PUT    | `/api/customers/{id}`                       | Update customer               |
| Customers          | DELETE | `/api/customers/{id}`                       | Delete customer               |
| Customer addresses | GET    | `/api/customers/{id}/addresses`             | List addresses                |
| Customer addresses | POST   | `/api/customers/{id}/addresses`             | Add address                   |
| Customer addresses | PUT    | `/api/customers/{id}/addresses/{addressId}` | Update address                |
| Customer addresses | DELETE | `/api/customers/{id}/addresses/{addressId}` | Delete address                |
| Orders             | GET    | `/api/customers/{id}/orders`                | Customer order history        |
| Orders             | POST   | `/api/orders`                               | Create order                  |
| Orders             | GET    | `/api/orders/{id}`                          | Get order                     |
| Orders             | GET    | `/api/orders`                               | List orders                   |
| Orders             | POST   | `/api/orders/{id}/cancel`                   | Cancel order                  |
| Orders             | PATCH  | `/api/orders/{id}/status`                   | Update order status           |
| Orders             | DELETE | `/api/orders/cancelled?olderThanDays=90`    | Remove old cancelled orders   |
| Categories         | GET    | `/api/categories`                           | List categories               |
| Categories         | GET    | `/api/categories/{id}`                      | Get category                  |
| Categories         | GET    | `/api/categories/{id}/products`             | Category products             |
| Categories         | POST   | `/api/categories`                           | Create category               |
| Categories         | PUT    | `/api/categories/{id}`                      | Update category               |
| Categories         | DELETE | `/api/categories/{id}`                      | Delete category               |
| Reports            | GET    | `/api/reports/monthly-sales`                | Monthly sales report          |
| Reports            | GET    | `/api/reports/low-stock`                    | Low-stock product report      |
| Reports            | GET    | `/api/reports/top-products`                 | Top-selling products          |
| Reports            | GET    | `/api/reports/top-customers`                | Top customers                 |

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

## Security

This project currently has:

- no authentication
- no authorization
- no Spring Security configuration
- no role-based access control

This is educational code, not a production-ready secured API.

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
