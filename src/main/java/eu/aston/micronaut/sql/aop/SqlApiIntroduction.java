package eu.aston.micronaut.sql.aop;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import eu.aston.micronaut.sql.Dbc;
import eu.aston.micronaut.sql.ISqlCmd;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@InterceptorBean(SqlApi.class)
public class SqlApiIntroduction implements MethodInterceptor<Object, Object> {

    private final static Logger LOGGER = LoggerFactory.getLogger(SqlApiIntroduction.class);
    private final BeanLocator locator;
    private final IMethodCmdFactory[] factories;

    public SqlApiIntroduction(BeanLocator locator, IMethodCmdFactory[] factories) {
        this.locator = locator;
        this.factories = factories;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        try {
            DbcCmd dbcCmd = findCmd(context.getExecutableMethod());
            if(dbcCmd.error()!=null) throw new SQLException(dbcCmd.error());
            return dbcCmd.dbc().exec(dbcCmd.cmd(), context.getParameterValues());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final Map<Method, DbcCmd> commands = new ConcurrentHashMap<>();

    private DbcCmd findCmd(ExecutableMethod<Object, Object> executableMethod) throws SQLException {
        DbcCmd dbcCmd = commands.get(executableMethod.getTargetMethod());
        if (dbcCmd == null) {
            String dbcName = executableMethod.stringValue(SqlApi.class).orElse("default");
            Dbc dbc = locator.getBean(Dbc.class, Qualifiers.byName(dbcName));
            for (IMethodCmdFactory factory : factories) {
                try {
                    ISqlCmd<?> cmd = factory.create(executableMethod, dbc);
                    if (cmd != null) {
                        dbcCmd = new DbcCmd(dbc, cmd, null);
                        commands.put(executableMethod.getTargetMethod(), dbcCmd);
                        break;
                    }
                } catch (Exception e) {
                    LOGGER.error("create sql method {} error {}", executableMethod.getDescription(), e.getMessage());
                }
            }
            if (dbcCmd == null) {
                dbcCmd = new DbcCmd(null, null,"not defined operation " + executableMethod.getName());
                commands.put(executableMethod.getTargetMethod(), dbcCmd);
            }
        }
        return dbcCmd;
    }

    private record DbcCmd(Dbc dbc, ISqlCmd<?> cmd, String error){}
}
