package com.apires.recurso;

import com.apirest.db.SuscripcionPushDAO;
import com.apirest.modelo.SuscripcionPushRequest;
import com.apirest.seguridad.SesionService;
import com.apirest.seguridad.WebPushService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/push")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PushResource {
    private final SuscripcionPushDAO dao = new SuscripcionPushDAO();

    @GET
    @Path("/clave-publica")
    public Response clavePublica(@Context HttpServletRequest request) {
        if (!esAdmin(request)) return prohibido();
        String clave = limpiarClave(System.getenv("VAPID_PUBLIC_KEY"),
                "VAPID_PUBLIC_KEY=");
        if (clave == null || clave.isBlank()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\":\"Web Push no está configurado\"}").build();
        }
        if (clave.length() != 87 || !clave.matches("[A-Za-z0-9_-]+")) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\":\"VAPID_PUBLIC_KEY está incompleta o tiene formato inválido\"}")
                    .build();
        }
        return Response.ok("{\"clavePublica\":\"" + clave + "\"}").build();
    }

    @POST
    @Path("/suscripciones")
    public Response suscribir(SuscripcionPushRequest datos,
                              @Context HttpServletRequest request) {
        if (!esAdmin(request)) return prohibido();
        if (datos == null || vacio(datos.getEndpoint()) || vacio(datos.getP256dh())
                || vacio(datos.getAuth())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Suscripción push incompleta\"}").build();
        }
        HttpSession sesion = request.getSession(false);
        long usuarioId = ((Number) sesion.getAttribute(SesionService.USUARIO_ID)).longValue();
        if (!dao.guardar(usuarioId, datos.getEndpoint(), datos.getP256dh(),
                datos.getAuth(), request.getHeader("User-Agent"),
                datos.getEndpointAnterior())) {
            return Response.serverError().entity("{\"error\":\"No se guardó la suscripción\"}").build();
        }
        return Response.status(Response.Status.CREATED)
                .entity("{\"mensaje\":\"Notificaciones activadas en este dispositivo\"}").build();
    }

    @POST
    @Path("/prueba")
    public Response probar(@Context HttpServletRequest request) {
        if (!esAdmin(request)) return prohibido();
        int enviadas = new WebPushService().enviarPrueba();
        if (enviadas == 0) {
            return Response.status(Response.Status.BAD_GATEWAY)
                    .entity("{\"error\":\"No se pudo enviar la prueba a ningún dispositivo\"}")
                    .build();
        }
        return Response.ok("{\"mensaje\":\"Prueba enviada\",\"dispositivos\":"
                + enviadas + "}").build();
    }

    private boolean esAdmin(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return sesion != null && "ADMIN".equals(sesion.getAttribute(SesionService.USUARIO_ROL));
    }
    private boolean vacio(String valor) { return valor == null || valor.isBlank(); }
    private String limpiarClave(String valor, String prefijo) {
        if (valor == null) return null;
        String limpia = valor.trim();
        if (limpia.startsWith(prefijo)) limpia = limpia.substring(prefijo.length());
        return limpia.replaceAll("\\s", "");
    }
    private Response prohibido() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\":\"Solo administradores pueden activar notificaciones\"}").build();
    }
}
