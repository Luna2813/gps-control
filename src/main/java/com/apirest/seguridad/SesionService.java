package com.apirest.seguridad;

import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

public final class SesionService {

    public static final String USUARIO_ID = "usuarioId";
    public static final String USUARIO_NOMBRE = "usuarioNombre";
    public static final String USUARIO_LOGIN = "usuarioLogin";
    public static final String USUARIO_ROL = "usuarioRol";
    public static final String CSRF_TOKEN = "csrfToken";

    private static final SecureRandom RANDOM = new SecureRandom();

    private SesionService() {
    }

    public static String nuevoTokenCsrf() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static boolean autenticada(HttpSession sesion) {
        return sesion != null && sesion.getAttribute(USUARIO_ID) != null;
    }
}
