package ConexionLogin;

import java.sql.*;
import java.util.Properties;
import java.io.InputStream;

public class Conexion {

    public static Connection obtenerConexion() {
        Connection con = null;
        try {

            Properties props = new Properties();
            InputStream is = Conexion.class.getClassLoader().getResourceAsStream("config.properties");
            if (is == null) {
                throw new RuntimeException("No se encontró config.properties");
            }
            props.load(is);


            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");


            con = DriverManager.getConnection(url, user, password);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }
}
