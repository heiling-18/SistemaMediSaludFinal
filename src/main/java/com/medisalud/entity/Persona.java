package com.medisalud.entity;

import java.io.Serializable;

public abstract class Persona implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombreCompleto;
    private String codigo;

    protected Persona(String nombreCompleto, String codigo) {
        setNombreCompleto(nombreCompleto);
        setCodigo(codigo);
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre completo no puede estar vacio");
        }
        if (!nombreCompleto.trim().matches("[\\p{L} .'-]+")) {
            throw new IllegalArgumentException("El nombre completo solo puede contener letras y espacios (no se permiten numeros)");
        }
        this.nombreCompleto = nombreCompleto.trim();
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento/codigo no puede estar vacio");
        }
        this.codigo = codigo.trim();
    }

    @Override
    public String toString() {
        return codigo + " - " + nombreCompleto;
    }
}
