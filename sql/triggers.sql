-- 03_triggers.sql
-- Triggers for the verkkis database. Run with the mariadb client so that
-- DELIMITER is understood.
USE verkkis;

DROP TRIGGER IF EXISTS trg_orderitems_before_insert;
DROP TRIGGER IF EXISTS trg_orderitems_after_insert;

DELIMITER //

-- Refuse an order line when there is not enough stock.
-- The row is only checked here; the stock is reduced in the AFTER trigger.
CREATE TRIGGER trg_orderitems_before_insert
BEFORE INSERT ON orderitems
FOR EACH ROW
BEGIN
    DECLARE current_stock INT;
    SELECT stock_quantity INTO current_stock
    FROM products WHERE id = NEW.product_id;

    IF current_stock IS NULL OR current_stock < NEW.quantity THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Not enough stock for this order line';
    END IF;
END //

-- Reduce stock automatically when an order line is added.
-- NOTE: the application must NOT reduce stock again, or it drops twice.
CREATE TRIGGER trg_orderitems_after_insert
AFTER INSERT ON orderitems
FOR EACH ROW
BEGIN
    UPDATE products
    SET stock_quantity = stock_quantity - NEW.quantity
    WHERE id = NEW.product_id;
END //

DELIMITER ;

-- Test (run inside a transaction and roll back):
--   START TRANSACTION;
--   SELECT stock_quantity FROM products WHERE id = 101;
--   INSERT INTO orders (customer_id, order_date, shipping_address_id, status)
--     VALUES (5, NOW(), 12, 'NEW');
--   INSERT INTO orderitems (order_id, product_id, quantity, unit_price)
--     VALUES (LAST_INSERT_ID(), 101, 3, 19.99);
--   SELECT stock_quantity FROM products WHERE id = 101;   -- reduced by 3
--   ROLLBACK;                                             -- restored