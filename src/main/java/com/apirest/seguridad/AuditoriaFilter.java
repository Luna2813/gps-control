package com.apirest.seguridad;

import com.apirest.db.AuditoriaDAO;
import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER + 100)
public class AuditoriaFilter implements ContainerResponseFilter {

    private final AuditoriaDAO dao = new AuditoriaDAO();

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(ContainerRequestContext request,
                       ContainerResponseContext response) {
        String metodo = request.getMethod();
        String ruta = request.getUriInfo().getPath();

        if (!modificaDatos(metodo)
                || ruta.startsWith("auth/")
                || response.getStatus() < 200
                || response.getStatus() >= 300) {
            return;
        }

        HttpSession sesion = servletRequest.getSession(false);
        if (!SesionService.autenticada(sesion)) {
            return;
        }

        Object id = sesion.getAttribute(SesionService.USUARIO_ID);
        dao.registrar(
                ((Number) id).longValue(),
                String.valueOf(sesion.getAttribute(SesionService.USUARIO_LOGIN)),
                String.valueOf(sesion.getAttribute(SesionService.USUARIO_ROL)),
                metodo,
                ruta,
                response.getStatus(),
                servletRequest.getRemoteAddr(),
                servletRequest.getHeader("User-Agent")
        );
    }

    private boolean modificaDatos(String metodo) {
        return "POST".equals(metodo)
                || "PUT".equals(metodo)
                || "PATCH".equals(metodo)
                || "DELETE".equals(metodo);
    }
}
