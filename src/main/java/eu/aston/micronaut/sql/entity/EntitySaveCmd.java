package eu.aston.micronaut.sql.entity;

import java.sql.Connection;
import java.sql.SQLException;

import eu.aston.micronaut.sql.ISqlCmd;
import eu.aston.micronaut.sql.cmd.InsertNCmd;
import eu.aston.micronaut.sql.cmd.UpdateCmd;
import eu.aston.micronaut.sql.convert.IConverterFactory;

public class EntitySaveCmd<T> implements ISqlCmd<Boolean> {

    private final EntityInfo<T> ei;
    private InsertNCmd insertCmd = null;
    private UpdateCmd updateCmd = null;

    public EntitySaveCmd(EntityInfo<T> ei) {
        this.ei = ei;
    }

    @Override
    public Boolean exec(IConverterFactory cf, Connection c, Object[] args) throws SQLException {
        try {
            Object entity = args[0];
            Object oid = ei.id().beanProperty().get(entity);
            if (emptyId(oid)) {
                Object newid = insertCmd().exec(cf, c, args);
                ei.id().beanProperty().set(entity, newid);
            } else {
                updateCmd().exec(cf, c, args);
            }
        } catch (Exception e) {
            throw new SQLException("saveCmd " + e.getMessage(), e);
        }
        return true;
    }

    protected boolean emptyId(Object oid) {
        if (oid == null)
            return true;
        if (oid instanceof Number nid && nid.intValue() == 0)
            return true;
        return false;
    }

    protected InsertNCmd insertCmd() {
        if (this.insertCmd == null) this.insertCmd = EntityCmdCreator.insertCmd(ei);
        return this.insertCmd;
    }

    protected UpdateCmd updateCmd() {
        if (this.updateCmd == null) this.updateCmd = EntityCmdCreator.updateCmd(ei);
        return this.updateCmd;
    }

    @Override
    public String toString() {
        return "save " + ei.type().getName();
    }
}
