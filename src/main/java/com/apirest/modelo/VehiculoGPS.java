package com.apirest.modelo;

public class VehiculoGPS {

    private int id;
    private int clienteId;

    private int cantidadDispositivos;
    private String vehiculo;
    private String placa;
    private String fechaInstalacion;

    private String tipoGps;
    private String imei;

    private String promocion;
    private String fechaFinPromocion;
    private String descripcionPromocion;

    private double montoNormal;
    private double montoPromocion;

    public VehiculoGPS() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }


    public int getCantidadDispositivos() {
        return cantidadDispositivos;
    }

    public void setCantidadDispositivos(int cantidadDispositivos) {
        this.cantidadDispositivos = cantidadDispositivos;
    }


    public String getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(String vehiculo) {
        this.vehiculo = vehiculo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getFechaInstalacion() {
        return fechaInstalacion;
    }

    public void setFechaInstalacion(String fechaInstalacion) {
        this.fechaInstalacion = fechaInstalacion;
    }


    public String getTipoGps() {
        return tipoGps;
    }

    public void setTipoGps(String tipoGps) {
        this.tipoGps = tipoGps;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }


    public String getPromocion() {
        return promocion;
    }

    public void setPromocion(String promocion) {
        this.promocion = promocion;
    }

    public String getFechaFinPromocion() {
        return fechaFinPromocion;
    }

    public void setFechaFinPromocion(String fechaFinPromocion) {
        this.fechaFinPromocion = fechaFinPromocion;
    }

    public String getDescripcionPromocion() {
        return descripcionPromocion;
    }

    public void setDescripcionPromocion(String descripcionPromocion) {
        this.descripcionPromocion = descripcionPromocion;
    }


    public double getMontoNormal() {
        return montoNormal;
    }

    public void setMontoNormal(double montoNormal) {
        this.montoNormal = montoNormal;
    }

    public double getMontoPromocion() {
        return montoPromocion;
    }

    public void setMontoPromocion(double montoPromocion) {
        this.montoPromocion = montoPromocion;
    }
}