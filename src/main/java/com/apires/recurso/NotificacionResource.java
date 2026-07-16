package com.apires.recurso;

import com.apirest.db.NotificacionDAO;
import com.apirest.seguridad.SesionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/notificaciones")
@Produces(MediaType.APPLICATION_JSON)
public class NotificacionResource {
    private final NotificacionDAO dao = new NotificacionDAO();

    @GET
    public Response obtener(@Context HttpServletRequest request) {
        if (!esAdmin(request)) return prohibido();
        dao.generarNotificacionesDeVencimiento();
        return Response.ok(dao.obtenerRecientes()).build();
    }

    @PUT
    @Path("/{id}/leida")
    public Response marcarLeida(@PathParam("id") long id,
                                @Context HttpServletRequest request) {
        if (!esAdmin(request)) return prohibido();
        if (!dao.marcarLeida(id)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Notificación no encontrada\"}").build();
        }
        return Response.ok("{\"mensaje\":\"Notificación marcada como leída\"}").build();
    }

    private boolean esAdmin(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return sesion != null && "ADMIN".equals(
                sesion.getAttribute(SesionService.USUARIO_ROL));
    }

    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\":\"Solo un administrador puede consultar notificaciones\"}")
                .build();
    }
}
