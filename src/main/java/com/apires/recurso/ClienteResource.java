package com.apires.recurso;

import com.apirest.db.ClienteDAO;
import com.apirest.modelo.Cliente;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

    private final ClienteDAO dao = new ClienteDAO();

    // OBTENER TODOS
    @GET
    public Response obtenerTodos() {
        List<Cliente> clientes = dao.obtenerTodos();
        return Response.ok(clientes).build();
    }

    // BUSCAR POR DPI
    @GET
    @Path("/dpi/{dpi}")
    public Response obtenerPorDpi(@PathParam("dpi") String dpi) {
        Cliente cliente = dao.obtenerPorDpi(dpi);

        if (cliente == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Cliente no encontrado\"}")
                    .build();
        }

        return Response.ok(cliente).build();
    }

    // BUSCAR POR ID
    @GET
    @Path("/{id}")
    public Response obtenerPorId(@PathParam("id") int id) {
        Cliente cliente = dao.obtenerPorId(id);

        if (cliente == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Cliente no encontrado\"}")
                    .build();
        }

        return Response.ok(cliente).build();
    }

    // CREAR CLIENTE
    @POST
    public Response crear(Cliente cliente) {
        if (cliente == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Datos del cliente requeridos\"}")
                    .build();
        }

        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Nombre requerido\"}")
                    .build();
        }

        if (cliente.getDpi() == null || cliente.getDpi().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"DPI requerido\"}")
                    .build();
        }

        if (cliente.getCantidadDispositivos() < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"La cantidad de dispositivos no puede ser negativa\"}")
                    .build();
        }

        Cliente creado = dao.crear(cliente);

        if (creado == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"No se pudo guardar el cliente\"}")
                    .build();
        }

        return Response.status(Response.Status.CREATED)
                .entity(creado)
                .build();
    }

    // ACTUALIZAR CLIENTE
    @PUT
    @Path("/{id}")
    public Response actualizar(@PathParam("id") int id, Cliente cliente) {
        if (cliente == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Datos del cliente requeridos\"}")
                    .build();
        }

        boolean actualizado = dao.actualizar(id, cliente);

        if (!actualizado) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Cliente no encontrado o no actualizado\"}")
                    .build();
        }

        return Response.ok("{\"mensaje\":\"Cliente actualizado correctamente\"}")
                .build();
    }

    // ELIMINAR CLIENTE
    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        boolean eliminado = dao.eliminar(id);

        if (!eliminado) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Cliente no encontrado\"}")
                    .build();
        }

        return Response.ok("{\"mensaje\":\"Cliente eliminado correctamente\"}")
                .build();
    }
}