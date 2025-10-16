package eu.aston.micronaut.sql.entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.sql.SQLException;

import eu.aston.micronaut.sql.ISqlExpr;
import eu.aston.micronaut.sql.convert.IConverter;

public class ExprEGet implements ISqlExpr {
    private final int pos;
    public final EntityProp prop;

    public ExprEGet(int pos, EntityProp prop) {
        this.pos = pos;
        this.prop = prop;
    }

    @Override
    public Object parse(Object[] args) throws SQLException {
        try {
            Object obj = args[pos];
            return obj != null ? prop.beanProperty().get(obj) : null;
        } catch (Exception e) {
            if (e instanceof InvocationTargetException e2) {
                if (e2.getTargetException() instanceof Exception)
                    e = (Exception) e2.getTargetException();
            }
            throw new SQLException("entity property " + prop.type().getTypeName() + "." + prop.name() + " " + e.getMessage(), e);
        }
    }

    @Override
    public Type type() {
        return prop.type();
    }

    @Override
    public IConverter converter() {
        return prop.converter();
    }

    @Override
    public String toString() {
        return "ExprEGet." + pos + "." + prop.name();
    }
}