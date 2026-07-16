package com.apirest.seguridad;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordService {

    private static final int COSTO_BCRYPT = 12;

    private PasswordService() {
    }

    public static String crearHash(String password) {
        validarPassword(password);
        return BCrypt.hashpw(password, BCrypt.gensalt(COSTO_BCRYPT));
    }

    public static boolean verificar(String password, String hash) {
        if (password == null || hash == null || hash.isBlank()) {
            return false;
        }

        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static void validarPassword(String password) {
        if (password == null || password.length() < 12) {
            throw new IllegalArgumentException(
                    "La contraseña debe contener al menos 12 caracteres"
            );
        }

        if (password.length() > 72) {
            throw new IllegalArgumentException(
                    "La contraseña no puede superar 72 caracteres"
            );
        }
    }
}
