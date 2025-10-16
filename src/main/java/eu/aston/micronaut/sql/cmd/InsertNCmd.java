package eu.aston.micronaut.sql.cmd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import eu.aston.micronaut.sql.ISqlCmd;
import eu.aston.micronaut.sql.convert.IConverter;
import eu.aston.micronaut.sql.convert.IConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertNCmd implements ISqlCmd<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISqlCmd.class);
    final private IConverter converter;
    final private String namesId;
    final private SqlParams sqlParams;

    public InsertNCmd(IConverter converter, String namesId, SqlParams sqlParams) {
        this.converter = converter;
        this.namesId = namesId;
        this.sqlParams = sqlParams;
    }

    public InsertNCmd(IConverter converter, String namesId, String sql, Object[] params) {
        this(converter, namesId, new SqlParams(sql, params));
    }

    @Override
    public Object exec(IConverterFactory cf, Connection c, Object[] args) throws SQLException {
        SqlParams sqlParams0 = sqlParams.dynamic(args);
        Object oid = null;
        long l1 = System.currentTimeMillis();
        try {
            try (PreparedStatement ps = c.prepareStatement(sqlParams0.getSql(), namesId.split(","))) {
                UpdateCmd.fillPs(cf, ps, sqlParams0.getParams(), args);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next())
                        oid = converter != null ? converter.readRs(rs, 1) : rs.getObject(1);
                }
            }
        } finally {
            long l2 = System.currentTimeMillis();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("{}; params {} args {} time {} ms",
                        sqlParams0.getSql(),
                        Arrays.toString(sqlParams0.getParams()),
                        Arrays.toString(args),
                        (l2 - l1));
        }
        return oid;
    }

    @Override
    public String toString() {
        return sqlParams.getSql();
    }
}
