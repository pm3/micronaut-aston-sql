package eu.aston.micronaut.sql.aop.factories;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import eu.aston.micronaut.sql.BaseDbc;
import eu.aston.micronaut.sql.Dbc;
import eu.aston.micronaut.sql.IRow;
import eu.aston.micronaut.sql.ISqlCmd;
import eu.aston.micronaut.sql.ISqlExpr;
import eu.aston.micronaut.sql.aop.IMethodCmdFactory;
import eu.aston.micronaut.sql.aop.Query;
import eu.aston.micronaut.sql.cmd.CallProcCmd;
import eu.aston.micronaut.sql.cmd.InsertNCmd;
import eu.aston.micronaut.sql.cmd.OptionalCmd;
import eu.aston.micronaut.sql.cmd.SelectCmd;
import eu.aston.micronaut.sql.cmd.UpdateCmd;
import eu.aston.micronaut.sql.convert.IConverter;
import eu.aston.micronaut.sql.entity.EntityInfo;
import eu.aston.micronaut.sql.entity.EntityProp;
import eu.aston.micronaut.sql.entity.ExprArg;
import eu.aston.micronaut.sql.entity.ExprEGet;
import eu.aston.micronaut.sql.entity.Format;
import eu.aston.micronaut.sql.parse.DynSqlParser;
import eu.aston.micronaut.sql.where.ICondition;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Order;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.inject.ExecutableMethod;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(100)
@Singleton
public class QueryMethodFactory implements IMethodCmdFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDbc.class);

    public DynSqlParser dynSqlParser = new DynSqlParser();

    @Override
    public ISqlCmd<?> create(ExecutableMethod<Object, Object> executableMethod, Dbc dbc) {
        AnnotationValue<Query> query = executableMethod.getAnnotation(Query.class);
        if (query == null) return null;
        String sql = query.stringValue().orElse("").replace("\n", " ");
        String sql0 = sql.trim().split("\\s",2)[0].toLowerCase();
        ReturnType<?> rType = executableMethod.getReturnType();
        if (sql0.equals("insert")) {
            try {
                if (void.class.equals(rType.getType())) {
                    return new UpdateCmd(dynSqlParser.parseAndCheckStatic(sql, createParamsExprMap(executableMethod, dbc)));
                }
                String autoId = query.stringValue("autoIdName").orElse("");
                if (autoId.length() > 0) {
                    IConverter c = dbc.converter(rType.asType(), null);
                    if (c == null) throw new SQLException("not convert auto id type " + rType.asType());
                    return new InsertNCmd(c, autoId, dynSqlParser.parseAndCheckStatic(sql, createParamsExprMap(executableMethod, dbc)));
                }
            } catch (Exception e) {
                LOGGER.warn("create insert method {} error {}", executableMethod.getDescription(), e.getMessage(), e);
            }
            throw new RuntimeException("invalid signature insert method " + executableMethod.getDescription());
        }
        if (sql0.equals("update")) {
            try {
                if (void.class.equals(rType.getType()) || int.class.equals(rType.getType())) {
                    return new UpdateCmd(dynSqlParser.parseAndCheckStatic(sql, createParamsExprMap(executableMethod, dbc)));
                }
            } catch (Exception e) {
                LOGGER.warn("create update method {} error {}", executableMethod.getDescription(), e.getMessage(), e);
            }
            throw new RuntimeException("invalid signature update method " + executableMethod.getDescription());
        }
        if (sql0.equals("select")) {
            try {
                if (rType.getType().equals(List.class)) {
                    IRow<?> row = dbc.getRow(rType.getTypeParameters()[0].asType());
                    if (row == null)
                        throw new SQLException("not found parser for tpye " + rType);
                    return new SelectCmd<>(new Dbc.ResultListRows<>(row), dynSqlParser.parseAndCheckStatic(sql, createParamsExprMap(executableMethod, dbc)));
                }
                if (rType.getType().equals(Stream.class)) {
                    IRow<?> row = dbc.getRow(rType.getTypeParameters()[0].asType());
                    if (row == null)
                        throw new SQLException("not found parser for tpye " + rType);
                    return new SelectCmd<>(new Dbc.ResultStreamRows<>(row), dynSqlParser.parseAndCheckStatic(sql, createParamsExprMap(executableMethod, dbc)));
                }
                if (rType.getType().equals(Optional.class)) {
                    IRow<?> row = dbc.getRow(rType.getTypeParameters()[0].asType());
                    if (row == null)
                        throw new SQLException("not found parser for tpye " + rType);
                    return new OptionalCmd<>(new SelectCmd<>(new Dbc.ResultRow1<>(row), dynSqlParser.parseAndCheckStatic(sql, createParamsExprMap(executableMethod, dbc))));
                }
                IRow<?> row = dbc.getRow(rType.asType());
                if (row != null) {
                    return new SelectCmd<>(new Dbc.ResultRow1<>(row), dynSqlParser.parseAndCheckStatic(sql, createParamsExprMap(executableMethod, dbc)));
                }
            } catch (Exception e) {
                LOGGER.warn("create select method {} error {}", executableMethod.getDescription(), e.getMessage(), e);
            }
            throw new RuntimeException("invalid signature select method " + executableMethod.getDescription());
        }
        if(sql0.startsWith("{")) {
            try{
                if(executableMethod.getArguments().length == 1 && void.class.equals(rType.getType())) {
                    return new CallProcCmd(dbc, executableMethod.getArguments()[0].getType(), sql);
                }
            } catch (Exception e) {
                LOGGER.warn("create CallProc method {} error {}", executableMethod.getDescription(), e.getMessage(), e);
            }
            throw new RuntimeException("invalid signature CallProc method " + executableMethod.getDescription()+", use single argument with introspected bean");

        }
        if (sql.length() == 0) {
            throw new RuntimeException("empty query " + executableMethod.getDescription());
        }
        if (void.class.equals(rType.getType())) {
            try {
                return new UpdateCmd(dynSqlParser.parseAndCheckStatic(sql, createParamsExprMap(executableMethod, dbc)));
            } catch (Exception e) {
                LOGGER.warn("create select method {} error {}", executableMethod.getDescription(), e.getMessage(), e);
            }
        }
        return null;
    }

    public Map<String, ISqlExpr> createParamsExprMap(ExecutableMethod<Object, Object> executableMethod, Dbc dbc) throws SQLException {
        Argument<?>[] arguments = executableMethod.getArguments();
        Map<String, ISqlExpr> params = new HashMap<>();
        if (arguments.length == 1) {
            Class<?> clType = arguments[0].getType();
            if (!clType.isPrimitive() && !clType.isArray() && !clType.isEnum() && !ICondition.class.isAssignableFrom(clType)) {
                try {
                    EntityInfo<?> ei = dbc.getEntityInfo(clType);
                    if (ei != null) {
                        for (EntityProp ep : ei.props()) {
                            params.put(ep.name(), new ExprEGet(0, ep));
                        }
                        return params;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        int pos = 0;
        for (Argument<?> a : executableMethod.getArguments()) {
            Optional<AnnotationValue<Format>> format = a.findAnnotation(Format.class);
            String sFormat = format.flatMap(AnnotationValue::stringValue).orElse(null);
            IConverter c = dbc.converter(a.asType(), sFormat != null && sFormat.length() > 0 ? sFormat : null);
            params.put(a.getName(), new ExprArg(pos++, a.asType(), c));
        }
        return params;
    }
}
