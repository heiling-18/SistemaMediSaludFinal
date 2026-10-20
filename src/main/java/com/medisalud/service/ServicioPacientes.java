package com.medisalud.service;

import com.medisalud.entity.Paciente;
import com.medisalud.exception.PacienteNoEncontradoException;
import com.medisalud.repository.RepositorioPacientes;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ServicioPacientes {

    private final RepositorioPacientes repositorioPacientes;

    public ServicioPacientes(RepositorioPacientes repositorioPacientes) {
        this.repositorioPacientes = repositorioPacientes;
    }

    public void registrarPaciente(Paciente paciente) {
        String codigo = paciente.getCodigo();
        if (!codigo.matches("\\d+")) {
            throw new IllegalArgumentException("El documento/codigo del paciente no puede contener letras ni simbolos (solo numeros)");
        }
        if (repositorioPacientes.buscarPorCodigo(codigo) != null) {
            throw new IllegalArgumentException("Ya existe un paciente registrado con el documento/codigo " + codigo);
        }
        repositorioPacientes.guardar(paciente);
    }

    public Paciente buscarPorCodigo(String codigo) throws PacienteNoEncontradoException {
        Paciente paciente = repositorioPacientes.buscarPorCodigo(codigo);
        if (paciente == null) {
            throw new PacienteNoEncontradoException(codigo);
        }
        return paciente;
    }

    public List<Paciente> listarTodos() {
        return repositorioPacientes.listarTodos();
    }

    public List<Paciente> listarMayoresDeEdad(int edadMinima) {
        Predicate<Paciente> esMayorDeEdad = paciente -> paciente.getEdad() >= edadMinima;
        return repositorioPacientes.listarTodos().stream()
                .filter(esMayorDeEdad)
                .collect(Collectors.toList());
    }
}
