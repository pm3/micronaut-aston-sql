package eu.aston.micronaut.sql.where;

import java.util.Collections;
import java.util.List;

public class SubQueryCon implements ICondition {
    private final String field;
    private final String expr;
    private final String subQuery;
    private final Object[] values;

    public SubQueryCon(String field, String expr, String subQuery, Object... values) {
        this.field = field;
        this.expr = expr;
        this.subQuery = subQuery;
        this.values = values;
    }

    @Override
    public boolean render(StringBuilder sql, List<Object> params) {
        if (field != null) sql.append(field);
        sql.append(" ").append(expr).append(" ");
        sql.append("(").append(subQuery).append(")");
        Collections.addAll(params, values);
        return true;
    }
}
