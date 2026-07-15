/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Rodrigo
 */
import config.conexion;
import dto.Paciente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    conexion cn = new conexion();

    public boolean registrarPaciente(Paciente p) {
        String sql = "INSERT INTO Pacientes (DNI, NombreCompleto, Correo, Telefono) VALUES (?, ?, ?, ?)";
        try (Connection con = cn.getConexion()) {
            if (con == null) {
                return false; // fallback controlado: BD no disponible
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, p.getDni());
                ps.setString(2, p.getNombreCompleto());
                ps.setString(3, p.getCorreo());
                ps.setString(4, p.getTelefono());
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Error al registrar paciente: " + e.getMessage());
            return false;
        }
    }

    public List<Paciente> listarPacientes() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT ID_Paciente, DNI, NombreCompleto, Correo, Telefono FROM Pacientes ORDER BY ID_Paciente DESC";
        try (Connection con = cn.getConexion()) {
            if (con == null) {
                return lista; // lista vacia en vez de romper la Vista
            }
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Paciente p = new Paciente();
                    p.setIdPaciente(rs.getInt("ID_Paciente"));
                    p.setDni(rs.getString("DNI"));
                    p.setNombreCompleto(rs.getString("NombreCompleto"));
                    p.setCorreo(rs.getString("Correo"));
                    p.setTelefono(rs.getString("Telefono"));
                    lista.add(p);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al listar pacientes: " + e.getMessage());
        }
        return lista;
    }
}
