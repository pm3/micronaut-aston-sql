package eu.aston.micronaut.sql.entity;

import java.lang.reflect.Type;

import eu.aston.micronaut.sql.ISqlExpr;
import eu.aston.micronaut.sql.convert.IConverter;

public class ExprArg implements ISqlExpr {

    int pos;
    Type type;
    IConverter _converter;

    public ExprArg(int pos, Type type, IConverter converter) {
        this.pos = pos;
        this.type = type;
        this._converter = converter;
    }

    @Override
    public Object parse(Object[] args) {
        return args[pos];
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public IConverter converter() {
        return _converter;
    }

    @Override
    public String toString() {
        return "ExprArg." + pos;
    }
}
