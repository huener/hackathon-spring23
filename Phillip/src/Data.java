import java.sql.*;
public final class Data {
    static Connection con;
    static void initializeData(String url)
    {
        con = null;
        try{
            con = DriverManager.getConnection(url);
        }catch(Exception SQLException)
        {
            System.out.println("ERROR : COULD NOT ESTABLISH CONNECTION TO DATABASE");
        }
    }

}
