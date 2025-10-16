package eu.aston.micronaut.sql.where;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Multi<T> implements ICondition {

    Iterator<T> it;

    Multi(Iterator<T> it) {
        this.it = it;
    }

    @Override
    public boolean render(StringBuilder sql, List<Object> params) {
        while (it.hasNext()) {
            Object val = it.next();
            sql.append("?");
            params.add(val);
            if (it.hasNext()) sql.append(",");
        }
        return true;
    }

    public static <T> Multi<T> of(Iterable<T> iterable) {
        return iterable != null ? new Multi<>(iterable.iterator()) : null;
    }

    public static <T> Multi<T> of(T[] arr) {
        return arr != null && arr.length > 0 ? new Multi<>(Arrays.stream(arr).iterator()) : null;
    }

    public static Multi<Integer> of(int[] arr) {
        return arr != null && arr.length > 0 ? new Multi<>(Arrays.stream(arr).iterator()) : null;
    }

    public static Multi<Long> of(long[] arr) {
        return arr != null && arr.length > 0 ? new Multi<>(Arrays.stream(arr).iterator()) : null;
    }
}
