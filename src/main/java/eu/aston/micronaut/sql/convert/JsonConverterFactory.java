package eu.aston.micronaut.sql.convert;

import java.lang.reflect.Type;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.annotation.Order;
import jakarta.inject.Singleton;

@Singleton
@Order(103)
public class JsonConverterFactory implements IConverterFactory {

    public static final String JSON = "json";

    private final ObjectMapper mapper;

    public JsonConverterFactory(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public IConverter converter(Type type, String name) throws SQLException {
        if (JSON.equals(name)) {
            JavaType javaType = mapper.constructType(type);
            return new StringFnConverter((str) -> toJson(mapper, javaType, str), val -> toString(mapper, val));
        }
        return null;
    }

    public static Object toJson(ObjectMapper mapper, JavaType type, String str) {
        try {
            return str != null ? mapper.readValue(str, type) : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("parse json error " + e.getMessage(), e);
        }
    }

    public static String toString(ObjectMapper mapper, Object val) {
        try {
            return val != null ? mapper.writeValueAsString(val) : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("stringify json error " + e.getMessage(), e);
        }
    }
}
