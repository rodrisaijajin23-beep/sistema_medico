/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

/**
 *
 * @author Rodrigo
 */

import dao.HorarioDAO;
import dto.Horario;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// GET    /api/horarios?idMedico=NN&disponibles=true  -> listar horarios
//        - con idMedico: horarios de ese medico (medico.html, tabla "Mis horarios")
//        - con disponibles=true: horarios libres de todos los medicos (index.html, select)
// POST   /api/horarios   -> el medico agrega disponibilidad
// DELETE /api/horarios?id=NN -> el medico elimina un horario
@WebServlet(name = "HorarioController", urlPatterns = {"/api/horarios"})
public class HorarioController extends HttpServlet {

    HorarioDAO horarioDAO = new HorarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        List<Horario> horarios;
        String idMedicoParam = request.getParameter("idMedico");
        String disponiblesParam = request.getParameter("disponibles");

        if (idMedicoParam != null && !idMedicoParam.isEmpty()) {
            horarios = horarioDAO.listarPorMedico(Integer.parseInt(idMedicoParam));
        } else if ("true".equalsIgnoreCase(disponiblesParam)) {
            horarios = horarioDAO.listarDisponibles();
        } else {
            horarios = horarioDAO.listarDisponibles();
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < horarios.size(); i++) {
            Horario h = horarios.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"idHorario\":").append(h.getIdHorario()).append(",")
                .append("\"idMedico\":").append(h.getIdMedico()).append(",")
                .append("\"fecha\":\"").append(h.getFecha()).append("\",")
                .append("\"hora\":\"").append(h.getHora()).append("\",")
                .append("\"disponible\":").append(h.isDisponible())
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
            Horario h = new Horario();
            h.setIdMedico(Integer.parseInt(request.getParameter("idMedico")));
            h.setFecha(request.getParameter("fechaDisponibilidad"));
            h.setHora(request.getParameter("horaDisponibilidad"));

            if (horarioDAO.registrarHorario(h)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print("{\"mensaje\":\"Horario agregado con exito\"}");
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
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int idHorario = Integer.parseInt(request.getParameter("id"));
            if (horarioDAO.eliminarHorario(idHorario)) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"mensaje\":\"Horario eliminado\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"mensaje\":\"No se pudo eliminar (verifique que no tenga una cita asociada)\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"mensaje\":\"Error en los datos enviados\"}");
        }
        out.flush();
    }
}
