package verkkis.demo.dto;

import java.math.BigDecimal;

/** Alennus prosentteina, esim. 15 = -15 %. */
public record DiscountRequest(BigDecimal percent) {
}
