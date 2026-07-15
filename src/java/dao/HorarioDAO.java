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
import dto.Horario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class HorarioDAO {

    conexion cn = new conexion();

    public boolean registrarHorario(Horario h) {
        String sql = "INSERT INTO Horarios (ID_Medico, Fecha, Hora, Disponible) VALUES (?, ?, ?, 1)";
        try (Connection con = cn.getConexion()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, h.getIdMedico());
                ps.setString(2, h.getFecha());
                ps.setString(3, h.getHora());
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Error al registrar horario: " + e.getMessage());
            return false;
        }
    }

    // Solo horarios de un medico especifico (usado por medico.html para pintar "Mis horarios")
    public List<Horario> listarPorMedico(int idMedico) {
        List<Horario> lista = new ArrayList<>();
        String sql = "SELECT ID_Horario, ID_Medico, Fecha, Hora, Disponible FROM Horarios "
                + "WHERE ID_Medico = ? ORDER BY Fecha, Hora";
        try (Connection con = cn.getConexion()) {
            if (con == null) {
                return lista;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idMedico);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapear(rs));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al listar horarios por medico: " + e.getMessage());
        }
        return lista;
    }

    // Solo horarios libres de todos los medicos (usado por index.html para armar citas)
    public List<Horario> listarDisponibles() {
        List<Horario> lista = new ArrayList<>();
        String sql = "SELECT ID_Horario, ID_Medico, Fecha, Hora, Disponible FROM Horarios "
                + "WHERE Disponible = 1 ORDER BY Fecha, Hora";
        try (Connection con = cn.getConexion()) {
            if (con == null) {
                return lista;
            }
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al listar horarios disponibles: " + e.getMessage());
        }
        return lista;
    }

    public boolean eliminarHorario(int idHorario) {
        String sql = "DELETE FROM Horarios WHERE ID_Horario = ? AND Disponible = 1";
        try (Connection con = cn.getConexion()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idHorario);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            // Puede fallar por FK si el horario ya tiene una cita asociada; se informa
            // como fallo controlado en vez de romper el servlet.
            System.err.println("Error al eliminar horario: " + e.getMessage());
            return false;
        }
    }

    private Horario mapear(ResultSet rs) throws Exception {
        Horario h = new Horario();
        h.setIdHorario(rs.getInt("ID_Horario"));
        h.setIdMedico(rs.getInt("ID_Medico"));
        h.setFecha(rs.getDate("Fecha").toString());
        h.setHora(rs.getTime("Hora").toString());
        h.setDisponible(rs.getBoolean("Disponible"));
        return h;
    }
}
