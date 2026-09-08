package com.abisheikmart.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);
    private static HikariDataSource dataSource;

    private DBUtil() {}

    public static synchronized void initDataSource(Properties props) {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url", "jdbc:h2:file:./data/abisheikmartdb;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE"));
            config.setDriverClassName(props.getProperty("db.driver", "org.h2.Driver"));
            config.setUsername(props.getProperty("db.username", "sa"));
            config.setPassword(props.getProperty("db.password", ""));

            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikari.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("hikari.minimumIdle", "2")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("hikari.idleTimeout", "300000")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("hikari.connectionTimeout", "20000")));
            config.setPoolName(props.getProperty("hikari.poolName", "AbisheikMartHikariPool"));

            dataSource = new HikariDataSource(config);
            logger.info("HikariCP DataSource initialized successfully for URL: {}", config.getJdbcUrl());
        } catch (Exception e) {
            logger.error("Failed to initialize HikariCP DataSource", e);
            throw new RuntimeException("Database initialization error", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("HikariCP DataSource is not initialized or closed.");
        }
        return dataSource.getConnection();
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static synchronized void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP DataSource closed cleanly.");
        }
    }
}
