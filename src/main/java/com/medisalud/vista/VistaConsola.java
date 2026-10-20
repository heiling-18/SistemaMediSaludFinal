package com.medisalud.vista;

import com.medisalud.controlador.ControladorMediSalud;
import com.medisalud.entity.Cita;
import com.medisalud.entity.ConstructorHistoriaClinica;
import com.medisalud.entity.Factura;
import com.medisalud.entity.FacturaConDescuentoAfiliado;
import com.medisalud.entity.FacturaConRecargoNocturno;
import com.medisalud.entity.HistoriaClinica;
import com.medisalud.entity.Medico;
import com.medisalud.entity.Paciente;
import com.medisalud.exception.CitaNoEncontradaException;
import com.medisalud.exception.MedicoNoEncontradoException;
import com.medisalud.exception.PacienteNoEncontradoException;
import com.medisalud.exception.TransicionInvalidaException;
import com.medisalud.service.EstrategiaCosto;
import com.medisalud.service.EstrategiaCostoConsultaEspecialista;
import com.medisalud.service.EstrategiaCostoConsultaGeneral;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class VistaConsola {

    private final ControladorMediSalud controlador;
    private final Scanner lector = new Scanner(System.in);

    public VistaConsola(ControladorMediSalud controlador) {
        this.controlador = controlador;
    }

    public void mostrarMenu() {
        boolean continuar = true;
        while (continuar) {
            System.out.println();
            System.out.println("=== Menu principal - MediSalud ===");
            System.out.println("1. Registrar paciente");
            System.out.println("2. Registrar medico");
            System.out.println("3. Agendar cita");
            System.out.println("4. Consultar historia clinica");
            System.out.println("5. Facturar consulta");
            System.out.println("6. Generar reporte");
            System.out.println("7. Salir");
            System.out.print("Elegir una opcion: ");
            String opcion = lector.nextLine();

            switch (opcion) {
                case "1":
                    registrarPaciente();
                    break;
                case "2":
                    registrarMedico();
                    break;
                case "3":
                    agendarCita();
                    break;
                case "4":
                    consultarHistoriaClinica();
                    break;
                case "5":
                    facturarConsulta();
                    break;
                case "6":
                    generarReporte();
                    break;
                case "7":
                    continuar = false;
                    break;
                default:
                    System.out.println("Opcion no valida");
            }
        }
        System.out.println("Cerrando el Sistema de Gestion de Citas Medicas MediSalud. Hasta pronto.");
    }

    private void registrarPaciente() {
        try {
            String nombreCompleto = leerSoloLetras("Nombre completo: ");
            String codigo = leerSoloNumeros("Documento / Codigo (solo numeros): ");
            int edad = leerEdad("Edad (0-120): ");

            Paciente paciente;
            if (leerSiNo("Desea registrar antecedentes y alergias? (s/n): ")) {
                String antecedentes = leerNoVacio("Antecedentes: ");
                String alergias = leerNoVacio("Alergias: ");
                HistoriaClinica historia = new ConstructorHistoriaClinica()
                        .conAntecedentes(antecedentes)
                        .conAlergias(alergias)
                        .construir();
                paciente = new Paciente(nombreCompleto, codigo, edad, historia);
            } else {
                paciente = new Paciente(nombreCompleto, codigo, edad);
            }

            controlador.registrarPaciente(paciente);
            System.out.println("Paciente registrado: " + paciente);
        } catch (IllegalArgumentException excepcion) {
            System.out.println("No se pudo registrar el paciente: " + excepcion.getMessage());
        }
    }

    private void registrarMedico() {
        try {
            String nombreCompleto = leerSoloLetras("Nombre completo: ");
            String codigo = leerSoloNumeros("Documento / Codigo (solo numeros): ");
            String especialidad = leerSoloLetras("Especialidad: ");

            Medico medico = new Medico(nombreCompleto, codigo, especialidad);
            controlador.registrarMedico(medico);
            System.out.println("Medico registrado: " + medico);
        } catch (IllegalArgumentException excepcion) {
            System.out.println("No se pudo registrar el medico: " + excepcion.getMessage());
        }
    }

    private void agendarCita() {
        try {
            String codigoCita = leerNoVacio("Codigo de la cita: ");
            String codigoPaciente = leerNoVacio("Documento / Codigo del paciente: ");
            String codigoMedico = leerNoVacio("Documento / Codigo del medico: ");
            LocalDate fecha = leerFecha("Fecha (aaaa-mm-dd): ");

            Cita cita = controlador.agendarCita(codigoCita, codigoPaciente, codigoMedico, fecha);
            System.out.println("Cita agendada: " + cita);

            if (leerSiNo("Confirmar la cita ahora? (s/n): ")) {
                controlador.confirmarCita(codigoCita);
                if (leerSiNo("Registrar atencion con diagnostico? (s/n): ")) {
                    String diagnostico = leerNoVacio("Diagnostico: ");
                    controlador.atenderCita(codigoCita, diagnostico);
                }
                System.out.println("Estado actualizado: " + controlador.buscarCita(codigoCita).getEstado());
            }
        } catch (PacienteNoEncontradoException | MedicoNoEncontradoException
                | CitaNoEncontradaException | TransicionInvalidaException
                | DateTimeParseException | IllegalArgumentException excepcion) {
            System.out.println("No se pudo agendar la cita: " + excepcion.getMessage());
        }
    }

    private void consultarHistoriaClinica() {
        try {
            String codigoPaciente = leerNoVacio("Documento / Codigo del paciente: ");
            HistoriaClinica historia = controlador.consultarHistoriaClinica(codigoPaciente);
            System.out.println(historia);
            if (historia.getConsultas().isEmpty()) {
                System.out.println("Sin consultas registradas todavia.");
            }
            for (String consulta : historia.getConsultas()) {
                System.out.println("- " + consulta);
            }
        } catch (PacienteNoEncontradoException excepcion) {
            System.out.println("No se pudo consultar: " + excepcion.getMessage());
        }
    }

    private void facturarConsulta() {
        try {
            String codigoCita = leerNoVacio("Codigo de la cita: ");
            String tipo = leerTipoConsulta("Tipo de consulta (G=general, E=especialista): ");
            EstrategiaCosto estrategiaCosto = tipo.equalsIgnoreCase("E")
                    ? new EstrategiaCostoConsultaEspecialista()
                    : new EstrategiaCostoConsultaGeneral();

            Factura factura = controlador.facturarCita(codigoCita, estrategiaCosto);

            if (leerSiNo("Es paciente afiliado? (s/n): ")) {
                factura = new FacturaConDescuentoAfiliado(factura);
            }
            if (leerSiNo("Es horario nocturno? (s/n): ")) {
                factura = new FacturaConRecargoNocturno(factura);
            }

            System.out.println(factura);
        } catch (CitaNoEncontradaException excepcion) {
            System.out.println("No se pudo facturar: " + excepcion.getMessage());
        }
    }

    private void generarReporte() {
        try {
            String codigoMedico = leerNoVacio("Codigo del medico para ver sus citas: ");
            List<Cita> citas = controlador.generarReportePorMedico(codigoMedico);
            imprimirTitulo("Citas del medico " + codigoMedico + " (" + citas.size() + ")");
            if (citas.isEmpty()) {
                System.out.println("  Sin citas registradas.");
            } else {
                String formato = "  %-6s | %-24s | %-10s | %-10s%n";
                System.out.printf(formato, "Cita", "Paciente", "Fecha", "Estado");
                System.out.println("  " + "-".repeat(7) + "+" + "-".repeat(26) + "+" + "-".repeat(12) + "+" + "-".repeat(11));
                for (Cita cita : citas) {
                    System.out.printf(formato, cita.getCodigo(), cita.getPaciente().getNombreCompleto(),
                            cita.getFecha(), cita.getEstado());
                }
            }

            Map<String, Double> totalPorMedico = controlador.generarReporteFacturacionPorMedico();
            imprimirTitulo("Total facturado por medico");
            if (totalPorMedico.isEmpty()) {
                System.out.println("  Sin facturas emitidas.");
            } else {
                double granTotal = 0;
                for (Map.Entry<String, Double> fila : totalPorMedico.entrySet()) {
                    System.out.printf("  %-10s | $%,12.2f%n", fila.getKey(), fila.getValue());
                    granTotal += fila.getValue();
                }
                System.out.printf("  %-10s | $%,12.2f%n", "TOTAL", granTotal);
            }

            int edadMinima = leerEdad("Edad minima para el reporte de pacientes: ");
            List<Paciente> mayores = controlador.listarPacientesMayoresDeEdad(edadMinima);
            imprimirTitulo("Pacientes de " + edadMinima + " anios o mas (" + mayores.size() + ")");
            if (mayores.isEmpty()) {
                System.out.println("  Ningun paciente cumple el criterio.");
            } else {
                String formato = "  %-8s | %-26s | %4s%n";
                System.out.printf(formato, "Codigo", "Nombre", "Edad");
                System.out.println("  " + "-".repeat(9) + "+" + "-".repeat(28) + "+" + "-".repeat(6));
                for (Paciente paciente : mayores) {
                    System.out.printf(formato, paciente.getCodigo(), paciente.getNombreCompleto(), paciente.getEdad());
                }
            }
            System.out.println();
        } catch (NumberFormatException excepcion) {
            System.out.println("Edad invalida: " + excepcion.getMessage());
        }
    }

    private String leerSoloLetras(String etiqueta) {
        while (true) {
            System.out.print(etiqueta);
            String entrada = lector.nextLine().trim();
            if (entrada.isEmpty()) {
                System.out.println("  [!] Este campo no puede estar vacio.");
                continue;
            }
            if (!entrada.matches("[\\p{L} .'-]+")) {
                System.out.println("  [!] Solo se permiten letras y espacios (no ingrese numeros ni simbolos).");
                continue;
            }
            return entrada;
        }
    }

    private String leerSoloNumeros(String etiqueta) {
        while (true) {
            System.out.print(etiqueta);
            String entrada = lector.nextLine().trim();
            if (entrada.isEmpty()) {
                System.out.println("  [!] El documento/codigo no puede estar vacio.");
                continue;
            }
            if (!entrada.matches("\\d+")) {
                System.out.println("  [!] El documento/codigo solo puede contener numeros (no se permiten letras).");
                continue;
            }
            return entrada;
        }
    }

    private int leerEdad(String etiqueta) {
        while (true) {
            System.out.print(etiqueta);
            String entrada = lector.nextLine().trim();
            try {
                int edad = Integer.parseInt(entrada);
                if (edad < 0 || edad > 120) {
                    System.out.println("  [!] La edad debe estar entre 0 y 120 anios.");
                    continue;
                }
                return edad;
            } catch (NumberFormatException e) {
                System.out.println("  [!] Debe ingresar un numero entero valido (no se permiten letras).");
            }
        }
    }

    private boolean leerSiNo(String etiqueta) {
        while (true) {
            System.out.print(etiqueta);
            String entrada = lector.nextLine().trim();
            if (entrada.equalsIgnoreCase("s")) {
                return true;
            }
            if (entrada.equalsIgnoreCase("n")) {
                return false;
            }
            System.out.println("  [!] Opcion invalida. Escriba 's' para si o 'n' para no.");
        }
    }

    private String leerTipoConsulta(String etiqueta) {
        while (true) {
            System.out.print(etiqueta);
            String entrada = lector.nextLine().trim();
            if (entrada.equalsIgnoreCase("G") || entrada.equalsIgnoreCase("E")) {
                return entrada.toUpperCase();
            }
            System.out.println("  [!] Opcion invalida. Escriba 'G' (general) o 'E' (especialista).");
        }
    }

    private LocalDate leerFecha(String etiqueta) {
        while (true) {
            System.out.print(etiqueta);
            String entrada = lector.nextLine().trim();
            try {
                return LocalDate.parse(entrada);
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Formato de fecha invalido. Use el formato aaaa-mm-dd (ejemplo: 2026-10-25).");
            }
        }
    }

    private String leerNoVacio(String etiqueta) {
        while (true) {
            System.out.print(etiqueta);
            String entrada = lector.nextLine().trim();
            if (!entrada.isEmpty()) {
                return entrada;
            }
            System.out.println("  [!] Este campo no puede estar vacio.");
        }
    }

    private void imprimirTitulo(String titulo) {
        System.out.println();
        System.out.println("=== " + titulo + " ===");
    }
}
