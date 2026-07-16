package com.apirest.modelo;

public class VehiculoGPS {

    private int id;
    private int clienteId;

    private String vehiculo;
    private String placa;
    private String fechaInstalacion;

    private String tipoGps;
    private String imei;
    private String telefonia;
    private String numeroSim;
    private String numeroTelefono;

    private String promocion;
    private String fechaFinPromocion;
    private String descripcionPromocion;

    private double montoOriginal;
    private double montoPromocion;
    private String tipoPlan;
    private String fechaFinPlanAnual;
    private String estadoPlanAnual;

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

    public String getTelefonia() {
        return telefonia;
    }

    public void setTelefonia(String telefonia) {
        this.telefonia = telefonia;
    }

    public String getNumeroSim() {
        return numeroSim;
    }

    public void setNumeroSim(String numeroSim) {
        this.numeroSim = numeroSim;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
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

    public double getMontoOriginal() {
        return montoOriginal;
    }

    public void setMontoOriginal(double montoOriginal) {
        this.montoOriginal = montoOriginal;
    }

    public double getMontoPromocion() {
        return montoPromocion;
    }

    public void setMontoPromocion(double montoPromocion) {
        this.montoPromocion = montoPromocion;
    }

    public String getTipoPlan() { return tipoPlan; }
    public void setTipoPlan(String tipoPlan) { this.tipoPlan = tipoPlan; }
    public String getFechaFinPlanAnual() { return fechaFinPlanAnual; }
    public void setFechaFinPlanAnual(String fechaFinPlanAnual) { this.fechaFinPlanAnual = fechaFinPlanAnual; }
    public String getEstadoPlanAnual() { return estadoPlanAnual; }
    public void setEstadoPlanAnual(String estadoPlanAnual) { this.estadoPlanAnual = estadoPlanAnual; }
}
