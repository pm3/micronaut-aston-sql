package eu.aston.micronaut.sql.convert;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class OptionalConverter implements IConverter {

    IConverter wrap;

    public OptionalConverter(IConverter wrap) {
        this.wrap = wrap;
    }

    @Override
    public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
        if (val instanceof Optional oval) wrap.fillPs(ps, pos, oval.orElse(null));
        else wrap.fillPs(ps, pos, val);
    }

    @Override
    public Object readRs(ResultSet rs, int pos) throws SQLException {
        return Optional.ofNullable(wrap.readRs(rs, pos));
    }

    @Override
    public Object readRs(ResultSet rs, String name) throws SQLException {
        return Optional.ofNullable(wrap.readRs(rs, name));
    }

    @Override
    public Object readCs(CallableStatement cs, int pos) throws SQLException {
        return Optional.ofNullable(wrap.readCs(cs, pos));
    }
}
