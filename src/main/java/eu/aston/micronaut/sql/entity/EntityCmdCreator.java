package eu.aston.micronaut.sql.entity;

import java.sql.SQLException;

import eu.aston.micronaut.sql.Dbc;
import eu.aston.micronaut.sql.cmd.InsertNCmd;
import eu.aston.micronaut.sql.cmd.InsertUpdateCmd;
import eu.aston.micronaut.sql.cmd.SelectCmd;
import eu.aston.micronaut.sql.cmd.UpdateCmd;

public class EntityCmdCreator {

    public static <T> InsertNCmd insertCmd(EntityInfo<T> ei) {
        if (ei.id() == null)
            throw new IllegalArgumentException("entity without id [insert] " + ei.type().getName());
        InsertUpdateCmd cmd = new InsertUpdateCmd(ei.tableName());
        if (ei.sequenceExpr() != null && ei.sequenceExpr().length() > 0)
            cmd.addExpr(ei.id().dbname(), ei.sequenceExpr());
        for (EntityProp ep : ei.props()) {
            if (ep == ei.id())
                continue;
            cmd.addParam(ep.dbname(), new ExprEGet(0, ep));
        }
        try {
            return cmd.insertN(ei.id().dbname(), ei.id().converter());
        } catch (SQLException e) {
            throw new IllegalArgumentException("create entity sql [insert] " + ei.type().getName() + " " + e.getMessage(), e);
        }
    }

    public static <T> UpdateCmd insertWithIdCmd(EntityInfo<T> ei) {
        if (ei.id() == null)
            throw new IllegalArgumentException("entity without id [insertWithId] " + ei.type().getName());
        InsertUpdateCmd cmd = new InsertUpdateCmd(ei.tableName());
        for (EntityProp ep : ei.props()) {
            cmd.addParam(ep.dbname(), new ExprEGet(0, ep));
        }
        try {
            return cmd.insert();
        } catch (SQLException e) {
            throw new IllegalArgumentException("create entity sql [insertWithId] " + ei.type().getName() + " " + e.getMessage(), e);
        }
    }

    public static <T> UpdateCmd updateCmd(EntityInfo<T> ei) {
        if (ei.id() == null)
            throw new IllegalArgumentException("entity without id [update] " + ei.type().getName());
        InsertUpdateCmd cmd = new InsertUpdateCmd(ei.tableName());
        for (EntityProp ep : ei.props()) {
            if (ep == ei.id())
                continue;
            cmd.addParam(ep.dbname(), new ExprEGet(0, ep));
        }
        cmd.where(ei.id().dbname() + "=?", new ExprEGet(0, ei.id()));
        try {
            return cmd.update();
        } catch (SQLException e) {
            throw new IllegalArgumentException("create entity sql [update] " + ei.type().getName() + " " + e.getMessage(), e);
        }
    }

    public static <T> EntitySaveCmd<T> saveCmd(EntityInfo<T> ei) {
        return new EntitySaveCmd<>(ei);
    }

    public static <T> SelectCmd<T> loadCmd(EntityInfo<T> ei) {
        if (ei.id() == null)
            throw new IllegalArgumentException("entity without id [load] " + ei.type().getName());
        String sql = "select * from " + ei.tableName() + " where " + ei.id().dbname() + "=?";
        Object[] cparams = new Object[]{new ExprArg(0, ei.id().type(), ei.id().converter())};
        return new SelectCmd<>(new Dbc.ResultRow1<>(new EntityRow<>(ei)), sql, cparams);
    }

    public static UpdateCmd deleteCmd(EntityInfo<?> ei) {
        if (ei.id() == null)
            throw new IllegalArgumentException("entity without id [delete] " + ei.type().getName());
        String sql = "delete from " + ei.tableName() + " where " + ei.id().dbname() + "=?";
        Object[] cparams = new Object[]{new ExprEGet(0, ei.id())};
        return new UpdateCmd(sql, cparams);
    }

    public static UpdateCmd deleteIdCmd(EntityInfo<?> ei) {
        if (ei.id() == null)
            throw new IllegalArgumentException("entity without id [delete] " + ei.type().getName());
        String sql = "delete from " + ei.tableName() + " where " + ei.id().dbname() + "=?";
        Object[] cparams = new Object[]{new ExprArg(0, ei.id().type(), ei.id().converter())};
        return new UpdateCmd(sql, cparams);
    }
}
