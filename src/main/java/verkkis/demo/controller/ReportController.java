package verkkis.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import verkkis.demo.dto.LowStockProduct;
import verkkis.demo.dto.MonthlySales;
import verkkis.demo.dto.TopCustomer;
import verkkis.demo.dto.TopProduct;
import verkkis.demo.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // GET /api/reports/monthly-sales — näkymä monthly_sales_summary
    @GetMapping("/monthly-sales")
    public List<MonthlySales> monthlySales() {
        return reportService.monthlySales();
    }

    // GET /api/reports/low-stock?threshold=5&limit=20 — näkymä products_list
    @GetMapping("/low-stock")
    public List<LowStockProduct> lowStock(@RequestParam(defaultValue = "5") int threshold,
            @RequestParam(defaultValue = "20") int limit) {
        return reportService.lowStock(threshold, limit);
    }

    // GET /api/reports/top-products?limit=10&from=2026-01-01&to=2026-06-30
    @GetMapping("/top-products")
    public List<TopProduct> topProducts(@RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.topProducts(limit, from, to);
    }

    // GET /api/reports/top-customers?limit=10
    @GetMapping("/top-customers")
    public List<TopCustomer> topCustomers(@RequestParam(defaultValue = "10") int limit) {
        return reportService.topCustomers(limit);
    }
}
