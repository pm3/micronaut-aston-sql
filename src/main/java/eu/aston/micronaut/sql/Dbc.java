package eu.aston.micronaut.sql;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import eu.aston.micronaut.sql.convert.IConverter;
import eu.aston.micronaut.sql.convert.IConverterFactory;
import eu.aston.micronaut.sql.convert.OptionalConverter;
import eu.aston.micronaut.sql.entity.EntityFactory;
import eu.aston.micronaut.sql.entity.EntityInfo;
import eu.aston.micronaut.sql.entity.EntityRow;

public abstract class Dbc {

    protected final EntityFactory entityFactory = new EntityFactory();
    protected final Map<Object, IConverter> _converters = new HashMap<>();
    protected IConverterFactory[] converterFactories;

    public Dbc(IConverterFactory[] converterFactories) {
        this.converterFactories = converterFactories;
    }

    protected abstract Connection getConnection() throws SQLException;

    protected abstract void closeConnection(Connection c);

    public IConverter converter(Type type, String name) throws SQLException {
        if (type != null && type.equals(Optional.class) && type instanceof ParameterizedType ptype && ptype.getActualTypeArguments().length == 1) {
            IConverter wrap = converter(ptype.getActualTypeArguments()[0], name);
            return wrap != null ? new OptionalConverter(wrap) : null;
        }
        Object key = null;
        if (type != null && name != null) key = name + ":" + type.getTypeName();
        else if (type != null) key = type;
        else if (name != null) key = name;
        else return null;
        IConverter c = _converters.get(key);
        if (c == null) {
            for (int i = converterFactories.length - 1; i >= 0; i--) {
                c = converterFactories[i].converter(type, name);
                if (c != null) {
                    _converters.put(key, c);
                    break;
                }
            }
        }
        return c;
    }

    public void addConverter(Object type, IConverter c) {
        if (type != null && c != null)
            _converters.put(type, c);
    }

    public <X> EntityInfo<X> getEntityInfo(Class<X> cl) throws SQLException {
        return entityFactory.create(this::converter, cl);
    }

    @SuppressWarnings("unchecked")
    public <T> IRow<T> getRow(Type type) throws SQLException {
        if (type instanceof Class cl) {
            EntityInfo<T> ei = entityFactory.create(this::converter, cl);
            if (ei != null) {
                return new EntityRow<>(ei);
            }
            if (Object[].class.equals(cl)) {
                return (IRow<T>) new ObjectARow();
            }
        }
        IConverter c = converter(type, null);
        if (c != null) {
            return new SimpleRow<>(c);
        }
        return null;
    }

    public <R> R exec(ISqlCmd<R> exec, Object... args) throws SQLException {
        R resp = null;
        Connection c = null;
        try {
            c = getConnection();
            resp = exec.exec(this::converter, c, args);
        } finally {
            closeConnection(c);
        }
        return resp;
    }

    public static class ResultListRows<T> implements IResult<List<T>> {
        private final IRow<T> row;

        public ResultListRows(IRow<T> row) {
            this.row = row;
        }

        @Override
        public List<T> result(ResultSet rs) throws SQLException {
            List<T> l = new ArrayList<>();
            while (rs.next()) {
                l.add(row.row(rs));
            }
            return l;
        }
    }

    public static class ResultStreamRows<T> implements IResult<Stream<T>> {
        private final IRow<T> row;

        public ResultStreamRows(IRow<T> row) {
            this.row = row;
        }

        @Override
        public Stream<T> result(ResultSet rs) throws SQLException {
            List<T> l = new ArrayList<>();
            while (rs.next()) {
                l.add(row.row(rs));
            }
            return l.stream();
        }
    }

    public static class ResultRow1<T> implements IResult<T> {
        private final IRow<T> row;

        public ResultRow1(IRow<T> row) {
            this.row = row;
        }

        @Override
        public T result(ResultSet rs) throws SQLException {
            T o = null;
            if (rs.next()) {
                o = row.row(rs);
            }
            if (rs.next()) {
                throw new SQLException("multiple_rows");
            }
            return o;
        }
    }

    @SuppressWarnings("unchecked")
    public static class SimpleRow<T> implements IRow<T> {

        private final IConverter converter;

        public SimpleRow(IConverter converter) {
            this.converter = converter;
        }

        @Override
        public T row(ResultSet rs) throws SQLException {
            return (T) converter.readRs(rs, 1);
        }
    }

    public static class ObjectARow implements IRow<Object[]> {

        private int rsize = -1;

        @Override
        public Object[] row(ResultSet rs) throws SQLException {
            if (rsize < 1)
                rsize = rs.getMetaData().getColumnCount();
            Object[] row = new Object[rsize];
            for (int i = 0; i < rsize; i++)
                row[i] = rs.getObject(i + 1);
            return row;
        }
    }
}
