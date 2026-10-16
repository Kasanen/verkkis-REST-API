-- 05_temporal.sql
-- Temporal (history) support for the verkkis database using MariaDB
-- system-versioned tables. MariaDB keeps every old row version and fills
-- ROW_START / ROW_END automatically (transaction time).
USE verkkis;

-- Run ONCE. It fails if orders is already versioned; check first with:
--   SHOW CREATE TABLE orders;   -- look for WITH SYSTEM VERSIONING
ALTER TABLE orders ADD SYSTEM VERSIONING;

-- Existing rows are kept as they are. History starts from this moment.

-- Demo: change a status twice and read the full history
--   UPDATE orders SET status = 'SHIPPED'   WHERE id = 1;
--   UPDATE orders SET status = 'DELIVERED' WHERE id = 1;
--
--   SELECT id, status, order_date, ROW_START, ROW_END
--   FROM orders FOR SYSTEM_TIME ALL
--   WHERE id = 1
--   ORDER BY ROW_START;
-- Result: three versions (CANCELLED -> SHIPPED -> DELIVERED). The current
-- version has ROW_END = 2038-01-19 05:14:07.999999.

-- Time travel: the state of an order at a given moment
--   SELECT id, status FROM orders
--   FOR SYSTEM_TIME AS OF '2026-09-16 13:46:00'
--   WHERE id = 1;

-- Used by the API for GET /orders/{id}/history:
--   SELECT id, status, ROW_START AS valid_from, ROW_END AS valid_to
--   FROM orders FOR SYSTEM_TIME ALL
--   WHERE id = ?
--   ORDER BY ROW_START;

-- Undo (also discards the stored history):
--   ALTER TABLE orders DROP SYSTEM VERSIONING;