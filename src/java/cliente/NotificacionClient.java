/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cliente;

/**
 *
 * @author Rodrigo
 */
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Cliente que el Servicio Principal usa para comunicarse por red con el
 * Servicio Secundario (servicio-notificaciones). Es intercambio real de
 * mensajes HTTP entre dos procesos independientes, no una llamada local.
 *
 * Implementa tolerancia a fallos: timeout corto de conexion/lectura y manejo
 * de excepciones para que, si el Servicio Secundario esta caido o lento, el
 * Servicio Principal NO se cuelgue ni falle: simplemente continua operando
 * parcialmente (la cita queda registrada aunque la notificacion falle).
 */
public class NotificacionClient {

    private static final String NOTIFICACIONES_URL =
            System.getenv().getOrDefault("NOTIFICACIONES_URL", "http://localhost:9090/notificar");

    private static final int TIMEOUT_MS = 3000; // 3 segundos: si no responde, se hace fallback

    /**
     * Intenta notificar. Devuelve true/false pero NUNCA lanza una excepcion
     * hacia el llamador: este metodo esta pensado como un "circuit breaker"
     * simple, para que un fallo del Servicio Secundario nunca tumbe al
     * Servicio Principal.
     */
    public boolean notificar(String mensajeJson) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(NOTIFICACIONES_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(mensajeJson.getBytes(StandardCharsets.UTF_8));
            }

            int codigo = conn.getResponseCode();
            return codigo >= 200 && codigo < 300;
        } catch (Exception e) {
            // Timeout, servicio caido, DNS, etc. Se registra el fallo pero
            // NO se propaga: este es el fallback controlado exigido por la rubrica.
            System.err.println("Servicio Secundario no disponible (fallback aplicado): " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
