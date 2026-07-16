package com.apires.recurso;

import com.apirest.db.AuditoriaDAO;
import com.apirest.seguridad.SesionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/auditoria")
@Produces(MediaType.APPLICATION_JSON)
public class AuditoriaResource {

    private final AuditoriaDAO dao = new AuditoriaDAO();

    @GET
    public Response recientes(@QueryParam("limite") Integer limite,
                              @Context HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null
                || !"ADMIN".equals(
                        sesion.getAttribute(SesionService.USUARIO_ROL))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of(
                            "error", "Se requiere el rol de administrador"))
                    .build();
        }

        int cantidad = limite == null ? 100 : limite;
        return Response.ok(dao.obtenerRecientes(cantidad)).build();
    }
}
