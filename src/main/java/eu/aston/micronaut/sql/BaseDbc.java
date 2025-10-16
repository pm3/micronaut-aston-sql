package eu.aston.micronaut.sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import eu.aston.micronaut.sql.convert.IConverterFactory;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(property = "aston.sql.transaction", notEquals = "true")
@EachBean(DataSource.class)
public class BaseDbc extends Dbc {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDbc.class);
    private final DataSource ds;

    public BaseDbc(DataSource ds, IConverterFactory[] converterFactories) {
        super(converterFactories);
        this.ds = ds;
    }

    public DataSource getDataSource() {
        return ds;
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    @Override
    protected void closeConnection(Connection c) {
        try {
            if (!c.getAutoCommit()) c.commit();
            c.close();
        } catch (Exception e) {
            try {
                c.close();
            } catch (Exception ignored) {
            }
            LOGGER.error("close", e);
        }
    }
}
