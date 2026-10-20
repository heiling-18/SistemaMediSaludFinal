package com.medisalud.entity;

public class Medico extends Persona {

    private String especialidad;

    public Medico(String nombreCompleto, String codigo, String especialidad) {
        super(nombreCompleto, codigo);
        setEspecialidad(especialidad);
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.trim().isEmpty()) {
            throw new IllegalArgumentException("La especialidad no puede estar vacia");
        }
        if (!especialidad.trim().matches("[\\p{L} .'-]+")) {
            throw new IllegalArgumentException("La especialidad solo puede contener letras y espacios (no se permiten numeros)");
        }
        this.especialidad = especialidad.trim();
    }

    @Override
    public String toString() {
        return super.toString() + " (medico, " + especialidad + ")";
    }
}
