import java.sql.*;

public class DBcon {
    static String url = "jdbc:mysql://localhost:3306/bbms";
    static String usr = "root";
    static String pw = "";

    public static Connection getCon() {
        Connection c = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            c = DriverManager.getConnection(url, usr, pw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }
}
