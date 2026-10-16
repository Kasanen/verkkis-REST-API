package verkkis.demo.exception;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Pienet syötetarkistukset, jotka heittävät BadRequestExceptionin. */
public final class Validation {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validation() {
    }

    public static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(field + " is required");
        }
        return optionalText(value, field, maxLength);
    }

    public static String optionalText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new BadRequestException(field + " must be at most " + maxLength + " characters");
        }
        return trimmed;
    }

    public static String requireEmail(String value, String field) {
        String email = requireText(value, field, 255);
        if (!EMAIL.matcher(email).matches()) {
            throw new BadRequestException(field + " is not a valid email address");
        }
        return email.toLowerCase();
    }

    public static String optionalEmail(String value, String field) {
        return value == null || value.isBlank() ? null : requireEmail(value, field);
    }

    public static BigDecimal requirePrice(BigDecimal value, String field) {
        if (value == null) {
            throw new BadRequestException(field + " is required");
        }
        if (value.signum() < 0 || value.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new BadRequestException(field + " must be between 0 and 99999999.99");
        }
        if (value.stripTrailingZeros().scale() > 2) {
            throw new BadRequestException(field + " can have at most 2 decimals");
        }
        return value;
    }

    public static int requireNonNegative(Integer value, String field) {
        if (value == null || value < 0) {
            throw new BadRequestException(field + " must be zero or positive");
        }
        return value;
    }

    public static int requirePositive(Integer value, String field) {
        if (value == null || value <= 0) {
            throw new BadRequestException(field + " must be a positive number");
        }
        return value;
    }
}
