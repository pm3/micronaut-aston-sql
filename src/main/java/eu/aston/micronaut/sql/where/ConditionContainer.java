/*
 * Created on Sep 13, 2005
 *
 */
package eu.aston.micronaut.sql.where;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author pm
 */
public class ConditionContainer implements ICondition {
    String op;
    List<ICondition> items;

    public ConditionContainer(String op) {
        this.op = op;
        this.items = new ArrayList<>();
    }

    public static ConditionContainer and() {
        return new ConditionContainer("and");
    }

    public static ConditionContainer or() {
        return new ConditionContainer("or");
    }

    public ConditionContainer add(ICondition c) {
        if (c != null)
            this.items.add(c);
        return this;
    }

    public ConditionContainer andContainer() {
        ConditionContainer cc = ConditionContainer.and();
        add(cc);
        return cc;
    }

    public ConditionContainer orContainer() {
        ConditionContainer cc = ConditionContainer.or();
        add(cc);
        return cc;
    }

    public ConditionContainer like(String field, Object val) {
        if (val != null) {
            String sval = val.toString();
            sval = (sval.indexOf('*') >= 0) ? sval.replace('*', '%') : sval + "%";
            add(new SingleValCon(field, " like ", sval));
        }
        return this;
    }

    public ConditionContainer likeInside(String field, Object val) {
        if (val != null) {
            String sval = val.toString();
            sval = (sval.indexOf('*') >= 0) ? sval.replace('*', '%') : "%" + sval + "%";
            add(new SingleValCon(field, " like ", sval));
        }
        return this;
    }

    public ConditionContainer notLike(String field, Object val) {
        if (val != null) {
            String sval = val.toString();
            sval = (sval.indexOf('*') >= 0) ? sval.replace('*', '%') : sval + "%";
            add(new SingleValCon(field, " not like ", sval));
        }
        return this;
    }

    public ConditionContainer eq(String field, Object val) {
        return add(new SingleValCon(field, "=", val));
    }

    public ConditionContainer notEq(String field, Object val) {
        return add(new SingleValCon(field, "!=", val));
    }

    public ConditionContainer gt(String field, Object val) {
        return add(new SingleValCon(field, ">", val));
    }

    public ConditionContainer ge(String field, Object val) {
        return add(new SingleValCon(field, ">=", val));
    }

    public ConditionContainer lt(String field, Object val) {
        return add(new SingleValCon(field, "<", val));
    }

    public ConditionContainer le(String field, Object val) {
        return add(new SingleValCon(field, "<=", val));
    }

    public ConditionContainer eq2(String field1, String field2) {
        return add(new TwoFieldCon(field1, "=", field2));
    }

    public ConditionContainer notEq2(String field1, String field2) {
        return add(new TwoFieldCon(field1, "!=", field2));
    }

    public ConditionContainer gt2(String field1, String field2) {
        return add(new TwoFieldCon(field1, ">", field2));
    }

    public ConditionContainer ge2(String field1, String field2) {
        return add(new TwoFieldCon(field1, ">=", field2));
    }

    public ConditionContainer lt2(String field1, String field2) {
        return add(new TwoFieldCon(field1, "<", field2));
    }

    public ConditionContainer le2(String field1, String field2) {
        return add(new TwoFieldCon(field1, "<=", field2));
    }

    public ConditionContainer isNull(String field) {
        return add(new IsNullCon(field, false));
    }

    public ConditionContainer isNotNull(String field) {
        return add(new IsNullCon(field, true));
    }

    public ConditionContainer in(String field, Object[] values) {
        return add(new ArrayCon(field, "in", values));
    }

    public ConditionContainer notIn(String field, Object[] values) {
        return add(new ArrayCon(field, "not in", values));
    }

    public ConditionContainer between(String field, Object value1, Object value2) {
        return add(new BetweenCon(field, value1, value2, false));
    }

    public ConditionContainer notBetween(String field, Object value1, Object value2) {
        return add(new BetweenCon(field, value1, value2, true));
    }

    public ConditionContainer betweenFields(String field1, String field2, Object value) {
        return add(new BetweenFieldsCon(field1, field2, value));
    }

    public ConditionContainer betweenDate(String fieldFrom, String fieldTo, Date value) {
        if (value != null) {
            ConditionContainer cand = this.andContainer();
            cand.isNotNull(fieldFrom);
            cand.le(fieldFrom, value);
            cand.orContainer().ge(fieldTo, value).isNull(fieldTo);
        }
        return this;
    }

    public ConditionContainer betweenDate(String fieldFrom, String fieldTo, Date valueFrom, Date valueTo) {
        ConditionContainer orcc = this.orContainer();
        orcc.between(fieldFrom, valueFrom, valueTo);
        orcc.between(fieldTo, valueFrom, valueTo);
        if (valueFrom != null && valueTo != null) {
            orcc.betweenFields(fieldFrom, fieldTo, valueFrom);
        }
        return this;
    }

    public ConditionContainer inSubQuery(String field, String subQuery, Object... values) {
        return add(new SubQueryCon(field, "in", subQuery, values));
    }

    public ConditionContainer notInSubQuery(String field, String subQuery, Object... values) {
        return add(new SubQueryCon(field, "not in", subQuery, values));
    }

    public ConditionContainer existSubQuery(String subQuery, Object... values) {
        return add(new SubQueryCon(null, "exists", subQuery, values));
    }

    @Override
    public boolean render(StringBuilder sql, List<Object> params) {
        boolean hasItem = false;
        int len = sql.length();
        sql.append("(");
        for (ICondition c : items) {
            if (c.render(sql, params)) {
                sql.append(" ").append(op).append(" ");
                hasItem = true;
            }
        }
        if (hasItem) {
            sql.setLength(sql.length() - op.length() - 2);
            sql.append(")");
        } else {
            sql.setLength(len);
        }
        return hasItem;
    }
}
