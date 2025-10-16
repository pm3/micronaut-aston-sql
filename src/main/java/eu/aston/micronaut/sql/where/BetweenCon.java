/*
 * Created on Sep 13, 2005
 *
 */
package eu.aston.micronaut.sql.where;

import java.util.List;

/**
 * @author pm
 */
public class BetweenCon implements ICondition {
    private final String field;
    private final Object value1;
    private final Object value2;
    private final boolean negate;

    public BetweenCon(String field, Object value1, Object value2, boolean negate) {
        this.field = field;
        this.value1 = value1;
        this.value2 = value2;
        this.negate = negate;
    }

    @Override
    public boolean render(StringBuilder sql, List<Object> params) {

        if (value1 != null && value2 != null) {
            sql.append(field);
            if (negate)
                sql.append(" not");
            sql.append(" between ? and ?");
            params.add(value1);
            params.add(value2);
            return true;
        }
        if (value1 != null) {
            sql.append(field).append(">=?");
            params.add(value1);
            return true;
        }
        if (value2 != null) {
            sql.append(field).append("<=?");
            params.add(value2);
            return true;
        }
        return false;
    }
}
