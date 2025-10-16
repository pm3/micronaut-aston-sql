package eu.aston.micronaut.sql.cmd;

import java.sql.SQLException;

public class SqlParams {
    final private String sql;
    final private Object[] params;

    public SqlParams(String sql, Object[] params) {
        this.sql = sql;
        this.params = params;
    }

    public String getSql() {
        return sql;
    }

    public Object[] getParams() {
        return params;
    }

    public SqlParams dynamic(Object[] args) throws SQLException {
        return this;
    }
}
