package verkkis.demo.dto;

import java.time.LocalDateTime;

public record OrderHistoryEntry(
        Integer id,
        String status,
        LocalDateTime changedAt) {
}
