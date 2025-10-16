package eu.aston.micronaut.sql.convert;

import java.lang.reflect.Type;
import java.sql.SQLException;

import io.micronaut.core.annotation.Order;
import jakarta.inject.Singleton;

@Singleton
@Order(102)
public class EnumConverterFactory implements IConverterFactory {

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public IConverter converter(Type type, String name) throws SQLException {
        if (type instanceof Class<?> cl && cl.isEnum()) {
            return new StringFnConverter(s -> toEnum((Class<? extends Enum>) cl, s), EnumConverterFactory::toString);
        }
        return null;
    }

    public static <T extends Enum<T>> Enum<T> toEnum(Class<T> type, String s) {
        if (s != null) {
            return Enum.valueOf(type, s);
        }
        return null;
    }

    public static String toString(Object val) {
        if (val instanceof Enum<?> e) {
            return e.name();
        }
        return null;
    }
}
