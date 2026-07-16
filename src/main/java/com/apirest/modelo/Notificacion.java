package com.apirest.modelo;

public class Notificacion {
    private long id;
    private String tipo;
    private String titulo;
    private String mensaje;
    private Integer vehiculoId;
    private Integer clienteId;
    private String fechaEvento;
    private boolean leida;
    private String creadoEn;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public Integer getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Integer vehiculoId) { this.vehiculoId = vehiculoId; }
    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    public String getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(String fechaEvento) { this.fechaEvento = fechaEvento; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public String getCreadoEn() { return creadoEn; }
    public void setCreadoEn(String creadoEn) { this.creadoEn = creadoEn; }
}
