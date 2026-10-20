package com.medisalud.persistencia.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class ConexionBD {

    private static final String URL = "jdbc:mysql://localhost:3307/medisalud";
    private static final String USUARIO = "root";
    private static final String CLAVE = "root";
    private static boolean migracionVerificada = false;

    private ConexionBD() {
    }

    public static synchronized Connection obtenerConexion() throws SQLException {
        Connection conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);
        if (!migracionVerificada) {
            asegurarColumnaMotivoCancelacion(conexion);
            migracionVerificada = true;
        }
        return conexion;
    }

    private static void asegurarColumnaMotivoCancelacion(Connection conexion) throws SQLException {
        try (ResultSet columnas = conexion.getMetaData().getColumns(null, "medisalud", "citas", "motivo_cancelacion")) {
            if (!columnas.next()) {
                try (Statement sentencia = conexion.createStatement()) {
                    sentencia.executeUpdate("ALTER TABLE citas ADD COLUMN motivo_cancelacion VARCHAR(200) NULL");
                }
            }
        }
    }
}
