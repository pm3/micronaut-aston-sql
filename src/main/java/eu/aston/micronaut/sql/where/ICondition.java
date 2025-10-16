/*
 * Created on Sep 13, 2005
 *
 */
package eu.aston.micronaut.sql.where;

import java.util.List;

/**
 * @author pm
 */
public interface ICondition {
    boolean render(StringBuilder sql, List<Object> params);
}
