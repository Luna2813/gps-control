const API_CLIENTES = "api/clientes";
const API_VEHICULOS = "api/vehiculos";

document.addEventListener("DOMContentLoaded", () => {
    document
        .getElementById("formCliente")
        .addEventListener("submit", guardarCliente);

    agregarVehiculoFormulario();
});

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
        headers: {
            "Content-Type": "application/json"
        },
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
            card.querySelector(".fechaFinPromocion").value
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
            headers: {
                "Content-Type": "application/json"
            },
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
                        <td colspan="15">
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

            <td>
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
                </button>
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
        headers: {
            "Content-Type": "application/json"
        },
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
        method: "DELETE"
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
        method: "DELETE"
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