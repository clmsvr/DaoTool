package dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnectionFectory {

    /**
     * Retorna uma conexao com o banco de dados de acordo com os dados definidos 
     * no arquivo "datasource.ini" procurado no CLASSPATH.
     * 
     * @return Connection
     * @throws SQLException
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public static Connection getLocalConnection() 
    throws SQLException, IOException, ClassNotFoundException
    {

        Properties props = ConfigHelper.getConfigFileProperties("datasource.ini");            
        
        String user = props.getProperty("jdbc.user") ;
        String pwd = ConfigHelper.decode64(props.getProperty("jdbc.pwd"));
        String url = props.getProperty("jdbc.url");
        String driver = props.getProperty("jdbc.driver");

        //Class.forName("com.mysql.jdbc.Driver");
        //Class.forName("com.mysql.cj.jdbc.Driver");
        Class.forName(driver);  //so isso
        //Class.forName(driver).newInstance();
        //DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        
        Connection conn = DriverManager.getConnection(url, user, pwd);

        conn.setAutoCommit(false);
        return conn;
    }
    
    public static void main(String[] args) {
    	
        try (Connection conn = DBConnectionFectory.getLocalConnection()) 
        {
            System.out.println("###### OK #######");
        } catch (Exception e) {
			e.printStackTrace();
		}
        
	}
    

}
