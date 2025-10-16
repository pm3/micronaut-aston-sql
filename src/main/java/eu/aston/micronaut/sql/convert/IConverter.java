package eu.aston.micronaut.sql.convert;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface IConverter {
    void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException;

    Object readRs(ResultSet rs, int pos) throws SQLException;

    Object readRs(ResultSet rs, String name) throws SQLException;

    Object readCs(CallableStatement cs, int pos) throws SQLException;
}
