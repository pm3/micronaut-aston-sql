package eu.aston.micronaut.sql.where;

import java.util.List;

public class BetweenFieldsCon implements ICondition {
    private final String field1;
    private final String field2;
    private final Object value;

    public BetweenFieldsCon(String field1, String field2, Object value) {
        this.field1 = field1;
        this.field2 = field2;
        this.value = value;
    }

    @Override
    public boolean render(StringBuilder sql, List<Object> params) {
        if (value != null) {
            sql.append("(").append("?").append(" ").append("between ").append(field1).append(" and ").append(field2).append(")");
            params.add(value);
            return true;
        }
        return false;
    }
}
