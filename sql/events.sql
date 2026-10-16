-- 04_events.sql
-- Scheduled events for the verkkis database.
USE verkkis;

-- Events only run while the scheduler is on. Also set event_scheduler=ON in
-- the server config so it survives a restart.
SET GLOBAL event_scheduler = ON;

DROP EVENT IF EXISTS evt_clean_old_cancelled_orders;

DELIMITER //

-- Daily cleanup of cancelled orders older than 90 days.
-- WARNING: the sample data is dated 2024, so the first run would match tens of
-- thousands of rows (66,707 in testing). Orders that still have orderitems
-- fail on the foreign key unless it is ON DELETE CASCADE. Run the SELECT below
-- first, and consider keeping the event DISABLEd until you are ready.
CREATE EVENT evt_clean_old_cancelled_orders
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP + INTERVAL 1 DAY
ON COMPLETION PRESERVE
ENABLE
DO
BEGIN
    DELETE FROM orders
    WHERE status = 'CANCELLED'
      AND order_date < NOW() - INTERVAL 90 DAY;
END //

DELIMITER ;

-- Dry run of what the event would delete:
--   SELECT COUNT(*) FROM orders
--   WHERE status = 'CANCELLED' AND order_date < NOW() - INTERVAL 90 DAY;

-- Testing: the event was verified on a copy table (orders_test) by shortening
-- the schedule to EVERY 1 MINUTE and checking that only the old CANCELLED row
-- disappeared. See the learning diary for the screenshots.
--   ALTER EVENT evt_clean_old_cancelled_orders DISABLE;
--   ALTER EVENT evt_clean_old_cancelled_orders ENABLE;
--   SELECT event_name, status, last_executed FROM information_schema.EVENTS
--   WHERE event_schema = 'verkkis';