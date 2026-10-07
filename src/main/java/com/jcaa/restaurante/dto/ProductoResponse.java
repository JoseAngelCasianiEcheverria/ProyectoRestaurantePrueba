package com.jcaa.restaurante.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductoResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private boolean disponible;
    private LocalDateTime creadoEn;

    public ProductoResponse(Long id, String nombre, String descripcion, BigDecimal precio,
                            boolean disponible, LocalDateTime creadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.disponible = disponible;
        this.creadoEn = creadoEn;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
}