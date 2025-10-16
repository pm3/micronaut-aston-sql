package eu.aston.micronaut.sql.convert;

import java.lang.reflect.Type;
import java.sql.SQLException;

@FunctionalInterface
public interface IConverterFactory {
    IConverter converter(Type type, String name) throws SQLException;
}
