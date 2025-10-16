package eu.aston.micronaut.sql.cmd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import eu.aston.micronaut.sql.ISqlCmd;
import eu.aston.micronaut.sql.ISqlExpr;
import eu.aston.micronaut.sql.convert.IConverter;
import eu.aston.micronaut.sql.convert.IConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateCmd implements ISqlCmd<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISqlCmd.class);
    private final SqlParams sqlParams;

    public UpdateCmd(SqlParams sqlParams) {
        this.sqlParams = sqlParams;
    }

    public UpdateCmd(String sql, Object[] params) {
        this(new SqlParams(sql, params));
    }

    @Override
    public Integer exec(IConverterFactory cf, Connection c, Object[] args) throws SQLException {
        SqlParams sqlParams0 = sqlParams.dynamic(args);
        long l1 = System.currentTimeMillis();
        try {
            try (PreparedStatement ps = c.prepareStatement(sqlParams0.getSql())) {
                UpdateCmd.fillPs(cf, ps, sqlParams0.getParams(), args);
                return ps.executeUpdate();
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
    }

    @Override
    public String toString() {
        return sqlParams.getSql();
    }

    public static void fillPs(IConverterFactory cf, PreparedStatement ps, Object[] params, Object[] args) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                Object v = params[i];
                if (v instanceof ISqlExpr expr) {
                    v = expr.parse(args);
                    if (expr.converter() != null) {
                        expr.converter().fillPs(ps, i + 1, v);
                        continue;
                    }
                }
                IConverter c = v != null ? cf.converter(v.getClass(), null) : null;
                if (c != null) {
                    c.fillPs(ps, i + 1, v);
                } else {
                    ps.setObject(i + 1, v);
                }
            }
        }
    }
}
