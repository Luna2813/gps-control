package com.apires.recurso;

import com.apirest.db.VehiculoGPSDAO;
import com.apirest.modelo.VehiculoGPS;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/vehiculos")
public class VehiculoGPSResource {

    private VehiculoGPSDAO dao = new VehiculoGPSDAO();

    // ======================================
    // OBTENER VEHICULOS DE UN CLIENTE
    // ======================================
    @GET
    @Path("/cliente/{clienteId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPorCliente(
            @PathParam("clienteId") int clienteId) {

        List<VehiculoGPS> lista =
                dao.obtenerPorCliente(clienteId);

        return Response.ok(lista).build();
    }

    // ======================================
    // OBTENER VEHICULO POR ID
    // ======================================
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPorId(
            @PathParam("id") int id) {

        VehiculoGPS vehiculo =
                dao.obtenerPorId(id);

        if (vehiculo == null) {

            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Vehículo no encontrado\"}")
                    .build();
        }

        return Response.ok(vehiculo).build();
    }

    // ======================================
    // CREAR VEHICULO
    // ======================================
    @POST
    @Path("/cliente/{clienteId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(
            @PathParam("clienteId") int clienteId,
            VehiculoGPS vehiculo) {

        VehiculoGPS creado =
                dao.crear(clienteId, vehiculo);

        return Response.status(Response.Status.CREATED)
                .entity(creado)
                .build();
    }

    // ======================================
    // CREAR VARIOS VEHICULOS
    // ======================================
    @POST
    @Path("/cliente/{clienteId}/varios")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearVarios(
            @PathParam("clienteId") int clienteId,
            List<VehiculoGPS> vehiculos) {

        for (VehiculoGPS v : vehiculos) {
            dao.crear(clienteId, v);
        }

        return Response.status(Response.Status.CREATED)
                .entity("{\"mensaje\":\"Vehículos guardados correctamente\"}")
                .build();
    }

    // ======================================
    // ACTUALIZAR VEHICULO
    // ======================================
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public boolean actualizar(
            @PathParam("id") int id,
            VehiculoGPS vehiculo) {

        return dao.actualizar(id, vehiculo);
    }

    // ======================================
    // ELIMINAR VEHICULO
    // ======================================
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(
            @PathParam("id") int id) {

        boolean ok = dao.eliminar(id);

        if (!ok) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"No existe\"}")
                    .build();
        }

        return Response.ok("{\"mensaje\":\"Vehículo eliminado\"}")
                .build();
    }
}
