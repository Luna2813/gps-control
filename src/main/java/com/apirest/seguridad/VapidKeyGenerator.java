package com.apirest.seguridad;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

public final class VapidKeyGenerator {
    private VapidKeyGenerator() {
    }

    public static void main(String[] args) throws Exception {
        KeyPairGenerator generador = KeyPairGenerator.getInstance("EC");
        generador.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair par = generador.generateKeyPair();

        ECPublicKey publica = (ECPublicKey) par.getPublic();
        ECPrivateKey privada = (ECPrivateKey) par.getPrivate();

        byte[] x = fijo(publica.getW().getAffineX(), 32);
        byte[] y = fijo(publica.getW().getAffineY(), 32);
        byte[] publicaSinComprimir = new byte[65];
        publicaSinComprimir[0] = 4;
        System.arraycopy(x, 0, publicaSinComprimir, 1, 32);
        System.arraycopy(y, 0, publicaSinComprimir, 33, 32);

        Base64.Encoder base64Url = Base64.getUrlEncoder().withoutPadding();
        String clavePublica = base64Url.encodeToString(publicaSinComprimir);
        String clavePrivada = base64Url.encodeToString(fijo(privada.getS(), 32));
        System.out.println("VAPID_PUBLIC_KEY=" + clavePublica);
        System.out.println("LONGITUD_PUBLICA=" + clavePublica.length());
        System.out.println("VAPID_PRIVATE_KEY=" + clavePrivada);
        System.out.println("LONGITUD_PRIVADA=" + clavePrivada.length());
    }

    private static byte[] fijo(BigInteger numero, int longitud) {
        byte[] original = numero.toByteArray();
        byte[] resultado = new byte[longitud];
        int inicio = Math.max(0, original.length - longitud);
        int cantidad = Math.min(original.length, longitud);
        System.arraycopy(original, inicio, resultado, longitud - cantidad, cantidad);
        return resultado;
    }
}
