package eu.aston.micronaut.sql.parse;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.aston.micronaut.sql.ISqlExpr;
import eu.aston.micronaut.sql.cmd.SqlParams;
import eu.aston.micronaut.sql.where.ICondition;

public class DynSqlParser {

    public SqlParams parseAndCheckStatic(String sql, Map<String, ISqlExpr> paramExprs) throws IOException {
        DynSqlParams dynSqlParams = parse(sql, paramExprs);
        try {
            return dynSqlParams.toStatic();
        } catch (Exception ignored) {
        }
        return dynSqlParams;
    }

    public DynSqlParams parse(String sql, Map<String, ISqlExpr> paramExprs) throws IOException {
        List<Object> items = new ArrayList<>();
        parseSql(new DSTokenizer(sql), items, false, paramExprs);
        return new DynSqlParams(sql, items);
    }

    protected void parseSql(DSTokenizer t, List<Object> items, boolean insideBlock, Map<String, ISqlExpr> paramExprs) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (!t.eof()) {
            if (t.check("'")) {
                items.add(sb.toString());
                sb.setLength(0);
                items.add(parseQuoted(t));
                continue;
            }
            if (t.check("::")) {
                sb.append("::");
                continue;
            }
            if (t.check(":")) {
                items.add(sb.toString());
                sb.setLength(0);
                items.add(parseParam(t, paramExprs));
                continue;
            }
            if (t.check("/**")) {
                if (insideBlock)
                    throw new IOException("block inside block");
                items.add(sb.toString());
                sb.setLength(0);
                items.add(parseBlock(t, paramExprs));
                continue;
            }
            if (t.check("/*")) {
                // ommit comment
                parseComment(t);
            }
            if (t.check("*/")) {
                if (!insideBlock)
                    throw new IOException("end block before start block");
                items.add(sb.toString());
                return;
            }
            sb.append(t.aktN());
        }
        if (insideBlock)
            throw new EOFException("unclosed block");
        items.add(sb.toString());
    }

    protected String parseQuoted(DSTokenizer t) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("'");
        while (!t.eof()) {
            if (t.check("''")) {
                sb.append("''");
                continue;
            }
            if (t.check("'")) {
                sb.append("'");
                return sb.toString();
            }
            sb.append(t.aktN());
        }
        throw new EOFException("unclosed quote " + sb);
    }

    protected DSparam parseParam(DSTokenizer t, Map<String, ISqlExpr> paramExprs) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (!t.eof()) {
            char ch = t.aktN();
            if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_')) {
                t.back();
                break;
            }
            sb.append(ch);
        }
        String pname = sb.toString();
        ISqlExpr expr = paramExprs.get(pname);
        if (expr == null) throw new IOException("undefined param " + pname);
        Class<?> cl = expr.type() instanceof Class ? (Class<?>) expr.type() : expr.type() instanceof ParameterizedType ? (Class<?>) ((ParameterizedType) expr.type()).getRawType() : null;
        boolean condition = cl != null && ICondition.class.isAssignableFrom(cl);
        return new DSparam(pname, expr, condition);
    }

    protected void parseComment(DSTokenizer t) throws IOException {
        while (!t.eof()) {
            if (t.check("'")) {
                parseQuoted(t);
                continue;
            }
            if (t.check("/*")) {
                throw new IOException("comment inside comment");
            }
            if (t.check("*/")) {
                return;
            }
            t.aktN();
        }
        throw new EOFException("unclosed comment");
    }

    protected DSblock parseBlock(DSTokenizer t, Map<String, ISqlExpr> paramExprs) throws IOException {
        DSblock b = new DSblock();
        parseSql(t, b.items, true, paramExprs);
        return b;
    }

    static class DSparam {
        String name;
        ISqlExpr expr;
        boolean condition;

        public DSparam(String name, ISqlExpr expr, boolean condition) {
            this.name = name;
            this.expr = expr;
            this.condition = condition;
        }

        @Override
        public String toString() {
            return "{name:" + name + "}";
        }
    }

    static class DSblock {
        List<Object> items = new ArrayList<>();

        @Override
        public String toString() {
            return items.stream().map(Object::toString).collect(Collectors.joining(""));
        }
    }

    public static class DSTokenizer {
        String s;
        int pos = 0;

        public DSTokenizer(String s) {
            this.s = s;
        }

        public boolean eof() {
            return pos >= s.length();
        }

        public void eofE() throws EOFException {
            if (eof())
                throw new EOFException();
        }

        public boolean check(String check) {
            for (int i = 0; i < check.length(); i++) {
                if (pos + i >= s.length())
                    return false;
                if (check.charAt(i) != s.charAt(pos + i))
                    return false;
            }
            pos += check.length();
            return true;
        }

        public char akt() throws EOFException {
            eofE();
            return s.charAt(pos);
        }

        public char aktN() throws EOFException {
            eofE();
            return s.charAt(pos++);
        }

        public boolean back() {
            if (pos > 0) {
                pos--;
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return pos < s.length() ? s.substring(pos) : "EOF";
        }
    }
}
