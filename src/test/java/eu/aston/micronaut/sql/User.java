package eu.aston.micronaut.sql;

import java.util.Map;

import eu.aston.micronaut.sql.entity.Format;
import eu.aston.micronaut.sql.entity.Table;

@Table(name = "test_user")
public record User(
        String id,
        String name,
        @Format("json") Map<String, String> props){}