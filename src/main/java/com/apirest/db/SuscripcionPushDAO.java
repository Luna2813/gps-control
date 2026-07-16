package com.apirest.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SuscripcionPushDAO {
    public boolean guardar(long usuarioId, String endpoint, String p256dh,
                           String auth, String agente, String endpointAnterior) {
        String sql = "INSERT INTO suscripciones_push "
                + "(usuario_id,endpoint,clave_p256dh,clave_auth,agente_usuario) "
                + "VALUES (?,?,?,?,?) ON CONFLICT (endpoint) DO UPDATE SET "
                + "usuario_id=EXCLUDED.usuario_id, clave_p256dh=EXCLUDED.clave_p256dh, "
                + "clave_auth=EXCLUDED.clave_auth, agente_usuario=EXCLUDED.agente_usuario, "
                + "activa=TRUE, actualizado_en=CURRENT_TIMESTAMP";
        try (Connection con = Conexion.obtener()) {
            String agenteLimitado = agente == null ? null
                    : agente.substring(0, Math.min(agente.length(), 300));
            // El agente de usuario no identifica un dispositivo: dos teléfonos
            // o computadoras pueden informar exactamente el mismo valor. Cada
            // endpoint push se conserva como una suscripción independiente.
            if (endpointAnterior != null && !endpointAnterior.isBlank()
                    && !endpointAnterior.equals(endpoint)) {
                try (PreparedStatement anterior = con.prepareStatement(
                        "UPDATE suscripciones_push SET activa=FALSE, "
                                + "actualizado_en=CURRENT_TIMESTAMP "
                                + "WHERE usuario_id=? AND endpoint=?")) {
                    anterior.setLong(1, usuarioId);
                    anterior.setString(2, endpointAnterior);
                    anterior.executeUpdate();
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, usuarioId); ps.setString(2, endpoint);
            ps.setString(3, p256dh); ps.setString(4, auth);
            ps.setString(5, agenteLimitado);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("No fue posible guardar la suscripción push: " + e.getMessage());
            return false;
        }
    }
}
