package eu.aston.micronaut.sql.entity;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import eu.aston.micronaut.sql.IRow;
import io.micronaut.core.beans.BeanIntrospection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityRow<T> implements IRow<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityRow.class);
    private final EntityInfo<T> entityInfo;
    private EntityProp[] columns;

    public EntityRow(EntityInfo<T> entityInfo) {
        this.entityInfo = entityInfo;
    }

    @Override
    public T row(ResultSet rs) throws SQLException {

        if (columns == null) {
            ResultSetMetaData mi = rs.getMetaData();
            columns = new EntityProp[mi.getColumnCount()];
            for (int i = 0; i < mi.getColumnCount(); i++) {
                String dbn = mi.getColumnLabel(i + 1).toLowerCase();
                columns[i] = entityInfo.dbnameProps().get(dbn);
            }
        }

        T obj = null;
        try {
            BeanIntrospection.Builder<T> builder = entityInfo.beanIntrospection().builder();
            for (EntityProp ep : columns) {
                try {
                    if (ep != null) {
                        Object o = ep.converter().readRs(rs, ep.dbname());
                        if (o != null) builder.with(ep.name(), o);
                    }
                } catch (Exception e) {
                    LOGGER.error(entityInfo.type().getSimpleName() + " " + e.getMessage());
                }
            }
            obj = builder.build();
        } catch (Exception e) {
            throw new SQLException("row " + entityInfo.type().getSimpleName() + " " + e.getMessage(), e);
        }
        return obj;
    }

}
