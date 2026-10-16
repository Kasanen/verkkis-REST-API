package verkkis.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import verkkis.demo.dto.LowStockProduct;
import verkkis.demo.dto.MonthlySales;
import verkkis.demo.dto.TopCustomer;
import verkkis.demo.dto.TopProduct;
import verkkis.demo.exception.BadRequestException;

/**
 * Raportit luetaan suoraan tietokannasta SQL:llä. Kaksi ensimmäistä käyttävät
 * tietokannan näkymiä (monthly_sales_summary, products_list), loput yhdistävät
 * useita tauluja (orders, orderitems, products, productcategories, customers).
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final int MAX_LIMIT = 100;

    private final JdbcTemplate jdbc;

    public ReportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Näkymä monthly_sales_summary: kuukausittainen myynti ilman peruttuja tilauksia. */
    public List<MonthlySales> monthlySales() {
        return jdbc.query("""
                SELECT sales_month, total_orders, total_items_sold, total_revenue
                FROM monthly_sales_summary
                ORDER BY sales_month DESC
                """,
                (rs, i) -> new MonthlySales(rs.getString("sales_month"), rs.getLong("total_orders"),
                        rs.getLong("total_items_sold"), rs.getBigDecimal("total_revenue")));
    }

    /** Näkymä products_list: tuotteet, joiden saldo on enintään threshold. */
    public List<LowStockProduct> lowStock(int threshold, int limit) {
        if (threshold < 0) {
            throw new BadRequestException("threshold must be zero or positive");
        }
        return jdbc.query("""
                SELECT product_id, product_name, price, stock_quantity, category_name, supplier_name, supplier_email
                FROM products_list
                WHERE stock_quantity <= ?
                ORDER BY stock_quantity, product_id
                LIMIT ?
                """,
                (rs, i) -> new LowStockProduct(rs.getInt("product_id"), rs.getString("product_name"),
                        rs.getBigDecimal("price"), rs.getInt("stock_quantity"), rs.getString("category_name"),
                        rs.getString("supplier_name"), rs.getString("supplier_email")),
                threshold, checkLimit(limit));
    }

    /** Myydyimmät tuotteet valinnaisella aikavälillä [from, to]. */
    public List<TopProduct> topProducts(int limit, LocalDate from, LocalDate to) {
        checkRange(from, to);
        return jdbc.query("""
                SELECT p.id, p.name, pc.name AS category_name,
                       SUM(oi.quantity) AS units_sold,
                       COUNT(DISTINCT o.id) AS order_count,
                       SUM(oi.quantity * oi.unit_price) AS revenue
                FROM orderitems oi
                JOIN orders o ON o.id = oi.order_id
                JOIN products p ON p.id = oi.product_id
                LEFT JOIN productcategories pc ON pc.id = p.category_id
                WHERE o.status <> 'CANCELLED'
                  AND (? IS NULL OR o.order_date >= ?)
                  AND (? IS NULL OR o.order_date < ? + INTERVAL 1 DAY)
                GROUP BY p.id, p.name, pc.name
                ORDER BY units_sold DESC, revenue DESC
                LIMIT ?
                """,
                (rs, i) -> new TopProduct(rs.getInt("id"), rs.getString("name"), rs.getString("category_name"),
                        rs.getLong("units_sold"), rs.getLong("order_count"), rs.getBigDecimal("revenue")),
                from, from, to, to, checkLimit(limit));
    }

    /** Eniten ostaneet asiakkaat. */
    public List<TopCustomer> topCustomers(int limit) {
        return jdbc.query("""
                SELECT c.id, CONCAT(c.first_name, ' ', c.last_name) AS name, c.email,
                       COUNT(DISTINCT o.id) AS order_count,
                       SUM(oi.quantity * oi.unit_price) AS total_spent
                FROM customers c
                JOIN orders o ON o.customer_id = c.id
                JOIN orderitems oi ON oi.order_id = o.id
                WHERE o.status <> 'CANCELLED'
                GROUP BY c.id, c.first_name, c.last_name, c.email
                ORDER BY total_spent DESC
                LIMIT ?
                """,
                (rs, i) -> new TopCustomer(rs.getInt("id"), rs.getString("name"), rs.getString("email"),
                        rs.getLong("order_count"), rs.getBigDecimal("total_spent")),
                checkLimit(limit));
    }

    private int checkLimit(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BadRequestException("limit must be between 1 and " + MAX_LIMIT);
        }
        return limit;
    }

    private void checkRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("from must be before to");
        }
    }
}
