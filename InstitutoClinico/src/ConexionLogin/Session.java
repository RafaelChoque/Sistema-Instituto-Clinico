/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ConexionLogin;

/**
 *
 * @author Erlan
 */
public class Session {
    private static int idUsuarioActual = -1;
    private static String nombreUsuarioActual = null;
    private static String rolUsuarioActual = null;
    private static String nombreCompleto;

    public static void setUsuario(int id, String nombre, String rol) {
        idUsuarioActual = id;
        nombreUsuarioActual = nombre;
        rolUsuarioActual = rol;
    }

    public static int getIdUsuario() {
        return idUsuarioActual;
    }

    public static String getNombreUsuario() {
        return nombreUsuarioActual;
    }

    public static String getRolUsuario() {
        return rolUsuarioActual;
    }
    
    public static void setNombreCompleto(String nombre) {
        nombreCompleto = nombre;
    }

    public static String getNombreCompleto() {
        return nombreCompleto;
    }
    
}