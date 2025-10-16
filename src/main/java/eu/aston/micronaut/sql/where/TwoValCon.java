package eu.aston.micronaut.sql.where;

import java.util.List;

public class TwoValCon implements ICondition {
    private final Object value1;
    private final String expr;
    private final Object value2;

    public TwoValCon(Object value1, String exp, Object value2) {
        this.value1 = value1;
        this.expr = exp;
        this.value2 = value2;
    }

    @Override
    public boolean render(StringBuilder sql, List<Object> params) {
        sql.append("? ").append(expr).append(" ?");
        params.add(value1);
        params.add(value2);
        return true;
    }
}
