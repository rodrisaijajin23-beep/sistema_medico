/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

/**
 *
 * @author Rodrigo
 */

import dao.PacienteDAO;
import dto.Paciente;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "PacienteController", urlPatterns = {"/api/pacientes"})
public class PacienteController extends HttpServlet {

    PacienteDAO pacienteDAO = new PacienteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        List<Paciente> pacientes = pacienteDAO.listarPacientes();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < pacientes.size(); i++) {
            Paciente p = pacientes.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"idPaciente\":").append(p.getIdPaciente()).append(",")
                .append("\"dni\":\"").append(p.getDni()).append("\",")
                .append("\"nombreCompleto\":\"").append(p.getNombreCompleto()).append("\"")
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
            Paciente p = new Paciente();
            p.setDni(request.getParameter("dni"));
            p.setNombreCompleto(request.getParameter("nombreCompleto"));
            p.setCorreo(request.getParameter("correo"));
            p.setTelefono(request.getParameter("telefono"));

            if (pacienteDAO.registrarPaciente(p)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print("{\"mensaje\":\"Paciente registrado con éxito\"}");
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
}