package com.apirest.seguridad;

import com.apirest.db.UsuarioDAO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class SeguridadBootstrapListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        configurarCookieSesion(evento);
        crearAdministradorInicialSiCorresponde();
    }

    private void configurarCookieSesion(ServletContextEvent evento) {
        SessionCookieConfig cookie = evento.getServletContext()
                .getSessionCookieConfig();
        cookie.setHttpOnly(true);
        cookie.setSecure(Boolean.parseBoolean(
                System.getenv().getOrDefault("SESSION_COOKIE_SECURE", "true")));
        cookie.setName("GPSCONTROLSESSION");
    }

    private void crearAdministradorInicialSiCorresponde() {
        String usuario = System.getenv("ADMIN_INITIAL_USER");
        String password = System.getenv("ADMIN_INITIAL_PASSWORD");
        String nombre = System.getenv().getOrDefault(
                "ADMIN_INITIAL_NAME", "Administrador");

        if (usuario == null || usuario.isBlank()
                || password == null || password.isBlank()) {
            return;
        }

        UsuarioDAO dao = new UsuarioDAO();
        if (dao.existenUsuarios()) {
            return;
        }

        dao.crear(
                nombre.trim(),
                usuario.trim(),
                PasswordService.crearHash(password),
                "ADMIN"
        );

        System.out.println("Usuario administrador inicial creado");
    }
}
