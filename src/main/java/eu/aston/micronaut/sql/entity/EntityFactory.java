package eu.aston.micronaut.sql.entity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.aston.micronaut.sql.convert.IConverter;
import eu.aston.micronaut.sql.convert.IConverterFactory;
import eu.aston.micronaut.sql.where.ICondition;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityFactory {

    private final Map<Class<?>, EntityInfo<?>> cache = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityFactory.class);

    @SuppressWarnings("unchecked")
    public <T> EntityInfo<T> create(IConverterFactory cf, Class<T> type) throws SQLException {

        EntityInfo<?> ei = cache.get(type);
        if (ei == null) {
            ei = create0(cf, type);
            if (ei != null)
                cache.put(type, ei);
        }
        return (EntityInfo<T>) ei;
    }

    protected <T> EntityInfo<T> create0(IConverterFactory cf, Class<T> type) throws SQLException {

        try{
            BeanIntrospection<T> beanIntrospection = BeanIntrospection.getIntrospection(type);
            String tableName = beanIntrospection.stringValue(Table.class, "name").orElse(type.getSimpleName());
            String sequenceExpr = beanIntrospection.stringValue(Table.class, "sequenceExpr").orElse(null);
            String idName = beanIntrospection.stringValue(Table.class, "id").orElse("id");
            List<EntityProp> props = new ArrayList<>();
            for (BeanProperty<T,Object> p : beanIntrospection.getBeanProperties()) {
                String dbName = p.stringValue(Name.class).orElse(p.getName());
                String converterName = p.stringValue(Format.class).orElse(null);
                IConverter converter = cf.converter(p.asArgument().asType(), converterName);
                if (converter == null && !ICondition.class.isAssignableFrom(type)){
                    LOGGER.warn("column type has undefined converter " + type.getName()+"."+p.getName()+" - "+p.asArgument().asType());
                    continue;
                }
                //noinspection unchecked
                props.add(new EntityProp((BeanProperty<Object,Object>)p, dbName, converter));
            }
            if (props.isEmpty())
                return null;

            EntityProp id = null;
            Map<String, EntityProp> dbnameProps = new HashMap<>(props.size());
            for (EntityProp ep : props) {
                if (ep.beanProperty().getName().equals(idName))
                    id = ep;
                dbnameProps.put(ep.dbname().toLowerCase(), ep);
            }
            return new EntityInfo<T>(beanIntrospection, tableName, sequenceExpr, props, dbnameProps, id);
        }catch (Exception e){
            return null;
        }
    }
}
