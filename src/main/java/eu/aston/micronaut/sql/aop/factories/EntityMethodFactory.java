package eu.aston.micronaut.sql.aop.factories;

import java.util.Optional;

import eu.aston.micronaut.sql.Dbc;
import eu.aston.micronaut.sql.ISqlCmd;
import eu.aston.micronaut.sql.aop.IMethodCmdFactory;
import eu.aston.micronaut.sql.aop.Query;
import eu.aston.micronaut.sql.cmd.OptionalCmd;
import eu.aston.micronaut.sql.entity.EntityCmdCreator;
import eu.aston.micronaut.sql.entity.EntityInfo;
import io.micronaut.core.annotation.Order;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ReturnType;
import io.micronaut.inject.ExecutableMethod;
import jakarta.inject.Singleton;

@Order(101)
@Singleton
public class EntityMethodFactory implements IMethodCmdFactory {
    @Override
    public ISqlCmd<?> create(ExecutableMethod<Object, Object> executableMethod, Dbc dbc) {
        String query = executableMethod.stringValue(Query.class).orElse(null);
        if (query != null) return null;
        String name = executableMethod.getName();
        Argument<?> arg0 = executableMethod.getArguments().length == 1 ? executableMethod.getArguments()[0] : null;
        ReturnType<?> rType = executableMethod.getReturnType();
        if (name.equals("save") && arg0 != null && void.class.equals(rType.getType())) {
            try {
                EntityInfo<?> ei = dbc.getEntityInfo(arg0.getType());
                if (ei != null && ei.id() != null) return EntityCmdCreator.saveCmd(ei);
            } catch (Exception ignore) {
            }
        }
        if (name.equals("update") && arg0 != null && (void.class.equals(rType.getType()) || int.class.equals(rType.getType()))) {
            try {
                EntityInfo<?> ei = dbc.getEntityInfo(arg0.getType());
                if (ei != null && ei.id() != null) return EntityCmdCreator.updateCmd(ei);
            } catch (Exception ignore) {

            }
        }
        if (name.equals("insert") && arg0 != null && void.class.equals(rType.getType())) {
            try {
                EntityInfo<?> ei = dbc.getEntityInfo(arg0.getType());
                if (ei != null && ei.id() != null) return EntityCmdCreator.insertWithIdCmd(ei);
            } catch (Exception ignore) {
            }
        }
        if (name.equals("delete") && arg0 != null && void.class.equals(rType.getType())) {
            try {
                EntityInfo<?> ei = dbc.getEntityInfo(arg0.getType());
                if (ei != null && ei.id() != null) return EntityCmdCreator.deleteCmd(ei);
            } catch (Exception ignore) {
            }
        }
        if (name.equals("loadById") && arg0 != null && !void.class.equals(rType.getType())) {
            try {
                if (rType.getType().equals(Optional.class) && rType.getTypeParameters() != null) {
                    EntityInfo<?> ei = dbc.getEntityInfo(rType.getTypeParameters()[0].getType());
                    if (ei != null && ei.id() != null) return new OptionalCmd<>(EntityCmdCreator.loadCmd(ei));
                }
                EntityInfo<?> ei = dbc.getEntityInfo(rType.getType());
                if (ei != null && ei.id() != null) return EntityCmdCreator.loadCmd(ei);
            } catch (Exception ignore) {
            }
        }
        return null;
    }
}
