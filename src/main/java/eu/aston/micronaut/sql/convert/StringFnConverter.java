package eu.aston.micronaut.sql.convert;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

public class StringFnConverter implements IConverter {
    private final Function<String, Object> fromString;
    private final Function<Object, String> toString;

    public StringFnConverter(Function<String, Object> fromString, Function<Object, String> toString) {
        this.fromString = fromString;
        this.toString = toString;
    }

    @Override
    public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
        ps.setString(pos, toString.apply(val));
    }

    @Override
    public Object readRs(ResultSet rs, int pos) throws SQLException {
        return fromString.apply(rs.getString(pos));
    }

    @Override
    public Object readRs(ResultSet rs, String name) throws SQLException {
        return fromString.apply(rs.getString(name));
    }

    @Override
    public Object readCs(CallableStatement cs, int pos) throws SQLException {
        return fromString.apply(cs.getString(pos));
    }
}
