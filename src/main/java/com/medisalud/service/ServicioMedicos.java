package com.medisalud.service;

import com.medisalud.entity.Medico;
import com.medisalud.exception.MedicoNoEncontradoException;
import com.medisalud.repository.RepositorioMedicos;
import java.util.List;

public class ServicioMedicos {

    private final RepositorioMedicos repositorioMedicos;

    public ServicioMedicos(RepositorioMedicos repositorioMedicos) {
        this.repositorioMedicos = repositorioMedicos;
    }

    public void registrarMedico(Medico medico) {
        String codigo = medico.getCodigo();
        if (!codigo.matches("\\d+")) {
            throw new IllegalArgumentException("El documento/codigo del medico no puede contener letras ni simbolos (solo numeros)");
        }
        if (repositorioMedicos.buscarPorCodigo(codigo) != null) {
            throw new IllegalArgumentException("Ya existe un medico registrado con el documento/codigo " + codigo);
        }
        repositorioMedicos.guardar(medico);
    }

    public Medico buscarPorCodigo(String codigo) throws MedicoNoEncontradoException {
        Medico medico = repositorioMedicos.buscarPorCodigo(codigo);
        if (medico == null) {
            throw new MedicoNoEncontradoException(codigo);
        }
        return medico;
    }

    public List<Medico> listarTodos() {
        return repositorioMedicos.listarTodos();
    }
}
