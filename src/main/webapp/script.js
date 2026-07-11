const API_CLIENTES = "api/clientes";
const API_VEHICULOS = "api/vehiculos";

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("formCliente")
        .addEventListener("submit", guardarCliente);

    agregarVehiculoFormulario();
});

function buscarClientePorDpi() {
    let dpi = document.getElementById("buscarDpi").value.trim();

    if (dpi === "") {
        alert("Ingrese un DPI");
        return;
    }

    fetch(`${API_CLIENTES}/dpi/${dpi}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("Cliente no encontrado");
            }
            return response.json();
        })
        .then(cliente => {
            cargarCliente(cliente);
            obtenerVehiculos(cliente.id);
        })
        .catch(error => {
            alert(error.message);
        });
}

function cargarCliente(cliente) {
    document.getElementById("clienteId").value = cliente.id;
    document.getElementById("nombre").value = cliente.nombre;
    document.getElementById("dpi").value = cliente.dpi;
    document.getElementById("nit").value = cliente.nit;
    document.getElementById("email").value = cliente.email;
    document.getElementById("telefono").value = cliente.telefono;
    document.getElementById("telefonia").value = cliente.telefonia;
    document.getElementById("numeroSim").value = cliente.numeroSim;
}

function guardarCliente(e) {
    e.preventDefault();

    let clienteId = document.getElementById("clienteId").value;

    const cliente = {
        nombre: document.getElementById("nombre").value,
        dpi: document.getElementById("dpi").value,
        nit: document.getElementById("nit").value,
        email: document.getElementById("email").value,
        telefono: document.getElementById("telefono").value,
        telefonia: document.getElementById("telefonia").value,
        numeroSim: document.getElementById("numeroSim").value
    };

    if (clienteId === "") {
        fetch(API_CLIENTES, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(cliente)
        })
        .then(response => response.json())
        .then(data => {
            cargarCliente(data);
            alert("Cliente guardado correctamente");
        });
    } else {
        fetch(`${API_CLIENTES}/${clienteId}`, {
            method: "PUT",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(cliente)
        })
        .then(() => {
            alert("Cliente actualizado correctamente");
        });
    }
}

function agregarVehiculoFormulario() {
    const contenedor = document.getElementById("contenedorVehiculos");

    const div = document.createElement("div");
    div.classList.add("vehiculo-card");

    div.innerHTML = `
        <h3 class="tituloVehiculo">+ Nuevo Vehículo</h3>

        <div class="gridVehiculo">

            <div class="panel panel-vehiculo">
                <h4>🚗 Datos del Vehículo</h4>

                <label>Cantidad de dispositivos</label>
                <input type="number" class="cantidadDispositivos" placeholder="Cantidad">

                <label>Vehículo</label>
                <input type="text" class="vehiculo" placeholder="Vehículo">

                <label>Placa</label>
                <input type="text" class="placa" placeholder="Placa">

                <label>Fecha de instalación</label>
                <input type="date" class="fechaInstalacion">
            </div>

            <div class="panel panel-gps">
                <h4>📡 Datos del GPS</h4>

                <label>Tipo GPS</label>
                <input type="text" class="tipoGps" placeholder="Tipo GPS">

                <label>IMEI</label>
                <input type="text" class="imei" placeholder="IMEI">

                <label>Monto</label>
                <input type="number" step="0.01" class="montoNormal" placeholder="Monto">
            </div>

            <div class="panel panel-promo">
                <h4>🏷 Promoción</h4>

                <label>Descripción</label>
                <input type="text" class="descripcionPromocion" placeholder="Descripción de la promoción">

                <label>Monto con promoción</label>
                <input type="number" step="0.01" class="montoPromocion" placeholder="Monto con promoción">

                <label>Fecha finalización</label>
                <input type="date" class="fechaFinPromocion">
            </div>

        </div>

        <div class="accionesVehiculo">
            <button type="button" class="btnEliminar"
                    onclick="this.parentElement.parentElement.remove()">
                🗑 Quitar Vehículo
            </button>
        </div>
    `;

    contenedor.appendChild(div);
}

function guardarVariosVehiculos() {
    const clienteId = document.getElementById("clienteId").value;

    if (clienteId === "") {
        alert("Primero debe buscar o guardar un cliente");
        return;
    }

    const cards = document.querySelectorAll(".vehiculo-card");
    let vehiculos = [];

    cards.forEach(card => {
        if (card.getAttribute("data-id")) {
            return;
        }

        vehiculos.push({
            cantidadDispositivos: parseInt(card.querySelector(".cantidadDispositivos").value),
            vehiculo: card.querySelector(".vehiculo").value,
            placa: card.querySelector(".placa").value,
            fechaInstalacion: card.querySelector(".fechaInstalacion").value,
            tipoGps: card.querySelector(".tipoGps").value,
            imei: card.querySelector(".imei").value,
            descripcionPromocion: card.querySelector(".descripcionPromocion").value,
            montoNormal: parseFloat(card.querySelector(".montoNormal").value),
            montoPromocion: parseFloat(card.querySelector(".montoPromocion").value),
            fechaFinPromocion: card.querySelector(".fechaFinPromocion").value
        });
    });

    if (vehiculos.length === 0) {
        alert("No hay vehículos nuevos para guardar");
        return;
    }

    fetch(`${API_VEHICULOS}/cliente/${clienteId}/varios`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(vehiculos)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Error al guardar vehículos");
        }
        return response.json();
    })
    .then(() => {
        alert("Vehículos guardados correctamente");
        document.getElementById("contenedorVehiculos").innerHTML = "";
        agregarVehiculoFormulario();
        obtenerVehiculos(clienteId);
    })
    .catch(error => {
        alert(error.message);
    });
}

function obtenerVehiculos(clienteId) {
    fetch(`${API_VEHICULOS}/cliente/${clienteId}`)
        .then(response => response.json())
        .then(data => {
            let tabla = document.getElementById("tablaDatos");
            let total = 0;

            tabla.innerHTML = "";

            if (data.length === 0) {
                tabla.innerHTML = `
                    <tr>
                        <td colspan="13">Este cliente no tiene vehículos registrados</td>
                    </tr>
                `;
                document.getElementById("totalMontoCliente").textContent =
                    "Total mensual: Q 0.00";
                return;
            }

            data.forEach(v => {
                tabla.innerHTML += crearFilaVehiculo(v);

                if (v.promocion === "Activa") {
                    total += Number(v.montoPromocion || 0);
                } else {
                    total += Number(v.montoNormal || 0);
                }
            });

            document.getElementById("totalMontoCliente").textContent =
                "Total mensual: Q " + total.toFixed(2);
        });

}

function crearFilaVehiculo(v) {
    return `
        <tr>
            <td>${v.id}</td>
            <td>${v.vehiculo}</td>
            <td>${v.placa}</td>
            <td>${v.cantidadDispositivos}</td>
            <td>${v.tipoGps}</td>
            <td>${v.imei}</td>
            <td>${v.fechaInstalacion}</td>
            <td>${v.promocion}</td>
            <td>${v.descripcionPromocion}</td>
            <td>${v.fechaFinPromocion}</td>
            <td>Q ${v.montoNormal}</td>
            <td>Q ${v.montoPromocion}</td>
            <td>
                <button onclick="editarVehiculo(${v.id})">Editar</button>
                <button onclick="eliminarVehiculo(${v.id})">Eliminar</button>
            </td>
        </tr>
    `;
}

function editarVehiculo(id) {
    fetch(`${API_VEHICULOS}/${id}`)
        .then(response => response.json())
        .then(v => {
            document.getElementById("contenedorVehiculos").innerHTML = "";

            agregarVehiculoFormulario();

            const card = document.querySelector(".vehiculo-card");

            card.setAttribute("data-id", v.id);
            card.querySelector(".tituloVehiculo").textContent = "Editar Vehículo";

            card.querySelector(".cantidadDispositivos").value = v.cantidadDispositivos;
            card.querySelector(".vehiculo").value = v.vehiculo;
            card.querySelector(".placa").value = v.placa;
            card.querySelector(".fechaInstalacion").value = v.fechaInstalacion;
            card.querySelector(".tipoGps").value = v.tipoGps;
            card.querySelector(".imei").value = v.imei;
            card.querySelector(".descripcionPromocion").value = v.descripcionPromocion;
            card.querySelector(".montoNormal").value = v.montoNormal;
            card.querySelector(".montoPromocion").value = v.montoPromocion;
            card.querySelector(".fechaFinPromocion").value = v.fechaFinPromocion;

            const botonGuardar = document.createElement("button");
            botonGuardar.type = "button";
            botonGuardar.classList.add("btnGuardar");
            botonGuardar.textContent = "Guardar Cambios";
            botonGuardar.onclick = function () {
                guardarVehiculoEditado(this);
            };

            const acciones = card.querySelector(".accionesVehiculo");
            acciones.insertBefore(botonGuardar, acciones.firstChild);
        });
}

function guardarVehiculoEditado(boton) {
    const card = boton.closest(".vehiculo-card");
    const id = card.getAttribute("data-id");

    if (!id) {
        alert("Primero seleccione un vehículo con Editar");
        return;
    }

    const vehiculo = {
        cantidadDispositivos: parseInt(card.querySelector(".cantidadDispositivos").value),
        vehiculo: card.querySelector(".vehiculo").value,
        placa: card.querySelector(".placa").value,
        fechaInstalacion: card.querySelector(".fechaInstalacion").value,
        tipoGps: card.querySelector(".tipoGps").value,
        imei: card.querySelector(".imei").value,
        descripcionPromocion: card.querySelector(".descripcionPromocion").value,
        montoNormal: parseFloat(card.querySelector(".montoNormal").value),
        montoPromocion: parseFloat(card.querySelector(".montoPromocion").value),
        fechaFinPromocion: card.querySelector(".fechaFinPromocion").value
    };

    fetch(`${API_VEHICULOS}/${id}`, {
        method: "PUT",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(vehiculo)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Error al actualizar vehículo");
        }

        alert("Vehículo actualizado correctamente");

        const clienteId = document.getElementById("clienteId").value;

        obtenerVehiculos(clienteId);

        document.getElementById("contenedorVehiculos").innerHTML = "";
        agregarVehiculoFormulario();
    })
    .catch(error => {
        alert(error.message);
    });
}

function eliminarVehiculo(id) {
    const clienteId = document.getElementById("clienteId").value;

    if (!confirm("¿Desea eliminar este vehículo?")) {
        return;
    }

    fetch(`${API_VEHICULOS}/${id}`, {
        method: "DELETE"
    })
    .then(() => {
        alert("Vehículo eliminado correctamente");
        obtenerVehiculos(clienteId);
    });
}

function limpiarTodo() {
    document.getElementById("formCliente").reset();
    document.getElementById("clienteId").value = "";
    document.getElementById("buscarDpi").value = "";
    document.getElementById("tablaDatos").innerHTML = "";

    document.getElementById("contenedorVehiculos").innerHTML = "";
    agregarVehiculoFormulario();
}
function eliminarCliente(){

    const id = document.getElementById("clienteId").value;

    if(id === ""){
        alert("Primero busque un cliente.");
        return;
    }

    if(!confirm("¿Desea eliminar este cliente y todos sus vehículos?")){
        return;
    }

    fetch(`${API_CLIENTES}/${id}`,{
        method:"DELETE"
    })
    .then(response=>{

        if(!response.ok){
            throw new Error("No se pudo eliminar el cliente.");
        }

        return response.text();

    })
    .then(()=>{

        alert("Cliente eliminado correctamente.");

        limpiarTodo();

    })
    .catch(error=>{

        alert(error.message);

    });

}