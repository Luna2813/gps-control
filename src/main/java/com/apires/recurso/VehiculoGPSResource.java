package com.apires.recurso;

import com.apirest.db.VehiculoGPSDAO;
import com.apirest.modelo.VehiculoGPS;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.apirest.seguridad.SesionService;

import java.util.ArrayList;
import java.util.List;

@Path("/vehiculos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VehiculoGPSResource {

    private final VehiculoGPSDAO dao = new VehiculoGPSDAO();

    // OBTENER VEHÍCULOS DE UN CLIENTE
    @GET
    @Path("/cliente/{clienteId}")
    public Response obtenerPorCliente(
            @PathParam("clienteId") int clienteId) {

        List<VehiculoGPS> vehiculos =
                dao.obtenerPorCliente(clienteId);

        return Response.ok(vehiculos).build();
    }

    // OBTENER VEHÍCULO POR ID
    @GET
    @Path("/{id}")
    public Response obtenerPorId(@PathParam("id") int id) {
        VehiculoGPS vehiculo = dao.obtenerPorId(id);

        if (vehiculo == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Vehículo no encontrado\"}")
                    .build();
        }

        return Response.ok(vehiculo).build();
    }

    // CREAR UN VEHÍCULO
    @POST
    @Path("/cliente/{clienteId}")
    public Response crear(
            @PathParam("clienteId") int clienteId,
            VehiculoGPS vehiculo) {

        if (!vehiculoValido(vehiculo)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Vehículo, placa y fecha de instalación son requeridos\"}")
                    .build();
        }

        VehiculoGPS creado = dao.crear(clienteId, vehiculo);

        if (creado == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"No se pudo guardar el vehículo\"}")
                    .build();
        }

        return Response.status(Response.Status.CREATED)
                .entity(creado)
                .build();
    }

    // CREAR VARIOS VEHÍCULOS
    @POST
    @Path("/cliente/{clienteId}/varios")
    public Response crearVarios(
            @PathParam("clienteId") int clienteId,
            List<VehiculoGPS> vehiculos) {

        if (vehiculos == null || vehiculos.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Debe enviar al menos un vehículo\"}")
                    .build();
        }

        List<VehiculoGPS> creados = new ArrayList<>();

        for (VehiculoGPS vehiculo : vehiculos) {
            if (!vehiculoValido(vehiculo)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Todos los vehículos deben tener vehículo, placa y fecha de instalación\"}")
                        .build();
            }

            VehiculoGPS creado = dao.crear(clienteId, vehiculo);

            if (creado == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\":\"No se pudieron guardar todos los vehículos\"}")
                        .build();
            }

            creados.add(creado);
        }

        return Response.status(Response.Status.CREATED)
                .entity(creados)
                .build();
    }

    // ACTUALIZAR VEHÍCULO
    @PUT
    @Path("/{id}")
    public Response actualizar(
            @PathParam("id") int id,
            VehiculoGPS vehiculo,
            @Context HttpServletRequest request) {

        if (!esAdministrador(request)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Solo un administrador puede editar vehículos\"}")
                    .build();
        }

        if (!vehiculoValido(vehiculo)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Vehículo, placa y fecha de instalación son requeridos\"}")
                    .build();
        }

        boolean actualizado = dao.actualizar(id, vehiculo);

        if (!actualizado) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Vehículo no encontrado o no actualizado\"}")
                    .build();
        }

        return Response.ok("{\"mensaje\":\"Vehículo actualizado correctamente\"}")
                .build();
    }

    // ELIMINAR VEHÍCULO
    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id,
                             @Context HttpServletRequest request) {
        if (!esAdministrador(request)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Solo un administrador puede eliminar vehículos\"}")
                    .build();
        }

        boolean eliminado = dao.eliminar(id);

        if (!eliminado) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Vehículo no encontrado\"}")
                    .build();
        }

        return Response.ok("{\"mensaje\":\"Vehículo eliminado correctamente\"}")
                .build();
    }

    private boolean vehiculoValido(VehiculoGPS vehiculo) {
        return vehiculo != null
                && vehiculo.getVehiculo() != null
                && !vehiculo.getVehiculo().isBlank()
                && vehiculo.getPlaca() != null
                && !vehiculo.getPlaca().isBlank()
                && vehiculo.getFechaInstalacion() != null
                && !vehiculo.getFechaInstalacion().isBlank()
                && (!"ANUAL".equalsIgnoreCase(vehiculo.getTipoPlan())
                    || (vehiculo.getFechaFinPlanAnual() != null
                        && !vehiculo.getFechaFinPlanAnual().isBlank()));
    }

    private boolean esAdministrador(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return sesion != null
                && "ADMIN".equals(
                        sesion.getAttribute(SesionService.USUARIO_ROL));
    }
}
