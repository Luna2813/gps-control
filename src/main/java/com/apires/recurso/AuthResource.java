package com.apires.recurso;

import com.apirest.db.UsuarioDAO;
import com.apirest.modelo.LoginRequest;
import com.apirest.modelo.Usuario;
import com.apirest.seguridad.PasswordService;
import com.apirest.seguridad.SesionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final int DURACION_SESION_SEGUNDOS = 30 * 60;
    private final UsuarioDAO dao = new UsuarioDAO();

    @POST
    @Path("/login")
    public Response login(LoginRequest datos, @Context HttpServletRequest request) {
        if (datos == null || datos.getUsuario() == null
                || datos.getPassword() == null
                || datos.getUsuario().isBlank()
                || datos.getPassword().isBlank()) {
            return error(Response.Status.BAD_REQUEST,
                    "Usuario y contraseña son requeridos");
        }

        String nombreUsuario = datos.getUsuario().trim();
        Usuario usuario = dao.obtenerParaLogin(nombreUsuario);

        if (usuario == null || !usuario.isActivo()
                || !PasswordService.verificar(
                        datos.getPassword(), usuario.getPasswordHash())) {
            dao.registrarLoginFallido(nombreUsuario);
            return error(Response.Status.UNAUTHORIZED,
                    "Usuario o contraseña incorrectos");
        }

        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }

        HttpSession sesion = request.getSession(true);
        sesion.setMaxInactiveInterval(DURACION_SESION_SEGUNDOS);
        sesion.setAttribute(SesionService.USUARIO_ID, usuario.getId());
        sesion.setAttribute(SesionService.USUARIO_NOMBRE, usuario.getNombre());
        sesion.setAttribute(SesionService.USUARIO_LOGIN, usuario.getUsuario());
        sesion.setAttribute(SesionService.USUARIO_ROL, usuario.getRol());
        sesion.setAttribute(SesionService.CSRF_TOKEN,
                SesionService.nuevoTokenCsrf());

        dao.registrarLoginCorrecto(usuario.getId());
        return Response.ok(datosSesion(sesion)).build();
    }

    @GET
    @Path("/session")
    public Response sesion(@Context HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);

        if (!SesionService.autenticada(sesion)) {
            return error(Response.Status.UNAUTHORIZED,
                    "No hay una sesión activa");
        }

        return Response.ok(datosSesion(sesion)).build();
    }

    @POST
    @Path("/logout")
    public Response logout(@Context HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }

        return Response.ok(Map.of("mensaje", "Sesión cerrada correctamente"))
                .build();
    }

    private Map<String, Object> datosSesion(HttpSession sesion) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("id", sesion.getAttribute(SesionService.USUARIO_ID));
        respuesta.put("nombre", sesion.getAttribute(SesionService.USUARIO_NOMBRE));
        respuesta.put("usuario", sesion.getAttribute(SesionService.USUARIO_LOGIN));
        respuesta.put("rol", sesion.getAttribute(SesionService.USUARIO_ROL));
        respuesta.put("csrfToken", sesion.getAttribute(SesionService.CSRF_TOKEN));
        return respuesta;
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado)
                .entity(Map.of("error", mensaje))
                .build();
    }
}
