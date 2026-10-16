# Verkkis API

## Demo project documentation

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

## Installation and Setup

## Project Structure

## Database features

The `verkkis` MariaDB schema is managed with the scripts. These parts are invisible in the API but are part of the solution.

| Feature | Implementation | Purpose |
|---|---|---|
| **Indexes** | `idx_orders_order_date`, `idx_orders_status`; unique `idx_customers_email`, `idx_suppliers_email` | Faster date and status queries and email lookups; unique emails prevent duplicates. Effect checked with `EXPLAIN` and `mysqlslap`. The optimizer skips an index when a query matches a large share of the table. |
| **Views** | `product_overview`, `monthly_sales_summary` | `product_overview` joins products with category and supplier for the web-shop product list. `monthly_sales_summary` gives orders, items and revenue per month, excluding cancelled orders. |
| **Triggers** | `trg_orderitems_before_insert`, `trg_orderitems_after_insert` | The BEFORE trigger rejects an order line if stock is insufficient (`SQLSTATE 45000`). The AFTER trigger reduces `products.stock_quantity`. The application must not reduce stock itself. |
| **Event** | `evt_clean_old_cancelled_orders`, runs daily | Deletes `CANCELLED` orders older than 90 days. Requires `event_scheduler=ON`. Tested on a copy table. |
| **Temporal** | `ALTER TABLE orders ADD SYSTEM VERSIONING` | Keeps every version of an order (transaction time), so status history can be queried with `FOR SYSTEM_TIME ALL` / `AS OF` and is served by `GET /orders/{id}/history`. |
| **Transactions** | Order creation in one transaction | Order, order lines and stock changes are saved together or not at all. |

## API Documentation

## Security

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