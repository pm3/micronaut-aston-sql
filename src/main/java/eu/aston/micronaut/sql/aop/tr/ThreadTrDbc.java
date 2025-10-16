package eu.aston.micronaut.sql.aop.tr;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import eu.aston.micronaut.sql.Dbc;
import eu.aston.micronaut.sql.convert.IConverterFactory;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(property = "aston.sql.transaction", value = "true")
@EachBean(DataSource.class)
public class ThreadTrDbc extends Dbc {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadTrDbc.class);

    private static final ThreadLocal<LazyConnection> threadConnection = new ThreadLocal<>();
    private final DataSource ds;

    public ThreadTrDbc(DataSource ds, IConverterFactory[] converterFactories) {
        super(converterFactories);
        this.ds = ds;
        LOGGER.info("start ThreadTrDbc");
    }

    public DataSource getDataSource() {
        return ds;
    }

    public Connection getConnection() throws SQLException {
        if (threadConnection.get() != null)
            return threadConnection.get().connection();
        Connection c = ds.getConnection();
        c.setAutoCommit(true);
        return c;
    }

    public void startTransaction() throws SQLException {
        if (threadConnection.get() != null)
            throw new SQLException("double open transaction");
        threadConnection.set(new LazyConnection(ds));
    }

    @Override
    protected void closeConnection(Connection c) {
        if (threadConnection.get() == null) {
            try {
                close(c, null);
            } catch (SQLException ignored) {
            }
        }
    }

    protected void close(boolean commit) throws SQLException {
        LazyConnection lc = threadConnection.get();
        threadConnection.remove();
        if (lc != null && lc.initialized()) {
            close(lc.connection(), commit);
        }
    }

    protected void close(Connection c, Boolean commit) throws SQLException {
        try (c) {
            if (commit != null) {
                if (commit)
                    c.commit();
                else
                    c.rollback();
            }
        }
    }

    public static class LazyConnection {
        private final DataSource dataSource;
        private Connection connection = null;

        public LazyConnection(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public boolean initialized() {
            return connection != null;
        }

        public Connection connection() throws SQLException {
            if (connection == null) {
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
            }
            return connection;
        }
    }
}
