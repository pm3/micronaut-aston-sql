package eu.aston.micronaut.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface IResult<T> {
    T result(ResultSet rs) throws SQLException;
}
