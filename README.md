# Demo project documentation

This project is a Spring Boot application for studying JPA mappings, repository-based CRUD, and a small REST API.

The codebase currently exposes one public REST controller for suppliers. The rest of the domain is accessed through Spring Data JPA repositories and custom repository classes.

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

## REST API

Base path: `/suppliers`

All request bodies and responses use JSON.

### GET `/suppliers`

Purpose: return all suppliers.

Request format:

```http
GET /suppliers
```

Response format:

```json
[
  {
    "id": 1,
    "name": "Nordic Audio",
    "email": "sales@nordicaudio.fi",
    "products": []
  }
]
```

### GET `/suppliers/{id}`

Purpose: fetch one supplier by identifier.

Request format:

```http
GET /suppliers/1
```

Response format:

```json
{
  "id": 1,
  "name": "Nordic Audio",
  "email": "sales@nordicaudio.fi",
  "products": []
}
```

If the supplier does not exist, the API returns `404 Not Found`.

### POST `/suppliers`

Purpose: create a new supplier.

Request format:

```http
POST /suppliers
Content-Type: application/json

{
  "name": "Nordic Audio",
  "email": "sales@nordicaudio.fi"
}
```

Response format:

```json
{
  "id": 1,
  "name": "Nordic Audio",
  "email": "sales@nordicaudio.fi",
  "products": []
}
```

The endpoint returns `201 Created` on success.

### PUT `/suppliers/{id}`

Purpose: update an existing supplier.

Request format:

```http
PUT /suppliers/1
Content-Type: application/json

{
  "name": "Nordic Audio Oy",
  "email": "info@nordicaudio.fi"
}
```

Response format:

```json
{
  "id": 1,
  "name": "Nordic Audio Oy",
  "email": "info@nordicaudio.fi",
  "products": []
}
```

If the supplier does not exist, the API returns `404 Not Found`.

### DELETE `/suppliers/{id}`

Purpose: delete a supplier.

Request format:

```http
DELETE /suppliers/1
```

Response format:

```http
204 No Content
```

If the supplier does not exist, the API returns `404 Not Found`.

## Repository-backed functionality

These classes expose CRUD and custom data access even though they are not HTTP endpoints:

- [ProductRepository](src/main/java/verkkis/demo/repository/ProductRepository.java)
  - Standard CRUD through `JpaRepository`.
  - `findBySupplierId(Integer supplierId)`.
  - `increaseStockForSupplier(Integer supplierId)`.
- [SupplierRepository](src/main/java/verkkis/demo/repository/SupplierRepository.java)
  - Standard CRUD through `JpaRepository`.
- [CustomerRepository](src/main/java/verkkis/demo/repository/CustomerRepository.java)
  - Standard CRUD through `JpaRepository`.
- [OrderRepository](src/main/java/verkkis/demo/repository/OrderRepository.java)
  - Standard CRUD through `JpaRepository`.
- [AddressRepository](src/main/java/verkkis/demo/repository/AddressRepository.java)
  - Polymorphic read access for the address hierarchy.
- [ContactRepository](src/main/java/verkkis/demo/repository/ContactRepository.java)
  - Standard CRUD through `JpaRepository`.
- [ProductCategoryRepository](src/main/java/verkkis/demo/repository/ProductCategoryRepository.java)
  - Standard CRUD through `JpaRepository`.
- [CustomerAddressRepository](src/main/java/verkkis/demo/repository/CustomerAddressRepository.java)
  - Standard CRUD through `JpaRepository`.
- [SupplierAddressRepository](src/main/java/verkkis/demo/repository/SupplierAddressRepository.java)
  - Standard CRUD through `JpaRepository`.
- [ProductCriteriaRepository](src/main/java/verkkis/demo/repository/ProductCriteriaRepository.java)
  - `applyDiscountForSupplier(Integer supplierId, BigDecimal discountMultiplier)`.
- [OrderCriteriaRepository](src/main/java/verkkis/demo/repository/OrderCriteriaRepository.java)
  - `deleteOldCancelledOrders(int daysOld)`.

## Domain model

The persistence model currently includes:

- [Supplier](src/main/java/verkkis/demo/model/Supplier.java)
  - Has a unique email.
  - Owns a one-to-many relationship to products.
- [Product](src/main/java/verkkis/demo/model/Product.java)
  - Has name, price, and stock quantity.
  - Belongs to one supplier.
  - Can belong to one direct category reference and also many categories through the join table `product_category_mapping`.
- [ProductCategory](src/main/java/verkkis/demo/model/ProductCategory.java)
  - Many-to-many inverse side of product categories.
- [Customer](src/main/java/verkkis/demo/model/Customer.java)
  - Uses the lowercase email converter.
  - Has a primary contact and a one-to-many contact list.
- [Contact](src/main/java/verkkis/demo/model/Contact.java)
  - Belongs to a customer.
  - Can be referenced as the primary contact of one customer.
- [Order](src/main/java/verkkis/demo/model/Order.java)
  - Belongs to one customer.
  - Can have one shipping address.
  - Owns order items.
  - Sets default values on persist.
- [OrderItem](src/main/java/verkkis/demo/model/OrderItem.java)
  - Belongs to one order and one product.
- [Address](src/main/java/verkkis/demo/model/Address.java)
  - Abstract base entity for address inheritance.
- [CustomerAddress](src/main/java/verkkis/demo/model/CustomerAddress.java)
  - Joined inheritance subtype of Address.
- [SupplierAddress](src/main/java/verkkis/demo/model/SupplierAddress.java)
  - Joined inheritance subtype of Address.

## Database details that are not visible from the API

The schema is generated by JPA/Hibernate from the entity mappings. Important details:

- `suppliers.email` is unique.
- Primary keys and foreign keys are generated from the JPA annotations.
- `Address` uses joined inheritance, so the `addresses` table is the base table and subtype data lives in `customer_addresses` and `supplier_addresses`.
- `products` has both a direct supplier foreign key and a category foreign key, plus a many-to-many join table `product_category_mapping`.
- `orders` references `customers` and `customer_addresses`.
- `contacts` references `customers`.
- `orderitems` references `orders` and `products`.

What is not present right now:

- No explicit database indexes are declared in the code.
- No database views are defined.
- No stored procedures are defined.
- No Spring Security configuration is defined.
- No authentication or authorization is enforced.

## Hidden behavior and constraints

- Customer email values are normalized to lowercase by `LowercaseEmailConverter`.
- `Order` sets `status` to `NEW` and `orderDate` to the current time if they are missing during persistence.
- `ProductRepositoryTest` and `SupplierProductsScenarioTest` verify repository behavior around supplier-linked products.
- `OrderCriteriaDeleteTest` verifies the custom criteria delete for old cancelled orders.
- `InheritanceTest` verifies the joined inheritance address model.

## Configuration

Application settings live in [src/main/resources/application.yaml](src/main/resources/application.yaml).

- Spring Boot runs on port `8081`.
- The application connects to MariaDB by default.
- Hibernate SQL logging is enabled.
- The schema validation mode is enabled through JPA properties.

## Tests

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

## Notes on completeness

The project is functionally complete for the supplier REST resource and for the repository-level domain model that the tests exercise. It is not a full CRUD API for every entity yet, because only suppliers are exposed through HTTP controllers.

If you want a broader HTTP API, the next logical additions would be controllers for products, customers, and orders.
