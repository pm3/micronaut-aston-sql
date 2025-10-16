package eu.aston.micronaut.sql.aop;

import eu.aston.micronaut.sql.Dbc;
import eu.aston.micronaut.sql.ISqlCmd;
import io.micronaut.inject.ExecutableMethod;

public interface IMethodCmdFactory {
    ISqlCmd<?> create(ExecutableMethod<Object, Object> executableMethod, Dbc dbc);
}
