/*
 * Created on Sep 13, 2005
 *
 */
package eu.aston.micronaut.sql.where;

import java.util.List;

/**
 * @author pm
 */
public class TwoFieldCon implements ICondition {
    private final String field1;
    private final String expr;
    private final String field2;

    public TwoFieldCon(String field1, String exp, String field2) {
        this.field1 = field1;
        this.expr = exp;
        this.field2 = field2;
    }

    @Override
    public boolean render(StringBuilder sql, List<Object> params) {
        sql.append(field1).append(expr).append(field2);
        return true;
    }
}
