package eu.aston.micronaut.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface IRow<T> {
    T row(ResultSet rs) throws SQLException;
}
