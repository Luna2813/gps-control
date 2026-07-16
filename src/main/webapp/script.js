const API_CLIENTES = "api/clientes";
const API_VEHICULOS = "api/vehiculos";
const API_AUTH = "api/auth";
const API_USUARIOS = "api/usuarios";
const API_AUDITORIA = "api/auditoria";
const API_NOTIFICACIONES = "api/notificaciones";
const API_PUSH = "api/push";

let csrfToken = "";
let usuarioActualId = null;
let usuarioActualRol = "";

document.addEventListener("DOMContentLoaded", () => {
    document
        .getElementById("formCliente")
        .addEventListener("submit", guardarCliente);

    document
        .getElementById("formLogin")
        .addEventListener("submit", iniciarSesion);

    document
        .getElementById("formUsuario")
        .addEventListener("submit", crearUsuario);

    comprobarSesion();
});

// =====================================
// AUTENTICACIÓN
// =====================================

async function comprobarSesion() {
    try {
        const response = await fetch(`${API_AUTH}/session`, {
            credentials: "same-origin"
        });
        const sesion = await leerRespuesta(response);
        mostrarAplicacion(sesion);
    } catch {
        mostrarLogin();
    }
}

async function iniciarSesion(evento) {
    evento.preventDefault();

    const error = document.getElementById("loginError");
    error.textContent = "";

    const credenciales = {
        usuario: document.getElementById("loginUsuario").value.trim(),
        password: document.getElementById("loginPassword").value
    };

    try {
        const response = await fetch(`${API_AUTH}/login`, {
            method: "POST",
            credentials: "same-origin",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(credenciales)
        });

        const sesion = await leerRespuesta(response);
        document.getElementById("formLogin").reset();
        mostrarAplicacion(sesion);
    } catch (e) {
        error.textContent = e.message;
    }
}

async function cerrarSesion() {
    try {
        const response = await fetch(`${API_AUTH}/logout`, {
            method: "POST",
            credentials: "same-origin",
            headers: { "X-CSRF-Token": csrfToken }
        });
        await leerRespuesta(response);
    } finally {
        csrfToken = "";
        limpiarTodo();
        mostrarLogin();
    }
}

function mostrarAplicacion(sesion) {
    csrfToken = sesion.csrfToken;
    usuarioActualId = Number(sesion.id);
    usuarioActualRol = sesion.rol;
    document.getElementById("usuarioActual").textContent =
        `${sesion.nombre} (${sesion.rol})`;
    document.getElementById("loginView").classList.add("oculto");
    document.getElementById("appView").classList.remove("oculto");

    const contenedor = document.getElementById("contenedorVehiculos");
    if (contenedor.children.length === 0) {
        agregarVehiculoFormulario();
    }

    const panelUsuarios = document.getElementById("adminUsuarios");
    if (sesion.rol === "ADMIN") {
        panelUsuarios.classList.remove("oculto");
        cargarUsuarios();
        cargarAuditoria();
        cargarNotificaciones();
    } else {
        panelUsuarios.classList.add("oculto");
    }
}

function mostrarLogin() {
    document.getElementById("appView").classList.add("oculto");
    document.getElementById("loginView").classList.remove("oculto");
    document.getElementById("adminUsuarios").classList.add("oculto");
    document.getElementById("loginUsuario").focus();
}

function headersJsonSeguros() {
    return {
        "Content-Type": "application/json",
        "X-CSRF-Token": csrfToken
    };
}

function escaparHtml(valor) {
    return String(valor ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

async function cargarNotificaciones() {
    const lista = document.getElementById("listaNotificaciones");
    try {
        const response = await fetch(API_NOTIFICACIONES);
        const notificaciones = await leerRespuesta(response);
        const pendientes = notificaciones.filter(n => !n.leida).length;
        document.getElementById("contadorNotificaciones").textContent = pendientes;
        lista.innerHTML = notificaciones.length === 0
            ? '<p class="sin-notificaciones">No hay notificaciones.</p>'
            : notificaciones.map(n => `
                <article class="notificacion ${n.leida ? "leida" : "pendiente"}">
                    <div>
                        <strong>${escaparHtml(n.titulo)}</strong>
                        <p>${escaparHtml(n.mensaje)}</p>
                        <small>Fecha del evento: ${escaparHtml(n.fechaEvento)}</small>
                    </div>
                    ${n.leida ? '<span>Leída</span>' : `
                        <button type="button" onclick="marcarNotificacionLeida(${n.id})">
                            Marcar como leída
                        </button>`}
                </article>`).join("");
    } catch (e) {
        lista.innerHTML = `<p class="login-error">${escaparHtml(e.message)}</p>`;
    }
}

async function marcarNotificacionLeida(id) {
    try {
        const response = await fetch(`${API_NOTIFICACIONES}/${id}/leida`, {
            method: "PUT",
            headers: headersJsonSeguros()
        });
        await leerRespuesta(response);
        cargarNotificaciones();
    } catch (e) {
        alert(e.message);
    }
}

function alternarNotificaciones() {
    alternarPanel("contenedorNotificaciones", "btnAlternarNotificaciones");
}

function alternarAuditoria() {
    alternarPanel("contenedorAuditoria", "btnAlternarAuditoria");
}

function alternarPanel(idPanel, idBoton) {
    const panel = document.getElementById(idPanel);
    const boton = document.getElementById(idBoton);
    const estaOculto = panel.classList.toggle("oculto");
    boton.textContent = estaOculto ? "Mostrar" : "Ocultar";
    boton.setAttribute("aria-expanded", String(!estaOculto));
}

async function activarNotificacionesPush() {
    if (!("serviceWorker" in navigator) || !("PushManager" in window)) {
        alert("Este navegador no admite notificaciones push.");
        return;
    }

    try {
        const permiso = await Notification.requestPermission();
        if (permiso !== "granted") {
            throw new Error("El permiso de notificaciones no fue autorizado");
        }

        const registro = await navigator.serviceWorker.register("service-worker.js");
        const respuestaClave = await fetch(`${API_PUSH}/clave-publica`);
        const datosClave = await leerRespuesta(respuestaClave);
        const claveServidor = base64UrlAUint8Array(datosClave.clavePublica);
        let suscripcion = await registro.pushManager.getSubscription();
        let endpointAnterior = null;

        // El botón también sirve para renovar un endpoint que el proveedor
        // haya vencido (HTTP 404/410). Por eso una activación solicitada por
        // el usuario reemplaza siempre la suscripción anterior del navegador.
        if (suscripcion) {
            endpointAnterior = suscripcion.endpoint;
            const cancelada = await suscripcion.unsubscribe();
            if (!cancelada) {
                throw new Error("No se pudo renovar la suscripción anterior");
            }
            suscripcion = null;
        }

        if (!suscripcion) {
            suscripcion = await registro.pushManager.subscribe({
                userVisibleOnly: true,
                applicationServerKey: claveServidor
            });
        }

        const json = suscripcion.toJSON();
        const response = await fetch(`${API_PUSH}/suscripciones`, {
            method: "POST",
            headers: headersJsonSeguros(),
            body: JSON.stringify({
                endpoint: json.endpoint,
                p256dh: json.keys.p256dh,
                auth: json.keys.auth,
                endpointAnterior
            })
        });
        const resultado = await leerRespuesta(response);
        alert(resultado.mensaje);
    } catch (e) {
        alert(e.message);
    }
}

function base64UrlAUint8Array(valor) {
    const limpia = String(valor || "")
        .trim()
        .replace(/^VAPID_PUBLIC_KEY=/, "")
        .replace(/\s/g, "");
    if (!/^[A-Za-z0-9_-]+$/.test(limpia)) {
        throw new Error("La clave pública VAPID tiene un formato inválido");
    }
    if (limpia.length !== 87) {
        throw new Error(
            `La clave pública VAPID está incompleta (tiene ${limpia.length} caracteres y debe tener 87)`
        );
    }
    const relleno = "=".repeat((4 - limpia.length % 4) % 4);
    const base64 = (limpia + relleno).replaceAll("-", "+").replaceAll("_", "/");
    const bytes = atob(base64);
    return Uint8Array.from(bytes, caracter => caracter.charCodeAt(0));
}

// =====================================
// ADMINISTRACIÓN DE USUARIOS
// =====================================

async function cargarUsuarios() {
    try {
        const response = await fetch(API_USUARIOS);
        const usuarios = await leerRespuesta(response);
        const cuerpo = document.getElementById("tablaUsuariosBody");
        cuerpo.innerHTML = "";

        usuarios.forEach(usuario => {
            const esActual = Number(usuario.id) === usuarioActualId;
            const siguienteEstado = !usuario.activo;
            cuerpo.innerHTML += `
                <tr>
                    <td>${numeroSeguro(usuario.id)}</td>
                    <td>${escaparHtml(usuario.nombre)}</td>
                    <td>${escaparHtml(usuario.usuario)}</td>
                    <td>
                        <select onchange="cambiarRolUsuario(${usuario.id}, this.value)"
                                ${esActual ? "disabled" : ""}>
                            <option value="OPERADOR" ${usuario.rol === "OPERADOR" ? "selected" : ""}>Operador</option>
                            <option value="ADMIN" ${usuario.rol === "ADMIN" ? "selected" : ""}>Administrador</option>
                        </select>
                    </td>
                    <td>${usuario.activo ? "Activo" : "Inactivo"}</td>
                    <td>
                        <button type="button"
                            onclick="cambiarEstadoUsuario(${usuario.id}, ${siguienteEstado})"
                            ${esActual ? "disabled" : ""}>
                            ${usuario.activo ? "Desactivar" : "Activar"}
                        </button>
                        <button type="button" onclick="restablecerPassword(${usuario.id})">
                            Cambiar contraseña
                        </button>
                    </td>
                </tr>`;
        });
    } catch (e) {
        alert(e.message);
    }
}

async function cargarAuditoria() {
    const cuerpo = document.getElementById("tablaAuditoriaBody");

    try {
        const response = await fetch(`${API_AUDITORIA}?limite=100`);
        const registros = await leerRespuesta(response);

        if (registros.length === 0) {
            cuerpo.innerHTML = '<tr><td colspan="7">Todavía no hay operaciones registradas.</td></tr>';
            return;
        }

        cuerpo.innerHTML = registros.map(registro => {
            const fecha = registro.creadoEn
                ? new Date(registro.creadoEn).toLocaleString("es-GT")
                : "";

            return `
                <tr>
                    <td>${escaparHtml(fecha)}</td>
                    <td>${escaparHtml(registro.usuario)}</td>
                    <td>${escaparHtml(registro.rol)}</td>
                    <td>${escaparHtml(describirAccionAuditoria(registro))}</td>
                    <td>${escaparHtml(`${registro.metodo} /${registro.ruta}`)}</td>
                    <td>${numeroSeguro(registro.estadoHttp)}</td>
                    <td>${escaparHtml(registro.direccionIp)}</td>
                </tr>`;
        }).join("");
    } catch (e) {
        cuerpo.innerHTML = `<tr><td colspan="7">${escaparHtml(e.message)}</td></tr>`;
    }
}

function describirAccionAuditoria(registro) {
    const metodo = String(registro.metodo || "").toUpperCase();
    const partes = String(registro.ruta || "")
        .split("/")
        .filter(Boolean);
    const recurso = partes[0] || "registro";
    const id = /^\d+$/.test(partes[1] || "") ? partes[1] : "";

    const nombres = {
        clientes: "cliente",
        vehiculos: "vehículo",
        instalaciones: "instalación",
        usuarios: "usuario"
    };
    const nombre = nombres[recurso] || recurso;
    const referencia = id ? ` #${id}` : "";

    if (recurso === "vehiculos" && partes[1] === "promociones"
            && partes[2] === "vencidas") {
        const cantidad = /^\d+$/.test(partes[3] || "") ? partes[3] : "";
        return `El sistema finalizó ${cantidad || "una o más"} promoción(es) vencida(s)`;
    }

    if (recurso === "usuarios" && partes[2] === "estado") {
        return `Cambió el estado del usuario${referencia}`;
    }
    if (recurso === "usuarios" && partes[2] === "rol") {
        return `Cambió el rol del usuario${referencia}`;
    }
    if (recurso === "usuarios" && partes[2] === "password") {
        return `Cambió la contraseña del usuario${referencia}`;
    }

    const acciones = {
        POST: "Creó",
        PUT: "Actualizó",
        PATCH: "Actualizó",
        DELETE: "Eliminó"
    };
    return `${acciones[metodo] || metodo} ${nombre}${referencia}`.trim();
}

async function crearUsuario(evento) {
    evento.preventDefault();
    const datos = {
        nombre: document.getElementById("nuevoNombre").value.trim(),
        usuario: document.getElementById("nuevoUsuario").value.trim(),
        password: document.getElementById("nuevoPassword").value,
        rol: document.getElementById("nuevoRol").value
    };

    try {
        const response = await fetch(API_USUARIOS, {
            method: "POST",
            headers: headersJsonSeguros(),
            body: JSON.stringify(datos)
        });
        await leerRespuesta(response);
        document.getElementById("formUsuario").reset();
        alert("Usuario creado correctamente");
        cargarUsuarios();
    } catch (e) {
        alert(e.message);
    }
}

async function cambiarEstadoUsuario(id, activo) {
    const accion = activo ? "activar" : "desactivar";
    if (!confirm(`¿Desea ${accion} este usuario?`)) return;

    try {
        const response = await fetch(`${API_USUARIOS}/${id}/estado`, {
            method: "PUT",
            headers: headersJsonSeguros(),
            body: JSON.stringify({ activo })
        });
        await leerRespuesta(response);
        cargarUsuarios();
    } catch (e) {
        alert(e.message);
    }
}

async function cambiarRolUsuario(id, rol) {
    try {
        const response = await fetch(`${API_USUARIOS}/${id}/rol`, {
            method: "PUT",
            headers: headersJsonSeguros(),
            body: JSON.stringify({ rol })
        });
        await leerRespuesta(response);
        cargarUsuarios();
    } catch (e) {
        alert(e.message);
        cargarUsuarios();
    }
}

async function restablecerPassword(id) {
    const password = prompt("Ingrese la nueva contraseña (entre 12 y 72 caracteres)");
    if (password === null) return;

    try {
        const response = await fetch(`${API_USUARIOS}/${id}/password`, {
            method: "PUT",
            headers: headersJsonSeguros(),
            body: JSON.stringify({ password })
        });
        await leerRespuesta(response);
        alert("Contraseña actualizada correctamente");
    } catch (e) {
        alert(e.message);
    }
}

// =====================================
// UTILIDADES
// =====================================

function valorSeguro(valor) {
    return valor ?? "";
}

function numeroSeguro(valor) {
    const numero = Number(valor);
    return Number.isFinite(numero) ? numero : 0;
}

async function leerRespuesta(response) {
    const texto = await response.text();

    let datos = {};

    if (texto) {
        try {
            datos = JSON.parse(texto);
        } catch {
            datos = { mensaje: texto };
        }
    }

    if (!response.ok) {
        throw new Error(
            datos.error ||
            datos.mensaje ||
            "Ocurrió un error en la solicitud"
        );
    }

    return datos;
}

// =====================================
// BUSCAR CLIENTE POR DPI
// =====================================

function buscarClientePorDpi() {
    const dpi = document.getElementById("buscarDpi").value.trim();

    if (dpi === "") {
        alert("Ingrese un DPI");
        return;
    }

    fetch(`${API_CLIENTES}/dpi/${encodeURIComponent(dpi)}`)
        .then(leerRespuesta)
        .then(cliente => {
            cargarCliente(cliente);
            obtenerVehiculos(cliente.id);
        })
        .catch(error => {
            alert(error.message);
        });
}

// =====================================
// CARGAR DATOS DEL CLIENTE
// =====================================

function cargarCliente(cliente) {
    document.getElementById("clienteId").value =
        valorSeguro(cliente.id);

    document.getElementById("nombre").value =
        valorSeguro(cliente.nombre);

    document.getElementById("dpi").value =
        valorSeguro(cliente.dpi);

    document.getElementById("nit").value =
        valorSeguro(cliente.nit);

    document.getElementById("email").value =
        valorSeguro(cliente.email);

    document.getElementById("telefono").value =
        valorSeguro(cliente.telefono);

    document.getElementById("cantidadDispositivos").value =
        numeroSeguro(cliente.cantidadDispositivos);
}

// =====================================
// GUARDAR O ACTUALIZAR CLIENTE
// =====================================

function guardarCliente(e) {
    e.preventDefault();

    const clienteId =
        document.getElementById("clienteId").value;

    const cantidadDispositivos = parseInt(
        document.getElementById("cantidadDispositivos").value,
        10
    );

    const cliente = {
        nombre: document.getElementById("nombre").value.trim(),
        dpi: document.getElementById("dpi").value.trim(),
        nit: document.getElementById("nit").value.trim(),
        email: document.getElementById("email").value.trim(),
        telefono: document.getElementById("telefono").value.trim(),
        cantidadDispositivos:
            Number.isNaN(cantidadDispositivos)
                ? 0
                : cantidadDispositivos
    };

    if (cliente.nombre === "") {
        alert("Ingrese el nombre del cliente");
        return;
    }

    if (cliente.dpi === "") {
        alert("Ingrese el DPI del cliente");
        return;
    }

    if (cliente.cantidadDispositivos < 0) {
        alert("La cantidad de dispositivos no puede ser negativa");
        return;
    }

    const creando = clienteId === "";

    const url = creando
        ? API_CLIENTES
        : `${API_CLIENTES}/${clienteId}`;

    const metodo = creando ? "POST" : "PUT";

    fetch(url, {
        method: metodo,
        headers: headersJsonSeguros(),
        body: JSON.stringify(cliente)
    })
        .then(leerRespuesta)
        .then(resultado => {
            if (creando) {
                cargarCliente(resultado);
                alert("Cliente guardado correctamente");
            } else {
                alert("Cliente actualizado correctamente");
            }
        })
        .catch(error => {
            alert(error.message);
        });
}

// =====================================
// AGREGAR FORMULARIO DE VEHÍCULO
// =====================================

function agregarVehiculoFormulario() {
    const contenedor =
        document.getElementById("contenedorVehiculos");

    const div = document.createElement("div");
    div.classList.add("vehiculo-card");

    div.innerHTML = `
        <h3 class="tituloVehiculo">+ Nuevo Vehículo</h3>

        <div class="gridVehiculo">

            <div class="panel panel-vehiculo">
                <h4>🚗 Datos del Vehículo</h4>

                <label>Vehículo</label>
                <input
                    type="text"
                    class="vehiculo"
                    placeholder="Vehículo"
                >

                <label>Placa</label>
                <input
                    type="text"
                    class="placa"
                    placeholder="Placa"
                >

                <label>Fecha de instalación</label>
                <input
                    type="date"
                    class="fechaInstalacion"
                >
            </div>

            <div class="panel panel-gps">
                <h4>📡 Datos del GPS</h4>

                <label>Tipo GPS</label>
                <input
                    type="text"
                    class="tipoGps"
                    placeholder="Tipo GPS"
                >

                <label>IMEI</label>
                <input
                    type="text"
                    class="imei"
                    placeholder="IMEI"
                >

                <label>Telefonía</label>
                <input
                    type="text"
                    class="telefonia"
                    placeholder="Telefonía"
                >

                <label>Número SIM</label>
                <input
                    type="text"
                    class="numeroSim"
                    placeholder="Número SIM"
                >

                <label>Número de teléfono del GPS</label>
                <input
                    type="text"
                    class="numeroTelefono"
                    placeholder="Número de teléfono del GPS"
                >

                <label>Monto original</label>
                <input
                    type="number"
                    step="0.01"
                    min="0"
                    class="montoOriginal"
                    placeholder="Monto original"
                >
            </div>

            <div class="panel panel-promo">
                <h4>🏷 Promoción</h4>

                <label>Descripción de la promoción</label>
                <input
                    type="text"
                    class="descripcionPromocion"
                    placeholder="Descripción de la promoción"
                >

                <label>Monto con promoción</label>
                <input
                    type="number"
                    step="0.01"
                    min="0"
                    class="montoPromocion"
                    placeholder="Monto con promoción"
                >

                <label>Fecha de finalización</label>
                <input
                    type="date"
                    class="fechaFinPromocion"
                >

                <label>Tipo de plan</label>
                <select class="tipoPlan">
                    <option value="MENSUAL">Mensual</option>
                    <option value="ANUAL">Anual</option>
                </select>

                <label>Fecha de finalización del plan anual</label>
                <input type="date" class="fechaFinPlanAnual">
            </div>

        </div>

        <div class="accionesVehiculo">
            <button
                type="button"
                class="btnEliminar"
                onclick="quitarTarjetaVehiculo(this)"
            >
                🗑 Quitar Vehículo
            </button>
        </div>
    `;

    contenedor.appendChild(div);
}

function quitarTarjetaVehiculo(boton) {
    const card = boton.closest(".vehiculo-card");

    if (card) {
        card.remove();
    }
}

// =====================================
// OBTENER DATOS DE UNA TARJETA
// =====================================

function obtenerVehiculoDesdeTarjeta(card) {
    const montoOriginal = parseFloat(
        card.querySelector(".montoOriginal").value
    );

    const montoPromocion = parseFloat(
        card.querySelector(".montoPromocion").value
    );

    return {
        vehiculo:
            card.querySelector(".vehiculo").value.trim(),

        placa:
            card.querySelector(".placa").value.trim(),

        fechaInstalacion:
            card.querySelector(".fechaInstalacion").value,

        tipoGps:
            card.querySelector(".tipoGps").value.trim(),

        imei:
            card.querySelector(".imei").value.trim(),

        telefonia:
            card.querySelector(".telefonia").value.trim(),

        numeroSim:
            card.querySelector(".numeroSim").value.trim(),

        numeroTelefono:
            card.querySelector(".numeroTelefono").value.trim(),

        montoOriginal:
            Number.isNaN(montoOriginal) ? 0 : montoOriginal,

        descripcionPromocion:
            card.querySelector(".descripcionPromocion").value.trim(),

        montoPromocion:
            Number.isNaN(montoPromocion) ? 0 : montoPromocion,

        fechaFinPromocion:
            card.querySelector(".fechaFinPromocion").value,

        tipoPlan: card.querySelector(".tipoPlan").value,

        fechaFinPlanAnual:
            card.querySelector(".fechaFinPlanAnual").value
    };
}

function validarVehiculo(vehiculo) {
    if (vehiculo.vehiculo === "") {
        return "Ingrese el nombre o modelo del vehículo";
    }

    if (vehiculo.placa === "") {
        return "Ingrese la placa del vehículo";
    }

    if (vehiculo.fechaInstalacion === "") {
        return "Seleccione la fecha de instalación";
    }

    if (vehiculo.montoOriginal < 0) {
        return "El monto original no puede ser negativo";
    }

    if (vehiculo.montoPromocion < 0) {
        return "El monto de promoción no puede ser negativo";
    }

    if (vehiculo.tipoPlan === "ANUAL" && vehiculo.fechaFinPlanAnual === "") {
        return "Seleccione la fecha de finalización del plan anual";
    }

    return null;
}

// =====================================
// GUARDAR VARIOS VEHÍCULOS
// =====================================

function guardarVariosVehiculos() {
    const clienteId =
        document.getElementById("clienteId").value;

    if (clienteId === "") {
        alert("Primero debe buscar o guardar un cliente");
        return;
    }

    const cards =
        document.querySelectorAll(".vehiculo-card");

    const vehiculos = [];

    for (const card of cards) {
        if (card.getAttribute("data-id")) {
            continue;
        }

        const vehiculo =
            obtenerVehiculoDesdeTarjeta(card);

        const error = validarVehiculo(vehiculo);

        if (error) {
            alert(error);
            return;
        }

        vehiculos.push(vehiculo);
    }

    if (vehiculos.length === 0) {
        alert("No hay vehículos nuevos para guardar");
        return;
    }

    fetch(
        `${API_VEHICULOS}/cliente/${clienteId}/varios`,
        {
            method: "POST",
            headers: headersJsonSeguros(),
            body: JSON.stringify(vehiculos)
        }
    )
        .then(leerRespuesta)
        .then(() => {
            alert("Vehículos guardados correctamente");

            document.getElementById(
                "contenedorVehiculos"
            ).innerHTML = "";

            agregarVehiculoFormulario();
            obtenerVehiculos(clienteId);
        })
        .catch(error => {
            alert(error.message);
        });
}

// =====================================
// OBTENER VEHÍCULOS DEL CLIENTE
// =====================================

function obtenerVehiculos(clienteId) {
    fetch(`${API_VEHICULOS}/cliente/${clienteId}`)
        .then(leerRespuesta)
        .then(data => {
            const tabla =
                document.getElementById("tablaDatos");

            let total = 0;

            tabla.innerHTML = "";

            if (!Array.isArray(data) || data.length === 0) {
                tabla.innerHTML = `
                    <tr>
                        <td colspan="18">
                            Este cliente no tiene vehículos registrados
                        </td>
                    </tr>
                `;

                document.getElementById(
                    "totalMontoCliente"
                ).textContent = "Total mensual: Q 0.00";

                return;
            }

            data.forEach(vehiculo => {
                tabla.innerHTML +=
                    crearFilaVehiculo(vehiculo);

                if (vehiculo.promocion === "Activa") {
                    total += numeroSeguro(
                        vehiculo.montoPromocion
                    );
                } else {
                    total += numeroSeguro(
                        vehiculo.montoOriginal
                    );
                }
            });

            document.getElementById(
                "totalMontoCliente"
            ).textContent =
                `Total mensual: Q ${total.toFixed(2)}`;
        })
        .catch(error => {
            console.error(error);
            alert(error.message);
        });
}

// =====================================
// CREAR FILA DE VEHÍCULO
// =====================================

function crearFilaVehiculo(v) {
    const accionesAdministrador = usuarioActualRol === "ADMIN"
        ? `
                <button
                    type="button"
                    onclick="editarVehiculo(${v.id})"
                >
                    Editar
                </button>

                <button
                    type="button"
                    onclick="eliminarVehiculo(${v.id})"
                >
                    Eliminar
                </button>`
        : `<span class="solo-lectura">Solo lectura</span>`;

    return `
        <tr>
            <td>${valorSeguro(v.id)}</td>
            <td>${valorSeguro(v.vehiculo)}</td>
            <td>${valorSeguro(v.placa)}</td>
            <td>${valorSeguro(v.fechaInstalacion)}</td>

            <td>${valorSeguro(v.tipoGps)}</td>
            <td>${valorSeguro(v.imei)}</td>
            <td>${valorSeguro(v.telefonia)}</td>
            <td>${valorSeguro(v.numeroSim)}</td>
            <td>${valorSeguro(v.numeroTelefono)}</td>

            <td>Q ${numeroSeguro(v.montoOriginal).toFixed(2)}</td>

            <td>${valorSeguro(v.promocion)}</td>
            <td>${valorSeguro(v.descripcionPromocion)}</td>
            <td>Q ${numeroSeguro(v.montoPromocion).toFixed(2)}</td>
            <td>${valorSeguro(v.fechaFinPromocion)}</td>

            <td>${valorSeguro(v.tipoPlan)}</td>
            <td>${valorSeguro(v.fechaFinPlanAnual)}</td>
            <td>${valorSeguro(v.estadoPlanAnual)}</td>

            <td>
                ${accionesAdministrador}
            </td>
        </tr>
    `;
}

// =====================================
// EDITAR VEHÍCULO
// =====================================

function editarVehiculo(id) {
    fetch(`${API_VEHICULOS}/${id}`)
        .then(leerRespuesta)
        .then(v => {
            const contenedor =
                document.getElementById(
                    "contenedorVehiculos"
                );

            contenedor.innerHTML = "";

            agregarVehiculoFormulario();

            const card =
                contenedor.querySelector(".vehiculo-card");

            card.setAttribute("data-id", v.id);

            card.querySelector(
                ".tituloVehiculo"
            ).textContent = "Editar Vehículo";

            card.querySelector(".vehiculo").value =
                valorSeguro(v.vehiculo);

            card.querySelector(".placa").value =
                valorSeguro(v.placa);

            card.querySelector(".fechaInstalacion").value =
                valorSeguro(v.fechaInstalacion);

            card.querySelector(".tipoGps").value =
                valorSeguro(v.tipoGps);

            card.querySelector(".imei").value =
                valorSeguro(v.imei);

            card.querySelector(".telefonia").value =
                valorSeguro(v.telefonia);

            card.querySelector(".numeroSim").value =
                valorSeguro(v.numeroSim);

            card.querySelector(".numeroTelefono").value =
                valorSeguro(v.numeroTelefono);

            card.querySelector(".montoOriginal").value =
                numeroSeguro(v.montoOriginal);

            card.querySelector(
                ".descripcionPromocion"
            ).value =
                valorSeguro(v.descripcionPromocion);

            card.querySelector(".montoPromocion").value =
                numeroSeguro(v.montoPromocion);

            card.querySelector(
                ".fechaFinPromocion"
            ).value =
                valorSeguro(v.fechaFinPromocion);

            card.querySelector(".tipoPlan").value =
                valorSeguro(v.tipoPlan) || "MENSUAL";

            card.querySelector(".fechaFinPlanAnual").value =
                valorSeguro(v.fechaFinPlanAnual);

            const botonGuardar =
                document.createElement("button");

            botonGuardar.type = "button";
            botonGuardar.classList.add("btnGuardar");
            botonGuardar.textContent = "Guardar Cambios";

            botonGuardar.onclick = function () {
                guardarVehiculoEditado(this);
            };

            const acciones =
                card.querySelector(".accionesVehiculo");

            acciones.insertBefore(
                botonGuardar,
                acciones.firstChild
            );

            card.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
        })
        .catch(error => {
            alert(error.message);
        });
}

// =====================================
// GUARDAR VEHÍCULO EDITADO
// =====================================

function guardarVehiculoEditado(boton) {
    const card =
        boton.closest(".vehiculo-card");

    const id =
        card.getAttribute("data-id");

    if (!id) {
        alert("Primero seleccione un vehículo con Editar");
        return;
    }

    const vehiculo =
        obtenerVehiculoDesdeTarjeta(card);

    const error =
        validarVehiculo(vehiculo);

    if (error) {
        alert(error);
        return;
    }

    fetch(`${API_VEHICULOS}/${id}`, {
        method: "PUT",
        headers: headersJsonSeguros(),
        body: JSON.stringify(vehiculo)
    })
        .then(leerRespuesta)
        .then(() => {
            alert("Vehículo actualizado correctamente");

            const clienteId =
                document.getElementById("clienteId").value;

            obtenerVehiculos(clienteId);

            document.getElementById(
                "contenedorVehiculos"
            ).innerHTML = "";

            agregarVehiculoFormulario();
        })
        .catch(error => {
            alert(error.message);
        });
}

// =====================================
// ELIMINAR VEHÍCULO
// =====================================

function eliminarVehiculo(id) {
    const clienteId =
        document.getElementById("clienteId").value;

    if (!confirm("¿Desea eliminar este vehículo?")) {
        return;
    }

    fetch(`${API_VEHICULOS}/${id}`, {
        method: "DELETE",
        headers: { "X-CSRF-Token": csrfToken }
    })
        .then(leerRespuesta)
        .then(() => {
            alert("Vehículo eliminado correctamente");
            obtenerVehiculos(clienteId);
        })
        .catch(error => {
            alert(error.message);
        });
}

// =====================================
// ELIMINAR CLIENTE
// =====================================

function eliminarCliente() {
    const id =
        document.getElementById("clienteId").value;

    if (id === "") {
        alert("Primero busque un cliente");
        return;
    }

    if (
        !confirm(
            "¿Desea eliminar este cliente y todos sus vehículos?"
        )
    ) {
        return;
    }

    fetch(`${API_CLIENTES}/${id}`, {
        method: "DELETE",
        headers: { "X-CSRF-Token": csrfToken }
    })
        .then(leerRespuesta)
        .then(() => {
            alert("Cliente eliminado correctamente");
            limpiarTodo();
        })
        .catch(error => {
            alert(error.message);
        });
}

// =====================================
// MOSTRAR TODOS LOS CLIENTES
// =====================================

function mostrarTodosClientes() {
    fetch(API_CLIENTES)
        .then(leerRespuesta)
        .then(clientes => {
            const tabla =
                document.getElementById("tablaClientes");

            const cuerpo =
                document.getElementById(
                    "tablaClientesBody"
                );

            tabla.style.display = "table";
            cuerpo.innerHTML = "";

            if (
                !Array.isArray(clientes) ||
                clientes.length === 0
            ) {
                cuerpo.innerHTML = `
                    <tr>
                        <td colspan="5">
                            No hay clientes registrados
                        </td>
                    </tr>
                `;

                return;
            }

            clientes.forEach(cliente => {
                cuerpo.innerHTML += `
                    <tr>
                        <td>${valorSeguro(cliente.id)}</td>
                        <td>${valorSeguro(cliente.nombre)}</td>
                        <td>${valorSeguro(cliente.dpi)}</td>
                        <td>${valorSeguro(cliente.telefono)}</td>
                        <td>
                            ${numeroSeguro(
                                cliente.cantidadDispositivos
                            )}
                        </td>
                    </tr>
                `;
            });
        })
        .catch(error => {
            alert(error.message);
        });
}

// =====================================
// OCULTAR TABLA DE CLIENTES
// =====================================

function ocultarClientes() {
    document.getElementById(
        "tablaClientes"
    ).style.display = "none";
}

// =====================================
// LIMPIAR TODO
// =====================================

function limpiarTodo() {
    document.getElementById("formCliente").reset();

    document.getElementById(
        "clienteId"
    ).value = "";

    document.getElementById(
        "buscarDpi"
    ).value = "";

    document.getElementById(
        "cantidadDispositivos"
    ).value = "0";

    document.getElementById(
        "tablaDatos"
    ).innerHTML = "";

    document.getElementById(
        "totalMontoCliente"
    ).textContent = "Total mensual: Q 0.00";

    document.getElementById(
        "contenedorVehiculos"
    ).innerHTML = "";

    agregarVehiculoFormulario();
}
