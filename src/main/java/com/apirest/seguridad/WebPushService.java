package com.apirest.seguridad;

import com.apirest.db.Conexion;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.jose4j.lang.JoseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class WebPushService {

    public int enviarPrueba() {
        registrarBouncyCastle();
        String publica = limpiarClave(System.getenv("VAPID_PUBLIC_KEY"),
                "VAPID_PUBLIC_KEY=");
        String privada = limpiarClave(System.getenv("VAPID_PRIVATE_KEY"),
                "VAPID_PRIVATE_KEY=");
        String asunto = System.getenv().getOrDefault(
                "VAPID_SUBJECT", "https://gps-control.onrender.com");
        if (vacio(publica) || vacio(privada)
                || publica.length() != 87 || privada.length() != 43) return 0;

        String sql = "SELECT id,endpoint,clave_p256dh,clave_auth "
                + "FROM suscripciones_push WHERE activa=TRUE";
        int enviadas = 0;
        try {
            PushService servicio = new PushServiceCompatible(publica, privada, asunto);
            try (Connection con = Conexion.obtener();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                String mensaje = payload("PRUEBA", "GPS Control",
                        "Las notificaciones están funcionando correctamente");
                while (rs.next()) {
                    try {
                        Notification notificacion = new Notification(
                                rs.getString("endpoint"),
                                rs.getString("clave_p256dh"),
                                rs.getString("clave_auth"),
                                mensaje.getBytes(StandardCharsets.UTF_8));
                        HttpResponse respuesta = servicio.send(
                                notificacion, Encoding.AES128GCM);
                        int estado = respuesta.getStatusLine().getStatusCode();
                        if (estado >= 200 && estado < 300) {
                            enviadas++;
                        } else {
                            if (suscripcionVencida(estado)) {
                                desactivarSuscripcion(con, rs.getLong("id"));
                            }
                            System.err.println("Servicio push rechazó la prueba. HTTP "
                                    + estado + " - " + detalleRespuesta(respuesta));
                        }
                    } catch (Exception e) {
                        System.err.println("Falló una notificación push de prueba");
                    }
                }
            }
        } catch (Exception | LinkageError e) {
            System.err.println("No se pudo ejecutar la prueba Web Push: " + e.getMessage());
        }
        return enviadas;
    }

    public void enviarPendientes() {
        registrarBouncyCastle();
        String publica = limpiarClave(System.getenv("VAPID_PUBLIC_KEY"),
                "VAPID_PUBLIC_KEY=");
        String privada = limpiarClave(System.getenv("VAPID_PRIVATE_KEY"),
                "VAPID_PRIVATE_KEY=");
        String asunto = System.getenv().getOrDefault(
                "VAPID_SUBJECT", "https://gps-control.onrender.com");
        if (vacio(publica) || vacio(privada)) return;
        if (publica.length() != 87 || privada.length() != 43) {
            System.err.println("Las claves VAPID están incompletas");
            return;
        }

        String sql = "SELECT n.id,n.tipo,n.titulo,n.mensaje,"
                + "s.id AS suscripcion_id,s.endpoint,"
                + "s.clave_p256dh,s.clave_auth FROM notificaciones n "
                + "CROSS JOIN suscripciones_push s "
                + "WHERE n.push_enviado_en IS NULL AND s.activa=TRUE "
                + "ORDER BY n.id";

        try {
            PushService servicio = new PushServiceCompatible(publica, privada, asunto);
            try (Connection con = Conexion.obtener();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                long notificacionActual = -1;
                boolean enviada = false;
                while (rs.next()) {
                    long id = rs.getLong("id");
                    if (notificacionActual != -1 && id != notificacionActual && enviada) {
                        marcarEnviada(con, notificacionActual);
                        enviada = false;
                    }
                    notificacionActual = id;
                    String payload = payload(rs.getString("tipo"),
                            rs.getString("titulo"), rs.getString("mensaje"));
                    try {
                        Notification notificacion = new Notification(
                                rs.getString("endpoint"),
                                rs.getString("clave_p256dh"),
                                rs.getString("clave_auth"),
                                payload.getBytes(StandardCharsets.UTF_8));
                        HttpResponse respuesta = servicio.send(
                                notificacion, Encoding.AES128GCM);
                        int estado = respuesta.getStatusLine().getStatusCode();
                        boolean envioActual = estado >= 200 && estado < 300;
                        if (envioActual) {
                            enviada = true;
                        } else {
                            if (suscripcionVencida(estado)) {
                                desactivarSuscripcion(
                                        con, rs.getLong("suscripcion_id"));
                            }
                            System.err.println("Servicio push rechazó el envío. HTTP "
                                    + estado + " - " + detalleRespuesta(respuesta));
                        }
                    } catch (Exception e) {
                        System.err.println("No se envió una notificación a un dispositivo");
                    }
                }
                if (notificacionActual != -1 && enviada) {
                    marcarEnviada(con, notificacionActual);
                }
            }
        } catch (Exception | LinkageError e) {
            System.err.println("Web Push no pudo inicializarse: " + e.getMessage());
        }
    }

    private void marcarEnviada(Connection con, long id) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE notificaciones SET push_enviado_en=CURRENT_TIMESTAMP WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private boolean suscripcionVencida(int estadoHttp) {
        return estadoHttp == 404 || estadoHttp == 410;
    }

    private void desactivarSuscripcion(Connection con, long id) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE suscripciones_push SET activa=FALSE, "
                        + "actualizado_en=CURRENT_TIMESTAMP WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private String payload(String tipo, String titulo, String mensaje) {
        return "{\"tipo\":\"" + escapar(tipo) + "\",\"titulo\":\""
                + escapar(titulo) + "\",\"mensaje\":\"" + escapar(mensaje)
                + "\",\"url\":\"./\"}";
    }

    private String escapar(String valor) {
        return valor == null ? "" : valor.replace("\\", "\\\\")
                .replace("\"", "\\\"").replace("\n", "\\n");
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private void registrarBouncyCastle() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private String detalleRespuesta(HttpResponse respuesta) {
        try {
            if (respuesta.getEntity() == null) return "sin detalle";
            String detalle = EntityUtils.toString(
                    respuesta.getEntity(), StandardCharsets.UTF_8)
                    .replaceAll("[\\r\\n]+", " ").trim();
            return detalle.substring(0, Math.min(detalle.length(), 500));
        } catch (Exception e) {
            return "sin detalle";
        }
    }

    private String limpiarClave(String valor, String prefijo) {
        if (valor == null) return null;
        String limpia = valor.trim();
        if (limpia.startsWith(prefijo)) limpia = limpia.substring(prefijo.length());
        return limpia.replaceAll("\\s", "");
    }

    /**
     * web-push 5.1.2 agrega el encabezado Crypto-Key de la especificacion
     * anterior incluso al usar aes128gcm. En aes128gcm la clave VAPID ya va
     * en Authorization (parametro k), por lo que se elimina el encabezado
     * redundante que algunos proveedores rechazan con HTTP 403.
     */
    private static final class PushServiceCompatible extends PushService {
        private PushServiceCompatible(String publicKey, String privateKey,
                                      String subject)
                throws GeneralSecurityException {
            super(publicKey, privateKey, subject);
        }

        @Override
        public HttpPost preparePost(Notification notification, Encoding encoding)
                throws GeneralSecurityException, IOException, JoseException {
            HttpPost solicitud = super.preparePost(notification, encoding);
            if (encoding == Encoding.AES128GCM) {
                solicitud.removeHeaders("Crypto-Key");
            }
            return solicitud;
        }
    }
}
