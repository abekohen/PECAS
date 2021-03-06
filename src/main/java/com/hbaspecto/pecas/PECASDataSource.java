package com.hbaspecto.pecas;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import com.hbaspecto.pecas.sd.DevelopNewAlternative;

public class PECASDataSource
        implements DataSource
{

    static Logger logger = Logger.getLogger(DevelopNewAlternative.class);
    String        dburl, dbUserName, dbPassword, dbDriver;

    public PECASDataSource(String landDatabaseDriver, String urlLandDatabaseSpecifier, String user,
            String password)
    {

        dburl = urlLandDatabaseSpecifier;
        dburl = dburl.trim();
        dbUserName = user;
        dbPassword = password;
        dbDriver = landDatabaseDriver;

        try
        {
            Class.forName(dbDriver);
        } catch (final Exception ex)
        {
            logger.fatal("Loading dbDriver: " + dbDriver + '\n' + ex.getMessage());
            throw new RuntimeException("Loading " + dbDriver, ex);
        }
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        final Connection con = java.sql.DriverManager.getConnection(dburl, dbUserName, dbPassword);
        return con;
    }

    @Override
    public Connection getConnection(String arg0, String arg1) throws SQLException
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int getLoginTimeout() throws SQLException
    {
        return 0;
    }

    @Override
    public void setLogWriter(PrintWriter arg0) throws SQLException
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void setLoginTimeout(int arg0) throws SQLException
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        throw new SQLFeatureNotSupportedException("No parent logger for " + PECASDataSource.class);
    }

}
