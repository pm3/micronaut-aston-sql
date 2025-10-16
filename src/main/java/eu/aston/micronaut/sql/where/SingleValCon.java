/*
 * Created on Sep 13, 2005
 *
 */
package eu.aston.micronaut.sql.where;

import java.util.List;

/**
 * @author pm
 */
public class SingleValCon implements ICondition {
    private final String field;
    private final String expr;
    private final Object value;

    public SingleValCon(String field, String exp, Object value) {
        this.field = field;
        this.expr = exp;
        this.value = value;
    }

    @Override
    public boolean render(StringBuilder sql, List<Object> params) {
        if (value != null) {
            sql.append(field).append(expr).append("?");
            params.add(value);
            return true;
        }
        return false;
    }
}
