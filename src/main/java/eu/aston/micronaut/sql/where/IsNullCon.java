/*
 * Created on Sep 13, 2005
 *
 */
package eu.aston.micronaut.sql.where;

import java.util.List;

/**
 * @author pm
 */
public class IsNullCon implements ICondition {
    private final String field;
    private final boolean negate;

    public IsNullCon(String field, boolean negate) {
        this.field = field;
        this.negate = negate;
    }

    @Override
    public boolean render(StringBuilder sql, List<Object> params) {
        sql.append(field);
        if (!negate)
            sql.append(" is null");
        else
            sql.append(" is not null");
        return true;
    }
}
