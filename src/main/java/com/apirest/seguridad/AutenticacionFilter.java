package com.apirest.seguridad;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AutenticacionFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(ContainerRequestContext request) {
        String ruta = request.getUriInfo().getPath();
        String metodo = request.getMethod();

        if (esRutaPublica(ruta, metodo)) {
            return;
        }

        HttpSession sesion = servletRequest.getSession(false);
        if (!SesionService.autenticada(sesion)) {
            abortar(request, Response.Status.UNAUTHORIZED,
                    "Debe iniciar sesión");
            return;
        }

        if (modificaDatos(metodo) && !csrfValido(request, sesion)) {
            abortar(request, Response.Status.FORBIDDEN,
                    "Token de seguridad inválido");
        }
    }

    private boolean esRutaPublica(String ruta, String metodo) {
        return ("auth/login".equals(ruta) && "POST".equals(metodo))
                || ("auth/session".equals(ruta) && "GET".equals(metodo));
    }

    private boolean modificaDatos(String metodo) {
        return "POST".equals(metodo)
                || "PUT".equals(metodo)
                || "PATCH".equals(metodo)
                || "DELETE".equals(metodo);
    }

    private boolean csrfValido(ContainerRequestContext request,
                                HttpSession sesion) {
        String recibido = request.getHeaderString("X-CSRF-Token");
        Object esperado = sesion.getAttribute(SesionService.CSRF_TOKEN);

        if (recibido == null || esperado == null) {
            return false;
        }

        return MessageDigest.isEqual(
                recibido.getBytes(StandardCharsets.UTF_8),
                esperado.toString().getBytes(StandardCharsets.UTF_8)
        );
    }

    private void abortar(ContainerRequestContext request,
                         Response.Status estado, String mensaje) {
        request.abortWith(Response.status(estado)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("error", mensaje))
                .build());
    }
}
