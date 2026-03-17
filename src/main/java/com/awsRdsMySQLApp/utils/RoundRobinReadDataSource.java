package com.awsRdsMySQLApp.utils;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class RoundRobinReadDataSource implements DataSource {

    private final List<DataSource> delegates;
    private final AtomicInteger index = new AtomicInteger(0);

    public RoundRobinReadDataSource(List<DataSource> delegates) {
        this.delegates = delegates;
    }

    private DataSource next() {
        int i = Math.abs(index.getAndIncrement() % delegates.size());
        return delegates.get(i);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return next().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return next().getConnection(username, password);
    }

    // ---- CommonDataSource methods ----

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        // delegate to first DS (or any strategy you prefer)
        return delegates.get(0).getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        // set on all delegates to keep them consistent
        for (DataSource ds : delegates) {
            ds.setLogWriter(out);
        }
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        for (DataSource ds : delegates) {
            ds.setLoginTimeout(seconds);
        }
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegates.get(0).getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        // if your underlying DS supports it, delegate
        return delegates.get(0).getParentLogger();
    }

    // ---- Wrapper methods ----

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        // If this class implements the interface, return this
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        // otherwise try delegates
        for (DataSource ds : delegates) {
            if (ds.isWrapperFor(iface)) {
                return ds.unwrap(iface);
            }
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return true;
        }
        for (DataSource ds : delegates) {
            if (ds.isWrapperFor(iface)) {
                return true;
            }
        }
        return false;
    }
}
