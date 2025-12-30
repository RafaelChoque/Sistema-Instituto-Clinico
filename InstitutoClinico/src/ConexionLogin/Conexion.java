package ConexionLogin;

import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import javax.swing.JOptionPane;

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
            JOptionPane.showMessageDialog(
                    null,
                    "No se pudo conectar a la base de datos.\n\n"
                    + "Verifique que:\n"
                    + "- MySQL esté iniciado\n"
                    + "Detalle técnico:\n" + e.getMessage(),
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }

        return con;
    }
}
