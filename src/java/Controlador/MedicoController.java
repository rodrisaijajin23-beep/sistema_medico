/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

/**
 *
 * @author Rodrigo
 */

import dao.MedicoDAO;
import dto.Medico;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// GET  /api/medicos  -> listar medicos (llena el <select> de index.html)
// POST /api/medicos  -> registrar un medico nuevo (medico.html)
@WebServlet(name = "MedicoController", urlPatterns = {"/api/medicos"})
public class MedicoController extends HttpServlet {

    MedicoDAO medicoDAO = new MedicoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        List<Medico> medicos = medicoDAO.listarMedicos();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < medicos.size(); i++) {
            Medico m = medicos.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"idMedico\":").append(m.getIdMedico()).append(",")
                .append("\"dni\":\"").append(escapar(m.getDni())).append("\",")
                .append("\"nombreCompleto\":\"").append(escapar(m.getNombreCompleto())).append("\",")
                .append("\"cmp\":\"").append(escapar(m.getCmp())).append("\",")
                .append("\"especialidad\":\"").append(escapar(m.getEspecialidad())).append("\"")
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
            String nombreCompleto = request.getParameter("nombreMedico");
            String dni = request.getParameter("dniMedico");
            String cmp = request.getParameter("cmpMedico");
            String especialidad = request.getParameter("especialidad");

            if (medicoDAO.registrarMedico(nombreCompleto, dni, cmp, especialidad)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print("{\"mensaje\":\"Medico registrado con exito\"}");
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

    private String escapar(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
