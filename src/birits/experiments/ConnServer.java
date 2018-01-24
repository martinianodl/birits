package birits.experiments;

import java.sql.*;

/**
 *
 * @author martinianodl
 */
public class ConnServer {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://birits-db.rds.amazonaws.com/";

    //  Database credentials
    static final String USER = "martinianodl";
    static final String PASS = "PASSWORD";

    public static String getQuery() throws InterruptedException {
        String todo = "";
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            String sql = "SELECT files FROM todo WHERE status = 'todo' LIMIT 1";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                //Retrieve by column name
                todo = rs.getString("files");
            }

            stmt = conn.createStatement();
            sql = "UPDATE todo SET status = 'doing' WHERE files = '" + todo + "'";
            stmt.executeUpdate(sql);
            rs.close();
        } catch (SQLException se) {
        } catch (ClassNotFoundException e) {
        } finally {
            try {
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
        }
        return todo;
    }

    public static void queryDone(String fileName) {
        String todo = "";
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            String sql = "UPDATE todo SET status = 'done' WHERE files = '" + fileName + "'";
            stmt.executeUpdate(sql);
        } catch (SQLException se) {
        } catch (ClassNotFoundException e) {
        } finally {
            try {
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
        }
    }
}
