package helpers;

import java.sql.*;

public class DBQueryHelper {
    private static Connection getConnection(){
        try{
            Connection conn = DriverManager.getConnection(Constants.DB_CONNECTION_URL);
            conn.setAutoCommit(true);
            return conn;
        }catch(SQLTimeoutException e){
            e.printStackTrace();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public static PreparedStatement getPreparedStatement(String sql){
        Connection conn = DBQueryHelper.getConnection();
        try{
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt;
        }catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }
}
