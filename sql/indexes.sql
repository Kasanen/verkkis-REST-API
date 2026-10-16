-- 01_indexes.sql
-- Indexes for the verkkis database. Safe to re-run (IF NOT EXISTS).
USE verkkis;

-- Date range / sorting of orders
-- Query: SELECT id, customer_id, order_date, status FROM orders
--        WHERE order_date >= '2024-01-01' AND order_date < '2024-02-01';
CREATE INDEX IF NOT EXISTS idx_orders_order_date ON orders(order_date);

-- Filtering orders by status
-- Query: SELECT id, customer_id, order_date, status FROM orders WHERE status = 'NEW';
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

-- Exact-match lookup of customers by email (unique)
-- Query: SELECT * FROM customers WHERE email = 'dana8235059@example.com';
CREATE UNIQUE INDEX IF NOT EXISTS idx_customers_email ON customers(email);

-- Exact-match lookup of suppliers by email (unique)
-- Query: SELECT * FROM suppliers WHERE email = 'mia.manninen@polarelec.fi';
CREATE UNIQUE INDEX IF NOT EXISTS idx_suppliers_email ON suppliers(email);

-- Verify with EXPLAIN. Observed in testing:
--  * email lookups use the unique indexes (type = const, rows = 1)
--  * orders.status and orders.order_date queries that match a large share of
--    the ~200k rows are still answered with a full scan (optimizer judges the
--    index not worthwhile at low selectivity); a narrow date range benefits.
-- mysqlslap (20 clients, 100 iterations): avg 0.560 s before, 0.349 s after.