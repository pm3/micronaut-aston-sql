package eu.aston.micronaut.sql.cmd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.aston.micronaut.sql.ISqlCmd;
import eu.aston.micronaut.sql.convert.IConverter;
import eu.aston.micronaut.sql.convert.IConverterFactory;
import eu.aston.micronaut.sql.where.ConditionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertUpdateCmd implements ISqlCmd<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISqlCmd.class);

    private final List<SqlItem> items = new ArrayList<>();
    private final String table;
    private SqlBlock where = null;

    public InsertUpdateCmd(String table) {
        this.table = table;
    }

    public InsertUpdateCmd addParam(String name, Object value) {
        items.add(new SqlItem(name, value));
        return this;
    }

    public InsertUpdateCmd addOptional(String name, Object value) {
        if (value != null) {
            items.add(new SqlItem(name, value));
        }
        return this;
    }

    public InsertUpdateCmd addExpr(String name, String sql, Object... params) {
        items.add(new SqlItem(name, new SqlBlock(sql, params)));
        return this;
    }

    public void where(String sql, Object... params) {
        if (sql != null)
            this.where = new SqlBlock(sql, params);
    }

    public void where(ConditionContainer wherecc) {
        if (wherecc != null) {
            StringBuilder sb = new StringBuilder();
            List<Object> l = new ArrayList<>();
            wherecc.render(sb, l);
            if (sb.length() > 0)
                this.where = new SqlBlock(sb.toString(), l.toArray());
        }
    }

    protected void createInsertSql(StringBuilder sb, List<Object> l) {
        sb.append("insert into ").append(table).append(" (");
        StringBuilder sb2 = new StringBuilder();
        for (SqlItem i : items) {
            sb.append(i.name);
            sb.append(",");
            itemSql(i, sb2, l);
            sb2.append(",");
        }
        sb.setLength(sb.length() - 1);
        sb2.setLength(sb2.length() - 1);
        sb.append(") values (").append(sb2).append(")");
    }

    protected void createUpdateSql(StringBuilder sb, List<Object> l) {
        if (where == null)
            throw new IllegalStateException("update statement without where condition");
        sb.append("update ").append(table).append(" set ");
        for (SqlItem i : items) {
            sb.append(i.name);
            sb.append("=");
            itemSql(i, sb, l);
            sb.append(", ");
        }
        sb.setLength(sb.length() - 2);
        if (where != null) {
            sb.append(" where ").append(where.sql);
            if (where.params != null) Collections.addAll(l, where.params);
        }
    }

    protected void itemSql(SqlItem i, StringBuilder sb, List<Object> l) {
        if (i.value instanceof SqlBlock b) {
            sb.append(b.sql);
            if (b.params != null)
                Collections.addAll(l, b.params);
        } else if (i.value != null) {
            sb.append("?");
            l.add(i.value);
        } else {
            sb.append("null");
        }
    }

    @Override
    public Integer exec(IConverterFactory cf, Connection c, Object[] args) throws SQLException {
        int res = 0;
        StringBuilder sb = new StringBuilder();
        List<Object> l = new ArrayList<>();
        if (where != null) {
            createUpdateSql(sb, l);
        } else {
            createInsertSql(sb, l);
        }
        String sql = sb.toString();
        long l1 = System.currentTimeMillis();
        try {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                UpdateCmd.fillPs(cf, ps, l.toArray(), args);
                res = ps.executeUpdate();
            }
        } finally {
            long l2 = System.currentTimeMillis();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("{}; params {} args {} time {} ms",
                        sql,
                        l,
                        Arrays.toString(args),
                        (l2 - l1));
        }
        return res;
    }

    public UpdateCmd insert() throws SQLException {
        StringBuilder sb = new StringBuilder();
        List<Object> l = new ArrayList<>();
        createInsertSql(sb, l);
        return new UpdateCmd(sb.toString(), l.toArray());
    }

    public InsertNCmd insertN(String namesId, IConverter converter) throws SQLException {
        StringBuilder sb = new StringBuilder();
        List<Object> l = new ArrayList<>();
        createInsertSql(sb, l);
        return new InsertNCmd(converter, namesId, sb.toString(), l.toArray());
    }

    public UpdateCmd update() throws SQLException {
        StringBuilder sb = new StringBuilder();
        List<Object> l = new ArrayList<>();
        createUpdateSql(sb, l);
        return new UpdateCmd(sb.toString(), l.toArray());
    }

    @Override
    public String toString() {
        return "save " + table;
    }

    static class SqlItem {
        String name;
        Object value;

        public SqlItem(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

    static class SqlBlock {
        String sql;
        Object[] params;

        public SqlBlock(String sql, Object[] params) {
            this.sql = sql;
            this.params = params;
        }
    }
}
