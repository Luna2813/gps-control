package com.apires.recurso;

import com.apirest.db.InstalacionGPSDAO;
import com.apirest.modelo.InstalacionGPS;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/instalaciones")
public class InstalacionGPSResource {

    private InstalacionGPSDAO dao = new InstalacionGPSDAO();

    // GET /api/instalaciones
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerTodos() {
        List<InstalacionGPS> lista = dao.obtenerTodos();
        return Response.ok(lista).build();
    }

    // GET /api/instalaciones/{id}
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPorNumero(@PathParam("id") int numero) {

        InstalacionGPS h = dao.obtenerPorNumero(numero);

        if (h == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Instalación no encontrada\"}")
                    .build();
        }

        return Response.ok(h).build();
    }

    // GET /api/instalaciones/dpi/{dpi}
    @GET
    @Path("/dpi/{dpi}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPorDpi(@PathParam("dpi") String dpi) {

        InstalacionGPS h = dao.obtenerPorDpi(dpi);

        if (h == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"DPI no encontrado\"}")
                    .build();
        }

        return Response.ok(h).build();
    }

    // POST /api/instalaciones
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(InstalacionGPS h) {

        if (h.getNombre() == null || h.getNombre().isBlank()) {
            return Response.status(400)
                    .entity("{\"error\":\"Nombre requerido\"}")
                    .build();
        }

        if (h.getPlaca() == null || h.getPlaca().isBlank()) {
            return Response.status(400)
                    .entity("{\"error\":\"Placa requerida\"}")
                    .build();
        }

        InstalacionGPS creada = dao.crear(h);

        return Response.status(Response.Status.CREATED)
                .entity(creada)
                .build();
    }

    // PUT /api/instalaciones/{id}
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public boolean actualizar(@PathParam("id") int id,
                              InstalacionGPS h) {

        return dao.actualizar(id, h);
    }

    // DELETE /api/instalaciones/{id}
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(@PathParam("id") int numero) {

        boolean ok = dao.eliminar(numero);

        if (!ok) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"No existe\"}")
                    .build();
        }

        return Response.ok("{\"mensaje\":\"Instalación eliminada\"}")
                .build();
    }
}