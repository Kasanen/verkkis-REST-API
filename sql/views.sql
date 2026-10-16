-- 02_views.sql
-- Views for the verkkis database. CREATE OR REPLACE makes the script re-runnable.
USE verkkis;

-- Product list with category and supplier details.
-- LEFT JOINs keep products whose category or supplier is NULL.
-- Example: SELECT * FROM product_overview WHERE stock_quantity < 10;
CREATE OR REPLACE VIEW product_overview AS
SELECT
    p.id             AS product_id,
    p.name           AS product_name,
    p.price,
    p.stock_quantity,
    pc.name          AS category_name,
    s.name           AS supplier_name,
    s.email          AS supplier_email
FROM products p
LEFT JOIN productcategories pc ON p.category_id = pc.id
LEFT JOIN suppliers s          ON p.supplier_id = s.id;

-- Monthly sales summary. Cancelled orders are excluded, so the business rule
-- lives in one place.
-- Example: SELECT * FROM monthly_sales_summary WHERE sales_month = '2024-06';
CREATE OR REPLACE VIEW monthly_sales_summary AS
SELECT
    DATE_FORMAT(o.order_date, '%Y-%m')  AS sales_month,
    COUNT(DISTINCT o.id)                AS total_orders,
    SUM(oi.quantity)                    AS total_items_sold,
    SUM(oi.quantity * oi.unit_price)    AS total_revenue
FROM orders o
JOIN orderitems oi ON o.id = oi.order_id
WHERE o.status != 'CANCELLED'
GROUP BY DATE_FORMAT(o.order_date, '%Y-%m');