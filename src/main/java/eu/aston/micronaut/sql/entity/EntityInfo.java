package eu.aston.micronaut.sql.entity;

import java.util.List;
import java.util.Map;

import io.micronaut.core.beans.BeanIntrospection;

public record EntityInfo<T>(
        BeanIntrospection<T> beanIntrospection,
        String tableName,
        String sequenceExpr,
        List<EntityProp> props,
        Map<String, EntityProp> dbnameProps,
        EntityProp id) {

    public Class<T> type(){
        return beanIntrospection.getBeanType();
    }
}

