package ConexionLogin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;


public class UsuarioDAO {
    public static void crearAdminSiNoExiste() {
        try (Connection con = Conexion.obtenerConexion()) {

            String check = "SELECT * FROM usuarios WHERE rol = 'Administrador' LIMIT 1";
            PreparedStatement ps = con.prepareStatement(check);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                String insert = "INSERT INTO usuarios(username, contrasena, rol, activo) VALUES(?, ?, 'Administrador', 1)";
                PreparedStatement psInsert = con.prepareStatement(insert);

                String usuario = "adminpulso"; 
                String password = BCrypt.hashpw("pulso123", BCrypt.gensalt());

                psInsert.setString(1, usuario);
                psInsert.setString(2, password);

                psInsert.executeUpdate();
                psInsert.close();

                System.out.println("Administrador creado automáticamente: " + usuario);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void cambiarContrasena(int idUsuario, String nuevaPass) {
        try (Connection con = Conexion.obtenerConexion()) {
            String hash = BCrypt.hashpw(nuevaPass, BCrypt.gensalt());
            String sql = "UPDATE usuarios SET contrasena = ? WHERE id_usuario = ?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, hash);
            ps.setInt(2, idUsuario);

            ps.executeUpdate();
            ps.close();

            System.out.println("Contraseña actualizada correctamente.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
