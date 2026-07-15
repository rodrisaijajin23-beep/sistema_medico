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
import dto.Cita;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CitaDAO {

    conexion cn = new conexion();

    public boolean registrarCita(Cita cita) {
        String sql = "INSERT INTO Citas (ID_Paciente, ID_Horario, Modalidad) VALUES (?, ?, ?)";
        try (Connection con = cn.getConexion()) {
            if (con == null) {
                return false; // BD no disponible: fallback controlado
            }
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, cita.getIdPaciente());
                ps.setInt(2, cita.getIdHorario());
                ps.setString(3, cita.getModalidad());

                int filasAfectadas = ps.executeUpdate();

                if (filasAfectadas > 0) {
                    String updateHorario = "UPDATE Horarios SET Disponible = 0 WHERE ID_Horario = ?";
                    try (PreparedStatement ps2 = con.prepareStatement(updateHorario)) {
                        ps2.setInt(1, cita.getIdHorario());
                        ps2.executeUpdate();
                    }
                    con.commit();
                    return true;
                }
                con.rollback();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Error al registrar cita: " + e.getMessage());
        }
        return false;
    }

    public boolean actualizarEstado(int idCita, String nuevoEstado) {
        String sql = "UPDATE Citas SET Estado = ? WHERE ID_Cita = ?";
        try (Connection con = cn.getConexion()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, idCita);

                int filasAfectadas = ps.executeUpdate();

                if (filasAfectadas > 0 && "Cancelado".equals(nuevoEstado)) {
                    String updateHorario = "UPDATE Horarios SET Disponible = 1 WHERE ID_Horario = "
                            + "(SELECT ID_Horario FROM Citas WHERE ID_Cita = ?)";
                    try (PreparedStatement ps2 = con.prepareStatement(updateHorario)) {
                        ps2.setInt(1, idCita);
                        ps2.executeUpdate();
                    }
                }
                return filasAfectadas > 0;
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar estado: " + e.getMessage());
            return false;
        }
    }

    // Listado enriquecido con JOIN: es lo que pinta la tabla de index.html
    public List<Cita> listarCitas() {
        List<Cita> lista = new ArrayList<>();
        String sql = "SELECT c.ID_Cita, c.ID_Paciente, c.ID_Horario, c.Modalidad, c.Estado, "
                + "p.DNI AS DniPaciente, p.NombreCompleto AS NombrePaciente, "
                + "u.Nombre AS NombreMedico, h.Fecha, h.Hora "
                + "FROM Citas c "
                + "INNER JOIN Pacientes p ON c.ID_Paciente = p.ID_Paciente "
                + "INNER JOIN Horarios h ON c.ID_Horario = h.ID_Horario "
                + "INNER JOIN Medicos m ON h.ID_Medico = m.ID_Medico "
                + "INNER JOIN Usuarios u ON m.ID_Usuario = u.ID_Usuario "
                + "ORDER BY c.ID_Cita DESC";
        try (Connection con = cn.getConexion()) {
            if (con == null) {
                return lista; // lista vacia: la Vista puede seguir mostrando "sin datos"
            }
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cita c = new Cita();
                    c.setIdCita(rs.getInt("ID_Cita"));
                    c.setIdPaciente(rs.getInt("ID_Paciente"));
                    c.setIdHorario(rs.getInt("ID_Horario"));
                    c.setModalidad(rs.getString("Modalidad"));
                    c.setEstado(rs.getString("Estado"));
                    c.setDniPaciente(rs.getString("DniPaciente"));
                    c.setNombrePaciente(rs.getString("NombrePaciente"));
                    c.setNombreMedico(rs.getString("NombreMedico"));
                    c.setFecha(rs.getDate("Fecha").toString());
                    c.setHora(rs.getTime("Hora").toString());
                    lista.add(c);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al listar citas: " + e.getMessage());
        }
        return lista;
    }

    public boolean eliminarCita(int idCita) {
        String sql = "DELETE FROM Citas WHERE ID_Cita = ?";
        try (Connection con = cn.getConexion()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idCita);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Error al eliminar cita: " + e.getMessage());
            return false;
        }
    }
}
