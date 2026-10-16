package verkkis.demo.model;

import java.util.Set;

/**
 * Tilauksen tilat ja sallitut siirtymät.
 * Tietokannassa status on VARCHAR, joten arvo tallennetaan nimenä (esim. "NEW").
 *
 * NEW -> PAID -> SHIPPED -> DELIVERED
 * NEW / PAID -> CANCELLED
 */
public enum OrderStatus {
    NEW,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canMoveTo(OrderStatus next) {
        return switch (this) {
            case NEW -> Set.of(PAID, CANCELLED).contains(next);
            case PAID -> Set.of(SHIPPED, CANCELLED).contains(next);
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    public boolean isCancellable() {
        return canMoveTo(CANCELLED);
    }

    public static OrderStatus parse(String value) {
        try {
            return OrderStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
