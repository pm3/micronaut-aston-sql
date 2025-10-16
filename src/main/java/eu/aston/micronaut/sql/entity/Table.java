package eu.aston.micronaut.sql.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.micronaut.core.annotation.Introspected;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Introspected
public @interface Table {
    String name() default "";

    String id() default "id";

    String sequenceExpr() default "";
}
