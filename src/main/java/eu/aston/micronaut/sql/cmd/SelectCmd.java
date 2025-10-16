package eu.aston.micronaut.sql.cmd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import eu.aston.micronaut.sql.IResult;
import eu.aston.micronaut.sql.ISqlCmd;
import eu.aston.micronaut.sql.convert.IConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectCmd<R> implements ISqlCmd<R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISqlCmd.class);
    private final IResult<R> result;
    private final SqlParams sqlParams;

    public SelectCmd(IResult<R> result, SqlParams sqlParams) {
        this.result = result;
        this.sqlParams = sqlParams;
    }

    public SelectCmd(IResult<R> result, String sql, Object[] params) {
        this(result, new SqlParams(sql, params));
    }

    @Override
    @SuppressWarnings("unchecked")
    public R exec(IConverterFactory cf, Connection c, Object[] args) throws SQLException {
        IResult<R> result0 = result;
        if (result0 == null && args[0] instanceof IResult) result0 = (IResult<R>) args[0];
        if (result0 == null) throw new SQLException("null result first param");
        SqlParams sqlParams0 = sqlParams.dynamic(args);
        long l1 = System.currentTimeMillis();
        try {
            try (PreparedStatement ps = c.prepareStatement(sqlParams0.getSql())) {
                UpdateCmd.fillPs(cf, ps, sqlParams0.getParams(), args);
                try (ResultSet rs = ps.executeQuery()) {
                    return result0.result(rs);
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
    }

    @Override
    public String toString() {
        return sqlParams.getSql();
    }

}
