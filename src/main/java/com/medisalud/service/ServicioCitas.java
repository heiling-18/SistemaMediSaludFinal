package com.medisalud.service;

import com.medisalud.entity.Cita;
import com.medisalud.entity.EstadoCita;
import com.medisalud.entity.Medico;
import com.medisalud.entity.Paciente;
import com.medisalud.exception.CancelacionTardiaException;
import com.medisalud.exception.CitaNoEncontradaException;
import com.medisalud.exception.TransicionInvalidaException;
import com.medisalud.notificacion.ObservadorCita;
import com.medisalud.repository.RepositorioCitas;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServicioCitas {

    private final RepositorioCitas repositorioCitas;
    private final ObservadorCita observadorCita;

    public ServicioCitas(RepositorioCitas repositorioCitas) {
        this(repositorioCitas, null);
    }

    public ServicioCitas(RepositorioCitas repositorioCitas, ObservadorCita observadorCita) {
        this.repositorioCitas = repositorioCitas;
        this.observadorCita = observadorCita;
    }

    public Cita agendarCita(String codigo, Paciente paciente, Medico medico, LocalDate fecha) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El codigo de la cita no puede estar vacio");
        }
        String codigoLimpio = codigo.trim();
        if (repositorioCitas.buscarPorCodigo(codigoLimpio) != null) {
            throw new IllegalArgumentException("Ya existe una cita agendada con el codigo " + codigoLimpio);
        }
        Cita cita = new Cita(codigoLimpio, paciente, medico, fecha);
        repositorioCitas.guardar(cita);
        return cita;
    }

    public Cita buscarPorCodigo(String codigo) throws CitaNoEncontradaException {
        Cita cita = repositorioCitas.buscarPorCodigo(codigo);
        if (cita == null) {
            throw new CitaNoEncontradaException(codigo);
        }
        return cita;
    }

    public void confirmarCita(String codigo) throws CitaNoEncontradaException, TransicionInvalidaException {
        cambiarEstado(codigo, EstadoCita.CONFIRMADA);
    }

    public void atenderCita(String codigo, String diagnostico)
            throws CitaNoEncontradaException, TransicionInvalidaException {
        if (diagnostico == null || diagnostico.trim().isEmpty()) {
            throw new IllegalArgumentException("El diagnostico no puede estar vacio");
        }
        Cita cita = cambiarEstado(codigo, EstadoCita.ATENDIDA);
        cita.getPaciente().getHistoriaClinica().agregarConsulta(diagnostico.trim());
    }

    public Cita cancelarCita(String codigo, String motivo)
            throws CitaNoEncontradaException, TransicionInvalidaException, CancelacionTardiaException {
        Cita cita = buscarPorCodigo(codigo);
        if (!cita.getEstado().puedeTransicionarA(EstadoCita.CANCELADA)) {
            throw new TransicionInvalidaException(codigo, cita.getEstado(), EstadoCita.CANCELADA);
        }
        if (cita.getFecha().isBefore(LocalDate.now())) {
            throw new CancelacionTardiaException(codigo, cita.getFecha());
        }
        if (observadorCita != null) {
            cita.agregarObservador(observadorCita);
        }
        cita.cancelar(motivo);
        repositorioCitas.guardar(cita);
        return cita;
    }

    public List<Cita> listarPorMedico(String codigoMedico) {
        return repositorioCitas.listarPorMedico(codigoMedico);
    }

    public Map<String, Long> contarCancelacionesPorMedico() {
        return repositorioCitas.listarTodas().stream()
                .filter(cita -> cita.getEstado() == EstadoCita.CANCELADA)
                .collect(Collectors.groupingBy(
                        cita -> cita.getMedico().getCodigo(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (primero, segundo) -> primero,
                        LinkedHashMap::new));
    }

    private Cita cambiarEstado(String codigo, EstadoCita nuevoEstado)
            throws CitaNoEncontradaException, TransicionInvalidaException {
        Cita cita = buscarPorCodigo(codigo);
        if (!cita.getEstado().puedeTransicionarA(nuevoEstado)) {
            throw new TransicionInvalidaException(codigo, cita.getEstado(), nuevoEstado);
        }
        if (observadorCita != null) {
            cita.agregarObservador(observadorCita);
        }
        cita.cambiarEstado(nuevoEstado);
        repositorioCitas.guardar(cita);
        return cita;
    }
}
