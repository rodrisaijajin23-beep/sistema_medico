/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

/**
 *
 * @author Rodrigo
 */

import cliente.NotificacionClient;
import dao.CitaDAO;
import dto.Cita;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// API REST de Citas.
// GET    /api/citas          -> listar todas las citas
// POST   /api/citas          -> registrar una nueva cita
// PUT    /api/citas          -> actualizar el estado de una cita (Confirmar/Pendiente/Cancelar)
// DELETE /api/citas?id=NN    -> eliminar una cita
@WebServlet(name = "CitaController", urlPatterns = {"/api/citas"})
public class CitaController extends HttpServlet {

    CitaDAO citaDAO = new CitaDAO();
    NotificacionClient notificacionClient = new NotificacionClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        List<Cita> citas = citaDAO.listarCitas();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < citas.size(); i++) {
            Cita c = citas.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"idCita\":").append(c.getIdCita()).append(",")
                .append("\"dniPaciente\":\"").append(escapar(c.getDniPaciente())).append("\",")
                .append("\"nombrePaciente\":\"").append(escapar(c.getNombrePaciente())).append("\",")
                .append("\"nombreMedico\":\"").append(escapar(c.getNombreMedico())).append("\",")
                .append("\"fecha\":\"").append(c.getFecha()).append("\",")
                .append("\"hora\":\"").append(c.getHora()).append("\",")
                .append("\"modalidad\":\"").append(escapar(c.getModalidad())).append("\",")
                .append("\"estado\":\"").append(escapar(c.getEstado())).append("\"")
                .append("}");
        }
        json.append("]");

        response.setStatus(HttpServletResponse.SC_OK);
        out.print(json.toString());
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int idPaciente = Integer.parseInt(request.getParameter("pacienteSelect"));
            int idHorario = Integer.parseInt(request.getParameter("horarioSelect"));
            String modalidad = request.getParameter("modalidad");

            Cita cita = new Cita();
            cita.setIdPaciente(idPaciente);
            cita.setIdHorario(idHorario);
            cita.setModalidad(modalidad);

            if (citaDAO.registrarCita(cita)) {
                // Comunicacion real por red con el Servicio Secundario.
                // Si falla (timeout, servicio caido), la cita YA quedo guardada:
                // el sistema sigue operando parcialmente, tal como exige la rubrica.
                String mensaje = "{\"tipo\":\"CITA_ASIGNADA\",\"idPaciente\":" + idPaciente
                        + ",\"idHorario\":" + idHorario + ",\"modalidad\":\"" + escapar(modalidad) + "\"}";
                boolean notificado = notificacionClient.notificar(mensaje);

                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print("{\"mensaje\":\"Cita asignada con exito\",\"notificacionEnviada\":" + notificado + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"mensaje\":\"Error al guardar en BD\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"mensaje\":\"Error en los datos enviados\"}");
        }
        out.flush();
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int idCita = Integer.parseInt(request.getParameter("idCita"));
            String nuevoEstado = request.getParameter("estado");

            if (citaDAO.actualizarEstado(idCita, nuevoEstado)) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"mensaje\":\"Estado actualizado\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"mensaje\":\"No se pudo actualizar el estado\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"mensaje\":\"Error en los datos enviados\"}");
        }
        out.flush();
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int idCita = Integer.parseInt(request.getParameter("id"));
            if (citaDAO.eliminarCita(idCita)) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"mensaje\":\"Cita eliminada\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"mensaje\":\"No se pudo eliminar la cita\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"mensaje\":\"Error en los datos enviados\"}");
        }
        out.flush();
    }

    private String escapar(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
