package com.apirest.modelo;

public class CambiarEstadoRequest {
    private boolean activo;

    public CambiarEstadoRequest() {
    }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
