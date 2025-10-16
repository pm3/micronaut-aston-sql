package eu.aston.micronaut.sql.entity;

import java.lang.reflect.Type;

import eu.aston.micronaut.sql.convert.IConverter;
import io.micronaut.core.beans.BeanProperty;

public record EntityProp(
        BeanProperty<Object, Object> beanProperty,
    String dbname,
    IConverter converter
){
    public String name(){
        return beanProperty.getName();
    }

    public Type type(){
        return beanProperty.asArgument().asType();
    }
}
