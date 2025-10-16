package eu.aston.micronaut.sql.cmd;

import java.lang.reflect.ParameterizedType;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.aston.micronaut.sql.Dbc;
import eu.aston.micronaut.sql.Dbc.ResultListRows;
import eu.aston.micronaut.sql.Dbc.ResultRow1;
import eu.aston.micronaut.sql.IResult;
import eu.aston.micronaut.sql.IRow;
import eu.aston.micronaut.sql.ISqlCmd;
import eu.aston.micronaut.sql.convert.IConverter;
import eu.aston.micronaut.sql.convert.IConverterFactory;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallProcCmd implements ISqlCmd<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISqlCmd.class);

    private final String parsedSql;
    private final List<ProcParam> params;

    public CallProcCmd(Dbc dbc, Class<?> type, String sql) throws Exception {
        BeanIntrospection<?> beanIntrospection = BeanIntrospection.getIntrospection(type);
        this.params = new ArrayList<>();
        this.parsedSql = parseSql(dbc, beanIntrospection, sql, params);
    }

    @Override
    public Void exec(IConverterFactory cf, Connection c, Object[] args) throws SQLException {
        Object value = (args != null && args.length > 0) ? args[0] : null;
        if (value == null)
            throw new SQLException("null value");
        long l1 = System.currentTimeMillis();
        try {
            try (CallableStatement cs = c.prepareCall(this.parsedSql)) {
                fillInputParams(value, cs);
                cs.execute();
                fillOutputParams(value, cs);
            }
        } finally {
            long l2 = System.currentTimeMillis();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("{}; args {} time {} ms",
                        parsedSql,
                        Arrays.toString(args),
                        (l2 - l1));
        }
        return null;
    }

    protected void fillInputParams(Object value, CallableStatement cs) throws SQLException {
        for (ProcParam pp : params) {
            if (pp.outputType()!=null) {
                // output param
                cs.registerOutParameter(pp.pos(), pp.outputType());
            } else {
                // input param
                try {
                    Object val = pp.beanProperty.get(value);
                    if (pp.converter != null) {
                        pp.converter.fillPs(cs, pp.pos(), val);
                    } else {
                        cs.setObject(pp.pos(), val);
                    }
                } catch (Exception e) {
                    throw new SQLException("get param [" + pp.name() + "] in CALL " + value.getClass().getName() + " " + e.getMessage(), e);
                }
            }
        }
    }

    protected void fillOutputParams(Object value, CallableStatement cs) throws SQLException {
        for (ProcParam pp : params) {
            if (pp.outputType()!=null) {
                try {
                    if (pp.result() != null) {
                        //parse cursor
                        try (ResultSet rs = (ResultSet) cs.getObject(pp.pos())) {
                            Object val2 = rs != null ? pp.result.result(rs) : null;
                            pp.beanProperty().set(value, val2);
                        }
                    } else {
                        // parse singe value converter
                        Object val2 = pp.converter.readCs(cs, pp.pos());
                        pp.beanProperty().set(value, val2);
                    }
                } catch (Exception e) {
                    throw new SQLException("set param [" + pp.name() + "] in CALL " + value.getClass().getName() + " " + e.getMessage(), e);
                }
            }
        }
    }

    protected String parseSql(Dbc dbc, BeanIntrospection<?> beanIntrospection, String sql, List<ProcParam> params) throws SQLException {
        int pos1 = sql.indexOf('(');
        int pos2 = sql.indexOf(')', pos1 + 1);
        if (!(pos1 > 0 && pos2 > pos1)) {
            throw new SQLException("sql format in CALL is invalid, create call(arg1, arg2, ...)");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(sql, 0, pos1+1);
        String[] names = sql.substring(pos1 + 1, pos2).split(",");
        for (int i = 0; i < names.length; i++) {
            params.add(parseParam(dbc, beanIntrospection, names[i].trim(), i+1));
            if (i > 0)
                sb.append(",");
            sb.append("?");
        }
        sb.append(") }");
        return sb.toString();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ProcParam parseParam(Dbc dbc, BeanIntrospection<?> beanIntrospection, String name, int pos) throws SQLException {
        String[] items = name.split(":",2);
        BeanProperty<Object, Object> beanProperty = (BeanProperty<Object, Object>)beanIntrospection.getProperty(items[0]).orElse(null);
        if (beanProperty == null)
            throw new SQLException("undefined property [" + name + "] " + beanIntrospection.getBeanType().getName());
        Integer outputType = null;
        IResult<?> result = null;
        IConverter converter = null;
        if (items.length==2) {
            items[1] = items[1].trim();
            try {
                outputType = Types.class.getField(items[1]).getInt(null);
            } catch (Exception e) {
                try {
                    outputType = Integer.parseInt(items[1]);
                } catch (Exception ee) {
                    throw new SQLException("undefined sqltype [" + items[1] + ":" + name + "] " + beanIntrospection.getBeanType().getName());
                }
            }
        }
        if (outputType!=null && (outputType == Types.REF_CURSOR || outputType == -10/* oracle cursor */)) {
            try {
                if (List.class.isAssignableFrom(beanProperty.getType())) {
                    Class<?> listType = (Class<?>) ((ParameterizedType) beanProperty.asArgument().asType()).getActualTypeArguments()[0];
                    IRow row = dbc.getRow(listType);
                    if (row == null)
                        throw new Exception("can't create row from type " + listType.getName());
                    result = new ResultListRows(row);
                } else {
                    IRow<?> row = dbc.getRow(beanProperty.getType());
                    if (row == null)
                        throw new Exception("cannnot create row from type " + beanProperty.getType().getName());
                    result = new ResultRow1(row);
                }
            } catch (Exception e) {
                throw new SQLException("cannot create ResultSet [" + name + "] " + beanIntrospection.getBeanType().getName(), e);
            }
        }
        if(result==null) {
            converter = dbc.converter(beanProperty.asArgument().asType(), null);
        }
        return new ProcParam(pos, beanProperty, converter, result, outputType);
    }

    @Override
    public String toString() {
        return parsedSql;
    }

    record ProcParam(
            int pos,
            BeanProperty<Object, Object> beanProperty,
            IConverter converter,
            IResult<?> result,
            Integer outputType){
        public String name(){
            return beanProperty().getName();
        }
    }
}
