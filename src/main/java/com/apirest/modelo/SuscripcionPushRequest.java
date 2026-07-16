package com.apirest.modelo;

public class SuscripcionPushRequest {
    private String endpoint;
    private String p256dh;
    private String auth;
    private String endpointAnterior;

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getP256dh() { return p256dh; }
    public void setP256dh(String p256dh) { this.p256dh = p256dh; }
    public String getAuth() { return auth; }
    public void setAuth(String auth) { this.auth = auth; }
    public String getEndpointAnterior() { return endpointAnterior; }
    public void setEndpointAnterior(String endpointAnterior) { this.endpointAnterior = endpointAnterior; }
}
