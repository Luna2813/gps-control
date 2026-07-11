package com.apires.recurso;

import com.apirest.db.ClienteDAO;
import com.apirest.modelo.Cliente;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/clientes")
public class ClienteResource {

    private ClienteDAO dao = new ClienteDAO();

    // ======================================
    // BUSCAR CLIENTE POR DPI
    // ======================================
    @GET
    @Path("/dpi/{dpi}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPorDpi(@PathParam("dpi") String dpi) {

        Cliente cliente = dao.obtenerPorDpi(dpi);

        if (cliente == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Cliente no encontrado\"}")
                    .build();
        }

        return Response.ok(cliente).build();
    }

    // ======================================
    // BUSCAR CLIENTE POR ID
    // ======================================
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPorId(@PathParam("id") int id) {

        Cliente cliente = dao.obtenerPorId(id);

        if (cliente == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Cliente no encontrado\"}")
                    .build();
        }

        return Response.ok(cliente).build();
    }

    // ======================================
    // CREAR CLIENTE
    // ======================================
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(Cliente cliente) {

        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            return Response.status(400)
                    .entity("{\"error\":\"Nombre requerido\"}")
                    .build();
        }

        if (cliente.getDpi() == null || cliente.getDpi().isBlank()) {
            return Response.status(400)
                    .entity("{\"error\":\"DPI requerido\"}")
                    .build();
        }

        Cliente creado = dao.crear(cliente);

        return Response.status(Response.Status.CREATED)
                .entity(creado)
                .build();
    }

    // ======================================
    // ACTUALIZAR CLIENTE
    // ======================================
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public boolean actualizar(@PathParam("id") int id,
                              Cliente cliente) {

        return dao.actualizar(id, cliente);
    }
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(@PathParam("id") int id) {

        boolean ok = dao.eliminar(id);

        if (!ok) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Cliente no encontrado\"}")
                    .build();
        }

        return Response.ok("{\"mensaje\":\"Cliente eliminado correctamente\"}")
                .build();
    

    }
}