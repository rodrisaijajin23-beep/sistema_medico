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
import dto.Medico;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedicoDAO {

    conexion cn = new conexion();

    // Registra el Usuario (Rol = 'Medico') y el Medico en una sola transaccion.
    public boolean registrarMedico(String nombreCompleto, String dni, String cmp, String especialidad) {
        String sqlUsuario = "INSERT INTO Usuarios (Nombre, Correo, Rol) VALUES (?, ?, 'Medico')";
        String sqlMedico = "INSERT INTO Medicos (ID_Usuario, DNI, CMP, Especialidad) VALUES (?, ?, ?, ?)";

        try (Connection con = cn.getConexion()) {
            if (con == null) {
                return false;
            }
            con.setAutoCommit(false);
            try {
                int idUsuario;
                // Se usa un correo generado a partir del DNI porque el formulario de la
                // Vista (medico.html) no pide correo; se mantiene compatible con el
                // esquema de Usuarios que sí lo exige como UNIQUE NOT NULL.
                String correoGenerado = dni + "@clinica.com";
                try (PreparedStatement ps1 = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                    ps1.setString(1, nombreCompleto);
                    ps1.setString(2, correoGenerado);
                    ps1.executeUpdate();
                    try (ResultSet rs = ps1.getGeneratedKeys()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        idUsuario = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps2 = con.prepareStatement(sqlMedico)) {
                    ps2.setInt(1, idUsuario);
                    ps2.setString(2, dni);
                    ps2.setString(3, cmp);
                    ps2.setString(4, especialidad);
                    ps2.executeUpdate();
                }
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                System.err.println("Error al registrar medico, rollback aplicado: " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error de conexion al registrar medico: " + e.getMessage());
            return false;
        }
    }

    public List<Medico> listarMedicos() {
        List<Medico> lista = new ArrayList<>();
        String sql = "SELECT m.ID_Medico, m.ID_Usuario, m.DNI, m.CMP, m.Especialidad, u.Nombre "
                + "FROM Medicos m INNER JOIN Usuarios u ON m.ID_Usuario = u.ID_Usuario "
                + "ORDER BY m.ID_Medico DESC";
        try (Connection con = cn.getConexion()) {
            if (con == null) {
                return lista;
            }
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Medico m = new Medico();
                    m.setIdMedico(rs.getInt("ID_Medico"));
                    m.setIdUsuario(rs.getInt("ID_Usuario"));
                    m.setDni(rs.getString("DNI"));
                    m.setCmp(rs.getString("CMP"));
                    m.setEspecialidad(rs.getString("Especialidad"));
                    m.setNombreCompleto(rs.getString("Nombre"));
                    lista.add(m);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al listar medicos: " + e.getMessage());
        }
        return lista;
    }
}
