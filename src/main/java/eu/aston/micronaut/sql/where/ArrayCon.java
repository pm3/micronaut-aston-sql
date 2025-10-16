package eu.aston.micronaut.sql.where;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ArrayCon implements ICondition {
    private final String field;
    private final String expr;
    private final Iterator<Object> iterator;

    public ArrayCon(String field, String expr, Object[] values) {
        this.field = field;
        this.expr = expr;
        this.iterator = values != null && values.length > 0 ? Arrays.stream(values).iterator() : null;
    }

    public ArrayCon(String field, String expr, Collection<Object> values) {
        this.field = field;
        this.expr = expr;
        this.iterator = values != null && !values.isEmpty() ? values.iterator() : null;
    }

    @Override
    public boolean render(StringBuilder sql, List<Object> params) {
        if (iterator != null) {
            sql.append(field).append(" ").append(expr).append("(");
            while (iterator.hasNext()) {
                sql.append("?");
                params.add(iterator.next());
                if (iterator.hasNext()) sql.append(", ");
            }
            sql.append(")");
            return true;
        }
        return false;
    }
}
