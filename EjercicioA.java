import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Ejercicio A: Respuesta en streaming (API de Claude)
 * Lee eventos SSE (Server-Sent Events) del formato event:/data: y reconstruye
 * cada mensaje completo. Al llegar message_stop imprime 4 lineas:
 * texto: <texto completo>
 * stop_reason: <razon>
 * input_tokens: N
 * output_tokens: N
 * Los eventos ping se ignoran.
 * La entrada puede tener varios mensajes seguidos.
 */
public class EjercicioA {

    public static void main(String[] args) throws IOException {
        configurarSalida();
        List<String> lineas = leerLineas();
        resolver(lineas);
    }

    public static void resolver(List<String> lineas) {
        // bloques: index -> texto acumulado
        TreeMap<Integer, StringBuilder> bloques = new TreeMap<>();
        long inputTokens = 0;
        long outputTokens = 0;
        String stopReason = "";

        for (String linea : lineas) {
            // Solo nos interesan las lineas "data:"
            if (!linea.startsWith("data:"))
                continue;

            String data = linea.substring(5).trim();

            if (data.contains("\"message_start\"")) {
                // Nuevo mensaje: resetear estado
                bloques.clear();
                stopReason = "";
                outputTokens = 0;
                // input_tokens esta dentro de "usage"
                inputTokens = extraerLong(data, "\"input_tokens\"");

            } else if (data.contains("\"content_block_delta\"")) {
                // Acumular texto en el bloque correspondiente
                int index = (int) extraerLong(data, "\"index\"");
                // El texto esta dentro del campo "delta"
                int posDelta = data.indexOf("\"delta\"");
                String texto = "";
                if (posDelta >= 0) {
                    texto = extraerString(data.substring(posDelta), "\"text\"");
                }
                bloques.computeIfAbsent(index, k -> new StringBuilder()).append(texto);

            } else if (data.contains("\"message_delta\"")) {
                // Obtener stop_reason y output_tokens
                stopReason = extraerString(data, "\"stop_reason\"");
                outputTokens = extraerLong(data, "\"output_tokens\"");

            } else if (data.contains("\"message_stop\"")) {
                // Unir todos los bloques en orden de index
                StringBuilder textoCompleto = new StringBuilder();
                for (StringBuilder sb : bloques.values()) {
                    textoCompleto.append(sb);
                }
                System.out.println("texto: " + textoCompleto);
                System.out.println("stop_reason: " + stopReason);
                System.out.println("input_tokens: " + inputTokens);
                System.out.println("output_tokens: " + outputTokens);
            }
            // ping y otros eventos se ignoran
        }
    }

    /** Lee todas las lineas de System.in con soporte UTF-8. */
    public static List<String> leerLineas() throws IOException {
        // Forzamos UTF-8 en stdin para que las tildes lleguen bien
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in, java.nio.charset.StandardCharsets.UTF_8));
        List<String> lineas = new ArrayList<>();
        String l;
        while ((l = in.readLine()) != null) {
            lineas.add(l);
        }
        return lineas;
    }

    /** Configura stdout en UTF-8 para que las tildes se impriman correctamente. */
    public static void configurarSalida() {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Extrae el valor de cadena de una clave JSON simple.
     * Busca: "clave": "valor" (maneja escapes basicos \n \t \" \\).
     */
    public static String extraerString(String json, String clave) {
        int pos = json.indexOf(clave);
        if (pos < 0)
            return "";
        int dosPuntos = json.indexOf(':', pos + clave.length());
        if (dosPuntos < 0)
            return "";
        // Avanzar hasta la primera comilla de apertura
        int i = dosPuntos + 1;
        while (i < json.length() && json.charAt(i) != '"')
            i++;
        if (i >= json.length())
            return "";
        i++; // saltar la comilla inicial

        StringBuilder sb = new StringBuilder();
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char sig = json.charAt(i + 1);
                switch (sig) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    default:
                        sb.append(sig);
                        break;
                }
                i += 2;
            } else if (c == '"') {
                break; // comilla de cierre
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * Extrae el valor numerico de una clave JSON simple.
     * Busca: "clave": 123
     */
    public static long extraerLong(String json, String clave) {
        int pos = json.indexOf(clave);
        if (pos < 0)
            return 0;
        int dosPuntos = json.indexOf(':', pos + clave.length());
        if (dosPuntos < 0)
            return 0;

        int i = dosPuntos + 1;
        // Saltamos espacios y posibles comillas (por si el numero viene como string)
        while (i < json.length() && (json.charAt(i) == ' ' || json.charAt(i) == '"'))
            i++;

        int inicio = i;
        if (i < json.length() && json.charAt(i) == '-')
            i++;
        while (i < json.length() && Character.isDigit(json.charAt(i)))
            i++;

        if (inicio == i)
            return 0;
        try {
            return Long.parseLong(json.substring(inicio, i));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
