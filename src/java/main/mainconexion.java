/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package main;

import config.conexion;

/**
 *
 * @author Rodrigo
 */
public class mainconexion {

    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) {
        conexion con = new conexion();
        con.getConexion();
    }
    
}
