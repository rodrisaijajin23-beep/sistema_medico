// Vista: Secretaria (index.html)
// Toda la comunicación con el Servicio Principal pasa por estas llamadas fetch
// hacia la API REST expuesta en /api/*. No hay lógica de negocio aquí: solo
// se arma la petición, se manda y se pinta la respuesta (patrón MVC: esto es la Vista).

const API_BASE = "api";

document.addEventListener("DOMContentLoaded", () => {
    cargarPacientes();
    cargarMedicos();
    cargarCitas();

    document.getElementById("formPaciente").addEventListener("submit", registrarPaciente);
    document.getElementById("formCita").addEventListener("submit", registrarCita);
    document.getElementById("medicoSelect").addEventListener("change", cargarHorariosDelMedico);
});

async function cargarPacientes() {
    try {
        const resp = await fetch(`${API_BASE}/pacientes`);
        const pacientes = await resp.json();
        const select = document.getElementById("pacienteSelect");
        select.innerHTML = '<option value="">Seleccione un paciente...</option>';
        pacientes.forEach(p => {
            const opt = document.createElement("option");
            opt.value = p.idPaciente;
            opt.textContent = `${p.dni} - ${p.nombreCompleto}`;
            select.appendChild(opt);
        });
    } catch (err) {
        console.error("No se pudo cargar la lista de pacientes:", err);
    }
}

async function cargarMedicos() {
    try {
        const resp = await fetch(`${API_BASE}/medicos`);
        const medicos = await resp.json();
        const select = document.getElementById("medicoSelect");
        select.innerHTML = '<option value="">Seleccione un médico...</option>';
        medicos.forEach(m => {
            const opt = document.createElement("option");
            opt.value = m.idMedico;
            opt.textContent = `${m.nombreCompleto} (${m.especialidad})`;
            select.appendChild(opt);
        });
    } catch (err) {
        console.error("No se pudo cargar la lista de médicos:", err);
    }
}

async function cargarHorariosDelMedico() {
    const idMedico = document.getElementById("medicoSelect").value;
    const select = document.getElementById("horarioSelect");
    select.innerHTML = '<option value="">Cargando...</option>';

    if (!idMedico) {
        select.innerHTML = '<option value="">Seleccione un médico primero...</option>';
        return;
    }

    try {
        const resp = await fetch(`${API_BASE}/horarios?disponibles=true`);
        const horarios = await resp.json();
        const delMedico = horarios.filter(h => String(h.idMedico) === String(idMedico));

        select.innerHTML = '<option value="">Seleccione un horario...</option>';
        delMedico.forEach(h => {
            const opt = document.createElement("option");
            opt.value = h.idHorario;
            opt.textContent = `${h.fecha} ${h.hora}`;
            select.appendChild(opt);
        });
        if (delMedico.length === 0) {
            select.innerHTML = '<option value="">Este médico no tiene horarios disponibles</option>';
        }
    } catch (err) {
        console.error("No se pudo cargar los horarios:", err);
        select.innerHTML = '<option value="">Error al cargar horarios</option>';
    }
}

async function registrarPaciente(evento) {
    evento.preventDefault();
    const form = evento.target;
    const datos = new URLSearchParams(new FormData(form));

    try {
        const resp = await fetch(`${API_BASE}/pacientes`, { method: "POST", body: datos });
        const resultado = await resp.json();
        if (resp.ok) {
            alert(resultado.mensaje);
            form.reset();
            cargarPacientes();
        } else {
            alert("Error: " + resultado.mensaje);
        }
    } catch (err) {
        alert("No se pudo conectar con el servidor. Intente nuevamente.");
        console.error(err);
    }
}

async function registrarCita(evento) {
    evento.preventDefault();
    const form = evento.target;
    const datos = new URLSearchParams(new FormData(form));

    try {
        const resp = await fetch(`${API_BASE}/citas`, { method: "POST", body: datos });
        const resultado = await resp.json();
        if (resp.ok) {
            alert(resultado.mensaje + (resultado.notificacionEnviada ? "" : " (aviso: la notificación no se pudo enviar, pero la cita quedó registrada)"));
            form.reset();
            document.getElementById("horarioSelect").innerHTML = '<option value="">Seleccione un médico primero...</option>';
            cargarCitas();
        } else {
            alert("Error: " + resultado.mensaje);
        }
    } catch (err) {
        alert("No se pudo conectar con el servidor. Intente nuevamente.");
        console.error(err);
    }
}

async function cargarCitas() {
    const tbody = document.getElementById("citasTableBody");
    try {
        const resp = await fetch(`${API_BASE}/citas`);
        const citas = await resp.json();

        document.getElementById("contadorCitas").textContent = citas.length;
        document.getElementById("contadorPendientes").textContent =
            citas.filter(c => c.estado === "Pendiente").length;

        if (citas.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6">No hay citas registradas todavía</td></tr>';
            return;
        }

        tbody.innerHTML = "";
        citas.forEach(c => {
            const badgeClase = c.estado === "Confirmado" ? "confirmed"
                : c.estado === "Cancelado" ? "cancelled" : "pending";

            const fila = document.createElement("tr");
            fila.innerHTML = `
                <td>${c.dniPaciente}</td>
                <td>${c.nombrePaciente}</td>
                <td>${c.nombreMedico}</td>
                <td>${c.fecha} ${c.hora}</td>
                <td><span class="badge ${badgeClase}">${c.estado}</span></td>
                <td>
                    <button class="btn btn-action confirm-btn" data-id="${c.idCita}" data-estado="Confirmado">Confirmar</button>
                    <button class="btn btn-action pending-btn" data-id="${c.idCita}" data-estado="Pendiente">Pendiente</button>
                    <button class="btn btn-action cancel-btn" data-id="${c.idCita}" data-estado="Cancelado">Cancelar</button>
                </td>
            `;
            tbody.appendChild(fila);
        });

        tbody.querySelectorAll("button[data-id]").forEach(btn => {
            btn.addEventListener("click", () => cambiarEstadoCita(btn.dataset.id, btn.dataset.estado));
        });
    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="6">No se pudo conectar con el servidor</td></tr>';
        console.error(err);
    }
}

async function cambiarEstadoCita(idCita, nuevoEstado) {
    try {
        // El cambio clave: Enviar idCita y estado directamente en la URL 
        // para que Tomcat los pueda leer con request.getParameter()
        const resp = await fetch(`${API_BASE}/citas?idCita=${idCita}&estado=${nuevoEstado}`, { 
            method: "PUT" 
        });
        
        const resultado = await resp.json();
        if (resp.ok) {
            cargarCitas();
        } else {
            alert("Error: " + resultado.mensaje);
        }
    } catch (err) {
        alert("No se pudo conectar con el servidor. Intente nuevamente.");
        console.error(err);
    }
}
