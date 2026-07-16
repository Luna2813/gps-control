package com.apirest.modelo;

public class Auditoria {
    private long id;
    private Long usuarioId;
    private String usuario;
    private String rol;
    private String metodo;
    private String ruta;
    private int estadoHttp;
    private String direccionIp;
    private String navegador;
    private String creadoEn;

    public Auditoria() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }
    public int getEstadoHttp() { return estadoHttp; }
    public void setEstadoHttp(int estadoHttp) { this.estadoHttp = estadoHttp; }
    public String getDireccionIp() { return direccionIp; }
    public void setDireccionIp(String direccionIp) { this.direccionIp = direccionIp; }
    public String getNavegador() { return navegador; }
    public void setNavegador(String navegador) { this.navegador = navegador; }
    public String getCreadoEn() { return creadoEn; }
    public void setCreadoEn(String creadoEn) { this.creadoEn = creadoEn; }
}
