package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class HDataSource {

    private HikariDataSource ds;

    @Value("${spring.datasource.username}")
    private String dbUser; 
    @Value("${spring.datasource.password}")
    private String dbPwd;
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${proj.jdbc.driver}")
    private String jdbcDriver;
  
    private HDataSource() {
    }

    @PostConstruct
    public void init() {
    	HikariConfig config = new HikariConfig();
    	
		config.setDriverClassName(jdbcDriver);
		config.setJdbcUrl(dbUrl);
		config.setUsername(dbUser);
		config.setPassword(dbPwd);
		config.setMinimumIdle(1);
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.setAutoCommit(false);
		
		/*
		autoCommit
		connectionTimeout
		idleTimeout
		maxLifetime
		connectionTestQuery
		connectionInitSql
		validationTimeout
		maximumPoolSize
		poolName
		allowPoolSuspension
		readOnly
		transactionIsolation
		leakDetectionThreshold			
		 */
		
		ds = new HikariDataSource(config);	    
    }
    
    @PreDestroy
    public void destroy() {
    	
    	System.out.println("DESTROY HDataSource...");
    	
    	if (ds != null)
    		ds.close();
    }
    
    public Connection getConnection() throws SQLException {
        try
        {
            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            if (conn.getAutoCommit() == true)
                throw new SQLException("JDBC DATASOURCE AUTOCOMMIT set to TRUE !!!!") ;
            
            return conn;
        }
        catch (Exception ex)
        {
            throw new SQLException(ex.getMessage(), ex);
        }       
    }
    
    public void closeConnection(Connection conn, Statement ps)
    {
        try
        {
            if (ps != null) ps.close();
        }
        catch (Exception e)
        {
            ps = null;
        }

        try
        {
            if (conn != null) conn.close();
        }
        catch (Exception e)
        {
            conn = null;
        }
    }	
    
    protected void closeConnection(Connection conn)
    {
        try
        {
            if (conn != null) conn.close();
        }
        catch (Exception e)
        {
            conn = null;
        }
    }	    
}