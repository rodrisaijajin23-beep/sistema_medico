package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexion {

    
    private static final String HOST = System.getenv().getOrDefault("DB_HOST", "localhost");
    private static final String PORT = System.getenv().getOrDefault("DB_PORT", "3306");
    private static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "CLINICA");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "app_user");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "clinica123");

    // connectTimeout en MySQL se define en milisegundos (5000 ms = 5 segundos)
    // Esto mantiene tu estrategia de tolerancia a fallos
    private static final int LOGIN_TIMEOUT_MS = 5000;

    
    // URL estructurada correctamente para el driver de MySQL
    private static final String URL = 
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME 
            + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&connectTimeout=" + LOGIN_TIMEOUT_MS;

    public Connection getConexion() {
        Connection con = null;
        try {
            // Driver actualizado para MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
            System.out.println("Conexión exitosa a MySQL");
        } catch (ClassNotFoundException | SQLException e) {
            // No relanzamos la excepción: dejamos que el DAO reciba con = null
            // y decida el fallback para no matar el sistema con un stacktrace.
            System.err.println("Error de conexión a la base de datos: " + e.getMessage());
        }
        return con;
    }
}