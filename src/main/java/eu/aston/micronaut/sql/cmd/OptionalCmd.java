package eu.aston.micronaut.sql.cmd;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import eu.aston.micronaut.sql.ISqlCmd;
import eu.aston.micronaut.sql.convert.IConverterFactory;

public class OptionalCmd<T> implements ISqlCmd<Optional<T>> {

    private final ISqlCmd<T> wrap;

    public OptionalCmd(ISqlCmd<T> wrap) {
        this.wrap = wrap;
    }

    @Override
    public Optional<T> exec(IConverterFactory cf, Connection c, Object[] args) throws SQLException {
        return Optional.ofNullable(wrap.exec(cf, c, args));
    }
}
