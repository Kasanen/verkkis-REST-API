package verkkis;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import verkkis.demo.converter.LowercaseEmailConverter;

class LowercaseEmailConverterTest {

    private final LowercaseEmailConverter converter = new LowercaseEmailConverter();

    @Test
    void convertsToLowercaseAndTrims() {
        assertThat(converter.convertToDatabaseColumn("  Matti@Example.COM "))
            .isEqualTo("matti@example.com");
    }

    @Test
    void nullStaysNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void readingDoesNotChangeValue() {
        assertThat(converter.convertToEntityAttribute("matti@example.com"))
            .isEqualTo("matti@example.com");
    }
}