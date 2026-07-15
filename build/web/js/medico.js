// Vista: Médico (medico.html)
// Igual que app.js: solo arma peticiones REST y pinta resultados (Vista del MVC).

const API_BASE = "api";

document.addEventListener("DOMContentLoaded", () => {
    cargarMedicosEnSelect();

    document.getElementById("formRegistrarMedico").addEventListener("submit", registrarMedico);
    document.getElementById("formConfigurarHorario").addEventListener("submit", registrarHorario);
    document.getElementById("medicoActivoSelect").addEventListener("change", cargarMisHorarios);
});

async function cargarMedicosEnSelect() {
    try {
        const resp = await fetch(`${API_BASE}/medicos`);
        const medicos = await resp.json();
        const select = document.getElementById("medicoActivoSelect");
        select.innerHTML = '<option value="">Seleccione su usuario de médico...</option>';
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

async function registrarMedico(evento) {
    evento.preventDefault();
    const form = evento.target;
    const datos = new URLSearchParams(new FormData(form));

    try {
        const resp = await fetch(`${API_BASE}/medicos`, { method: "POST", body: datos });
        const resultado = await resp.json();
        if (resp.ok) {
            alert(resultado.mensaje);
            form.reset();
            cargarMedicosEnSelect();
        } else {
            alert("Error: " + resultado.mensaje);
        }
    } catch (err) {
        alert("No se pudo conectar con el servidor. Intente nuevamente.");
        console.error(err);
    }
}

async function registrarHorario(evento) {
    evento.preventDefault();
    const form = evento.target;
    const idMedico = document.getElementById("medicoActivoSelect").value;

    if (!idMedico) {
        alert("Primero seleccione su usuario de médico.");
        return;
    }

    const datos = new URLSearchParams(new FormData(form));

    try {
        const resp = await fetch(`${API_BASE}/horarios`, { method: "POST", body: datos });
        const resultado = await resp.json();
        if (resp.ok) {
            alert(resultado.mensaje);
            document.getElementById("fechaDisponibilidad").value = "";
            document.getElementById("horaDisponibilidad").value = "";
            cargarMisHorarios();
        } else {
            alert("Error: " + resultado.mensaje);
        }
    } catch (err) {
        alert("No se pudo conectar con el servidor. Intente nuevamente.");
        console.error(err);
    }
}

async function cargarMisHorarios() {
    const idMedico = document.getElementById("medicoActivoSelect").value;
    const tbody = document.getElementById("horariosTableBody");

    if (!idMedico) {
        tbody.innerHTML = '<tr><td colspan="4">Seleccione un médico para ver sus horarios</td></tr>';
        return;
    }

    try {
        const resp = await fetch(`${API_BASE}/horarios?idMedico=${idMedico}`);
        const horarios = await resp.json();

        if (horarios.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4">No hay horarios registrados</td></tr>';
            return;
        }

        tbody.innerHTML = "";
        horarios.forEach(h => {
            const fila = document.createElement("tr");
            fila.innerHTML = `
                <td>${h.fecha}</td>
                <td>${h.hora}</td>
                <td>${h.disponible ? "Disponible" : "Ocupado"}</td>
                <td>${h.disponible
                    ? `<button class="btn btn-action pending-btn" data-id="${h.idHorario}">Eliminar</button>`
                    : "-"}</td>
            `;
            tbody.appendChild(fila);
        });

        tbody.querySelectorAll("button[data-id]").forEach(btn => {
            btn.addEventListener("click", () => eliminarHorario(btn.dataset.id));
        });
    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="4">No se pudo conectar con el servidor</td></tr>';
        console.error(err);
    }
}

async function eliminarHorario(idHorario) {
    if (!confirm("¿Eliminar este horario?")) return;

    try {
        const resp = await fetch(`${API_BASE}/horarios?id=${idHorario}`, { method: "DELETE" });
        const resultado = await resp.json();
        if (resp.ok) {
            cargarMisHorarios();
        } else {
            alert("Error: " + resultado.mensaje);
        }
    } catch (err) {
        alert("No se pudo conectar con el servidor. Intente nuevamente.");
        console.error(err);
    }
}
