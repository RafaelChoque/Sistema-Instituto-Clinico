package ConexionLogin;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author Erlann
 */
public class SesionUsuario {
    public static int idUsuario;
    public static int idtecnico; 
    public static String username;
    public static String rol;
    public static String nombre; 
    public static String apellido; 
    public static int ci;
    public static String telefono;

    public static void setDatos(int idUsuario, int idtecnico, String username, String rol, String nombre, String apellido, int ci, String telefono) {
        SesionUsuario.idUsuario = idUsuario;
        SesionUsuario.idtecnico = idtecnico;
        SesionUsuario.username = username;
        SesionUsuario.rol = rol;
        SesionUsuario.nombre = nombre;
        SesionUsuario.apellido = apellido;
        SesionUsuario.ci = ci;
        SesionUsuario.telefono = telefono;
    }
}
