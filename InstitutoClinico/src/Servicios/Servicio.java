/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Servicios;

/**
 *
 * @author Erlan
 */
public class Servicio {

    private String nombre;
    private double precio;
    private String tipoPrecio; 

    public Servicio(String nombre, double precio, String tipoPrecio) {
        this.nombre = nombre;
        this.precio = precio;
        this.tipoPrecio = tipoPrecio;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public String getTipoPrecio() {
        return tipoPrecio;
    }

    @Override
    public String toString() {
        return nombre + " (" + tipoPrecio + ") - Bs. " + precio;
    }
}
