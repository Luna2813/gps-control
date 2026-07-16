package com.apires.recurso;

import com.apirest.db.UsuarioDAO;
import com.apirest.modelo.CambiarEstadoRequest;
import com.apirest.modelo.CambiarPasswordRequest;
import com.apirest.modelo.CambiarRolRequest;
import com.apirest.modelo.CrearUsuarioRequest;
import com.apirest.modelo.Usuario;
import com.apirest.seguridad.PasswordService;
import com.apirest.seguridad.SesionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    private static final Pattern USUARIO_VALIDO =
            Pattern.compile("^[a-zA-Z0-9._-]{3,40}$");

    private final UsuarioDAO dao = new UsuarioDAO();

    @GET
    public Response listar(@Context HttpServletRequest request) {
        Response acceso = exigirAdmin(request);
        if (acceso != null) return acceso;

        List<Map<String, Object>> usuarios = dao.obtenerTodos().stream()
                .map(this::respuestaUsuario)
                .toList();
        return Response.ok(usuarios).build();
    }

    @POST
    public Response crear(CrearUsuarioRequest datos,
                          @Context HttpServletRequest request) {
        Response acceso = exigirAdmin(request);
        if (acceso != null) return acceso;

        String error = validarCreacion(datos);
        if (error != null) {
            return error(Response.Status.BAD_REQUEST, error);
        }

        String nombreUsuario = datos.getUsuario().trim().toLowerCase();
        if (dao.existeNombreUsuario(nombreUsuario)) {
            return error(Response.Status.CONFLICT,
                    "El nombre de usuario ya está registrado");
        }

        String rol = normalizarRol(datos.getRol());
        Usuario creado = dao.crear(
                datos.getNombre().trim(),
                nombreUsuario,
                PasswordService.crearHash(datos.getPassword()),
                rol
        );

        return Response.status(Response.Status.CREATED)
                .entity(respuestaUsuario(creado))
                .build();
    }

    @PUT
    @Path("/{id}/estado")
    public Response cambiarEstado(@PathParam("id") long id,
                                  CambiarEstadoRequest datos,
                                  @Context HttpServletRequest request) {
        Response acceso = exigirAdmin(request);
        if (acceso != null) return acceso;

        if (datos == null) {
            return error(Response.Status.BAD_REQUEST, "Estado requerido");
        }

        if (id == usuarioActualId(request) && !datos.isActivo()) {
            return error(Response.Status.BAD_REQUEST,
                    "No puede desactivar su propia cuenta");
        }

        if (!dao.actualizarEstado(id, datos.isActivo())) {
            return error(Response.Status.NOT_FOUND, "Usuario no encontrado");
        }

        return Response.ok(Map.of("mensaje", "Estado actualizado"))
                .build();
    }

    @PUT
    @Path("/{id}/rol")
    public Response cambiarRol(@PathParam("id") long id,
                               CambiarRolRequest datos,
                               @Context HttpServletRequest request) {
        Response acceso = exigirAdmin(request);
        if (acceso != null) return acceso;

        String rol = datos == null ? null : normalizarRol(datos.getRol());
        if (rol == null) {
            return error(Response.Status.BAD_REQUEST, "Rol inválido");
        }

        if (id == usuarioActualId(request) && !"ADMIN".equals(rol)) {
            return error(Response.Status.BAD_REQUEST,
                    "No puede retirar su propio rol de administrador");
        }

        if (!dao.actualizarRol(id, rol)) {
            return error(Response.Status.NOT_FOUND, "Usuario no encontrado");
        }

        return Response.ok(Map.of("mensaje", "Rol actualizado"))
                .build();
    }

    @PUT
    @Path("/{id}/password")
    public Response cambiarPassword(@PathParam("id") long id,
                                    CambiarPasswordRequest datos,
                                    @Context HttpServletRequest request) {
        Response acceso = exigirAdmin(request);
        if (acceso != null) return acceso;

        try {
            PasswordService.validarPassword(
                    datos == null ? null : datos.getPassword());
        } catch (IllegalArgumentException e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        }

        if (!dao.actualizarPassword(
                id, PasswordService.crearHash(datos.getPassword()))) {
            return error(Response.Status.NOT_FOUND, "Usuario no encontrado");
        }

        return Response.ok(Map.of("mensaje", "Contraseña actualizada"))
                .build();
    }

    private String validarCreacion(CrearUsuarioRequest datos) {
        if (datos == null || datos.getNombre() == null
                || datos.getNombre().isBlank()) {
            return "Nombre requerido";
        }
        if (datos.getNombre().trim().length() > 120) {
            return "El nombre no puede superar 120 caracteres";
        }
        if (datos.getUsuario() == null
                || !USUARIO_VALIDO.matcher(datos.getUsuario().trim()).matches()) {
            return "El usuario debe tener entre 3 y 40 caracteres válidos";
        }
        try {
            PasswordService.validarPassword(datos.getPassword());
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        if (normalizarRol(datos.getRol()) == null) {
            return "Rol inválido";
        }
        return null;
    }

    private String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) return "OPERADOR";
        String normalizado = rol.trim().toUpperCase();
        return "ADMIN".equals(normalizado) || "OPERADOR".equals(normalizado)
                ? normalizado : null;
    }

    private Response exigirAdmin(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null
                || !"ADMIN".equals(sesion.getAttribute(SesionService.USUARIO_ROL))) {
            return error(Response.Status.FORBIDDEN,
                    "Se requiere el rol de administrador");
        }
        return null;
    }

    private long usuarioActualId(HttpServletRequest request) {
        Object id = request.getSession(false)
                .getAttribute(SesionService.USUARIO_ID);
        return ((Number) id).longValue();
    }

    private Map<String, Object> respuestaUsuario(Usuario usuario) {
        return Map.of(
                "id", usuario.getId(),
                "nombre", usuario.getNombre(),
                "usuario", usuario.getUsuario(),
                "rol", usuario.getRol(),
                "activo", usuario.isActivo()
        );
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado)
                .entity(Map.of("error", mensaje))
                .build();
    }
}
