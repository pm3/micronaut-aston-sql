package eu.aston.micronaut.sql.parse;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eu.aston.micronaut.sql.cmd.SqlParams;
import eu.aston.micronaut.sql.where.ICondition;
import eu.aston.micronaut.sql.where.Multi;

public class DynSqlParams extends SqlParams {
    private final List<Object> _items;

    public DynSqlParams(String sql, List<Object> items) {
        super(sql, null);
        this._items = items;
    }

    @Override
    public SqlParams dynamic(Object[] args) throws SQLException {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        exec(sql, params, args, _items);
        return new SqlParams(sql.toString(), params.toArray(new Object[0]));
    }

    public SqlParams toStatic() throws SQLException {
        boolean isStatic = true;
        for (Object o1 : _items) {
            if (o1 instanceof DynSqlParser.DSblock) isStatic = false;
            if (o1 instanceof DynSqlParser.DSparam p && p.condition) isStatic = false;
        }
        if (!isStatic) throw new SQLException("isn't static");
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        exec(sql, params, new Object[20], _items);
        return new SqlParams(sql.toString(), params.toArray(new Object[0]));
    }

    protected void exec(StringBuilder sql, List<Object> params, Object[] args, List<Object> items) throws SQLException {
        for (Object o1 : items) {
            if (o1 instanceof String) {
                sql.append(o1);
            } else if (o1 instanceof DynSqlParser.DSparam) {
                execParam(sql, params, args, (DynSqlParser.DSparam) o1);
            } else if (o1 instanceof DynSqlParser.DSblock) {
                execBlock(sql, params, args, (DynSqlParser.DSblock) o1);
            }
        }
    }

    protected void execParam(StringBuilder sql, List<Object> params, Object[] args, DynSqlParser.DSparam p) throws SQLException {
        Object v = p.expr.parse(args);
        if (p.condition) {
            if (v instanceof Multi<?> m) {
                m.render(sql, params);
                return;
            } else if (v instanceof ICondition condition) {
                if (condition.render(sql, params)) return;
            }
            //null condition or empty condition render positive condition
            sql.append("1=1");
            return;
        }
        sql.append("?");
        params.add(p.expr);
    }

    protected void execBlock(StringBuilder sql, List<Object> params, Object[] args, DynSqlParser.DSblock b) throws
            SQLException {
        for (Object o2 : b.items) {
            if (o2 instanceof DynSqlParser.DSparam p) {
                Object v = p.expr.parse(args);
                if (v == null)
                    return;
            }
        }
        exec(sql, params, args, b.items);
    }
}
