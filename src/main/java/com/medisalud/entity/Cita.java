package com.medisalud.entity;

import com.medisalud.notificacion.ObservadorCita;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cita implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int LONGITUD_MAXIMA_MOTIVO = 200;

    private final String codigo;
    private final Paciente paciente;
    private final Medico medico;
    private final LocalDate fecha;
    private EstadoCita estado;
    private String motivoCancelacion;
    private transient List<ObservadorCita> observadores = new ArrayList<>();

    public Cita(String codigo, Paciente paciente, Medico medico, LocalDate fecha) {
        this(codigo, paciente, medico, fecha, EstadoCita.PENDIENTE, null);
    }

    public Cita(String codigo, Paciente paciente, Medico medico, LocalDate fecha,
                EstadoCita estado, String motivoCancelacion) {
        this.codigo = codigo;
        this.paciente = paciente;
        this.medico = medico;
        this.fecha = fecha;
        this.estado = estado;
        this.motivoCancelacion = motivoCancelacion;
    }

    public void agregarObservador(ObservadorCita observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void cambiarEstado(EstadoCita nuevoEstado) {
        this.estado = nuevoEstado;
        for (ObservadorCita observador : observadores) {
            observador.notificarCambioEstado(this);
        }
    }

    public void cancelar(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de cancelacion es obligatorio y no puede estar vacio");
        }
        String motivoLimpio = motivo.trim();
        if (motivoLimpio.length() > LONGITUD_MAXIMA_MOTIVO) {
            throw new IllegalArgumentException(
                    "El motivo de cancelacion no puede superar los " + LONGITUD_MAXIMA_MOTIVO + " caracteres");
        }
        this.motivoCancelacion = motivoLimpio;
        cambiarEstado(EstadoCita.CANCELADA);
    }

    public String getCodigo() {
        return codigo;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public String obtenerTextoMotivo() {
        if (estado != EstadoCita.CANCELADA) {
            return "";
        }
        if (motivoCancelacion == null || motivoCancelacion.trim().isEmpty()) {
            return "Sin motivo registrado";
        }
        return motivoCancelacion;
    }

    @Override
    public String toString() {
        String base = "Cita " + codigo + " [" + estado + "] " + paciente.getNombreCompleto()
                + " con " + medico.getNombreCompleto() + " el " + fecha;
        if (estado == EstadoCita.CANCELADA) {
            return base + " - Motivo: " + obtenerTextoMotivo();
        }
        return base;
    }

    private void readObject(ObjectInputStream entrada) throws IOException, ClassNotFoundException {
        entrada.defaultReadObject();
        observadores = new ArrayList<>();
    }
}
