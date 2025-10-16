package eu.aston.micronaut.sql;

import java.sql.Connection;
import java.sql.SQLException;

import eu.aston.micronaut.sql.convert.IConverterFactory;

@FunctionalInterface
public interface ISqlCmd<R> {
    R exec(IConverterFactory cf, Connection c, Object[] args) throws SQLException;
}
