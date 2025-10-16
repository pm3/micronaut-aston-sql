package eu.aston.micronaut.sql;

import java.lang.reflect.Type;
import java.sql.SQLException;

import eu.aston.micronaut.sql.convert.IConverter;

public interface ISqlExpr {
    Object parse(Object[] args) throws SQLException;

    Type type();

    IConverter converter();
}
