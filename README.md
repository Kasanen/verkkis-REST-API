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

For the code-based API contract reference, see [API_ENDPOINTS.md](API_ENDPOINTS.md).

## What is implemented

The project includes the main backend flow for a web shop:

- supplier CRUD and supplier product management
- product catalog, filtering, stock updates, and pricing logic
- customer and address handling
- order creation, cancellation, status updates, and order history
- category-based product browsing
- reports for low stock, sales, and top customers
- JPA relationships, inheritance, and database triggers for business rules

---

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

---

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

---

## Security and data protection

Security in this project is implemented mainly at the database level.

### Database users and privileges

Two database roles are used, following the principle of least privilege.

| Role          | Account                | Privileges                                                                            | Purpose                                                                                |
| ------------- | ---------------------- | ------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| Administrator | `admin`@`localhost`    | `ALL PRIVILEGES` on `verkkis.*`                                                       | Manages products, customers, orders, suppliers and categories, and runs schema changes |
| Customer      | `customer`@`localhost` | `SELECT` on `products` and `productcategories`; `INSERT` on `orders` and `orderitems` | Web-shop use only: browse products and place orders                                    |

```sql
-- Administrator
CREATE USER 'admin'@'localhost' IDENTIFIED BY '<strong password>';
GRANT ALL PRIVILEGES ON verkkis.* TO 'admin'@'localhost';

-- Customer (web-shop user)
CREATE USER 'customer'@'localhost' IDENTIFIED BY '<strong password>';
GRANT SELECT ON verkkis.products          TO 'customer'@'localhost';
GRANT SELECT ON verkkis.productcategories TO 'customer'@'localhost';
GRANT INSERT ON verkkis.orders            TO 'customer'@'localhost';
GRANT INSERT ON verkkis.orderitems        TO 'customer'@'localhost';
```

The customer account cannot read other customers' data, change or delete products, or modify existing orders. If a user is given too many privileges by mistake, they are removed with `REVOKE`:

```sql
REVOKE ALL PRIVILEGES ON verkkis.* FROM 'customer'@'localhost';
```

---

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
