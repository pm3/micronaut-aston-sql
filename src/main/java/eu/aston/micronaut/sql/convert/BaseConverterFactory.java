package eu.aston.micronaut.sql.convert;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.micronaut.core.annotation.Order;
import jakarta.inject.Singleton;

@Singleton
@Order(101)
public class BaseConverterFactory implements IConverterFactory {

    private final Map<Object, IConverter> base = new HashMap<>();

    public BaseConverterFactory() {
        base.put(String.class, new StringConverter());
        base.put(boolean.class, new BooleanNConverter());
        base.put(Boolean.class, new BooleanOConverter());
        base.put(int.class, new IntConverter());
        base.put(Integer.class, new IntegerConverter());
        base.put(long.class, new LongNConverter());
        base.put(Long.class, new LongOConverter());
        base.put(double.class, new DoubleNConverter());
        base.put(Double.class, new DoubleOConverter());
        base.put(BigDecimal.class, new BigDecimalConverter());
        base.put(Date.class, new DateConverter());
        base.put(LocalDate.class, new LocalDateConverter());
        base.put(LocalDateTime.class, new LocalDateTimeConverter());
        base.put(Instant.class, new InstantConverter());
        base.put(byte[].class, new ByteAConverter());
        base.put(String[].class, new StringArrayConverter());
    }

    @Override
    public IConverter converter(Type type, String name) {
        if (type != null) {
            return base.get(type);
        }
        return null;
    }

    public static class StringConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            ps.setObject(pos, val);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            return rs.getString(pos);
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            return rs.getString(name);
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            return cs.getString(pos);
        }
    }

    public static class BooleanNConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            boolean v = false;
            if (val instanceof Boolean)
                v = (Boolean) val;
            ps.setBoolean(pos, v);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            return rs.getBoolean(pos);
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            return rs.getBoolean(name);
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            return cs.getBoolean(pos);
        }
    }

    public static class BooleanOConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            ps.setObject(pos, val);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            boolean b = rs.getBoolean(pos);
            return rs.wasNull() ? null : b;
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            boolean b = rs.getBoolean(name);
            return rs.wasNull() ? null : b;
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            boolean b = cs.getBoolean(pos);
            return cs.wasNull() ? null : b;
        }
    }

    public static class IntConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            int v = 0;
            if (val instanceof Number)
                v = ((Number) val).intValue();
            ps.setInt(pos, v);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            return rs.getInt(pos);
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            return rs.getInt(name);
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            return cs.getInt(pos);
        }
    }

    public static class IntegerConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            ps.setObject(pos, val);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            int v = rs.getInt(pos);
            return rs.wasNull() ? null : v;
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            int v = rs.getInt(name);
            return rs.wasNull() ? null : v;
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            int v = cs.getInt(pos);
            return cs.wasNull() ? null : v;
        }
    }

    public static class LongNConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            long v = 0;
            if (val instanceof Number)
                v = ((Number) val).longValue();
            ps.setLong(pos, v);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            return rs.getLong(pos);
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            return rs.getLong(name);
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            return cs.getLong(pos);
        }
    }

    public static class LongOConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            ps.setObject(pos, val);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            long v = rs.getLong(pos);
            return rs.wasNull() ? null : v;
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            long v = rs.getLong(name);
            return rs.wasNull() ? null : v;
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            long v = cs.getLong(pos);
            return cs.wasNull() ? null : v;
        }
    }

    public static class DoubleNConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            double v = 0.0;
            if (val instanceof Number)
                v = ((Number) val).doubleValue();
            ps.setDouble(pos, v);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            return rs.getDouble(pos);
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            return rs.getDouble(name);
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            return cs.getDouble(pos);
        }
    }

    public static class DoubleOConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            ps.setObject(pos, val);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            double v = rs.getDouble(pos);
            return rs.wasNull() ? null : v;
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            double v = rs.getDouble(name);
            return rs.wasNull() ? null : v;
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            double v = cs.getDouble(pos);
            return cs.wasNull() ? null : v;
        }
    }

    public static class BigDecimalConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            ps.setObject(pos, val);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            return rs.getBigDecimal(pos);
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            return rs.getBigDecimal(name);
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            return cs.getBigDecimal(pos);
        }
    }

    public static class DateConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            Timestamp t = null;
            if (val instanceof Date)
                t = new Timestamp(((Date) val).getTime());
            ps.setTimestamp(pos, t);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            return ts2date(rs.getTimestamp(pos));
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            return ts2date(rs.getTimestamp(name));
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            return ts2date(cs.getTimestamp(pos));
        }

        Date ts2date(Timestamp t) {
            return t != null ? new Date(t.getTime()) : null;
        }
    }

    public static class LocalDateConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            java.sql.Date d = null;
            if (val instanceof LocalDate)
                d = java.sql.Date.valueOf((LocalDate) val);
            ps.setDate(pos, d);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            return sdate2ldate(rs.getDate(pos));
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            return sdate2ldate(rs.getDate(name));
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            return sdate2ldate(cs.getDate(pos));
        }

        LocalDate sdate2ldate(java.sql.Date d) {
            return d != null ? d.toLocalDate() : null;
        }

    }

    public static class LocalDateTimeConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            Timestamp t = null;
            if (val instanceof LocalDateTime)
                t = Timestamp.valueOf(((LocalDateTime) val));
            ps.setTimestamp(pos, t);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            return ts2date(rs.getTimestamp(pos));
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            return ts2date(rs.getTimestamp(name));
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            return ts2date(cs.getTimestamp(pos));
        }

        LocalDateTime ts2date(Timestamp t) {
            return t != null ? t.toLocalDateTime() : null;
        }
    }

    public static class InstantConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            Timestamp t = null;
            if (val instanceof Instant)
                t = Timestamp.from((Instant) val);
            ps.setTimestamp(pos, t);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            return ts2date(rs.getTimestamp(pos));
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            return ts2date(rs.getTimestamp(name));
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            return ts2date(cs.getTimestamp(pos));
        }

        Instant ts2date(Timestamp t) {
            return t != null ? t.toInstant() : null;
        }
    }

    public static class ByteAConverter implements IConverter {

        @Override
        public void fillPs(PreparedStatement ps, int pos, Object val) throws SQLException {
            ps.setBytes(pos, val instanceof byte[] ? (byte[]) val : null);
        }

        @Override
        public Object readRs(ResultSet rs, int pos) throws SQLException {
            return rs.getBytes(pos);
        }

        @Override
        public Object readRs(ResultSet rs, String name) throws SQLException {
            return rs.getBytes(name);
        }

        @Override
        public Object readCs(CallableStatement cs, int pos) throws SQLException {
            return cs.getBytes(pos);
        }
    }

    public static class StringArrayConverter extends StringFnConverter {

        public StringArrayConverter() {
            this("\n");
        }

        public StringArrayConverter(String delim) {
            super(s -> toArray(s, delim), val -> toString(val, delim));
        }

        public static String[] toArray(String s, String delim) {
            if (s != null) {
                return s.split(delim);
            }
            return null;
        }

        public static String toString(Object obj, String delim) {
            if (obj instanceof String[] arr) {
                return String.join(delim, arr);
            }
            return null;
        }
    }

}
