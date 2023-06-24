import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class jdbc {
    public static void main(String[] args) throws Exception{
        String url = "jdbc:mysql://localhost:3306/nerdygadgets";
        String uname = "root";
        String pass = "";
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con = DriverManager.getConnection(url, uname, pass);
        Statement st = con.createStatement();
        String query = "SELECT * FROM retouren";
        ResultSet rs = st.executeQuery(query);
        st.close();
        con.close();
    }
}
