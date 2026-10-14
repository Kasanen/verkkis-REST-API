package verkkis.demo.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter  
public class LowercaseEmailConverter implements AttributeConverter<String, String> {

    // tallennus
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : attribute.trim().toLowerCase();
    }

    // lukeminen
    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData;
    }
}