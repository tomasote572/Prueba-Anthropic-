import java.io.IOException;
import java.util.List;

/**
 * Ejercicio B: Uso de herramientas (Tool use - API de Claude)
 * Cada linea es una respuesta JSON del modelo.
 * - Si stop_reason es "tool_use": ejecutar las herramientas de los bloques
 * tool_use
 * en orden e imprimir: tool_result <id> <resultado>
 * - Si stop_reason es "end_turn": imprimir: final: <texto>
 * Al final imprimir: resumen: N llamadas, E errores
 * Herramientas disponibles:
 * calculadora { operacion, a, b } -> sumar/restar/multiplicar/dividir (entero)
 * clima { ciudad } -> Bogota 14 C, Medellin 24 C, Cali 26 C
 */
public class EjercicioB {

    public static void main(String[] args) throws IOException {
        EjercicioA.configurarSalida();
        List<String> lineas = EjercicioA.leerLineas();
        resolver(lineas);
    }

    public static void resolver(List<String> lineas) {
        int totalLlamadas = 0;
        int totalErrores = 0;

        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty())
                continue;

            String stopReason = EjercicioA.extraerString(linea, "\"stop_reason\"");

            if ("end_turn".equals(stopReason)) {
                // Solo hay bloques de tipo "text"; tomamos el primero
                String texto = extraerPrimerTexto(linea);
                System.out.println("final: " + texto);

            } else if ("tool_use".equals(stopReason)) {
                // Iterar sobre cada objeto del array "content"
                String[] bloques = extraerBloques(linea);
                for (String bloque : bloques) {
                    if (!bloque.contains("\"tool_use\""))
                        continue;

                    totalLlamadas++;
                    String tid = EjercicioA.extraerString(bloque, "\"id\"");
                    String name = EjercicioA.extraerString(bloque, "\"name\"");
                    String resultado = ejecutarHerramienta(name, bloque);

                    if (resultado.startsWith("error:"))
                        totalErrores++;
                    System.out.println("tool_result " + tid + " " + resultado);
                }
            }
        }

        System.out.println("resumen: " + totalLlamadas + " llamadas, " + totalErrores + " errores");
    }

    private static String ejecutarHerramienta(String nombre, String bloque) {
        // Extraemos el sub-objeto "input" para leer sus campos
        int posInput = bloque.indexOf("\"input\"");
        String input = posInput >= 0 ? bloque.substring(posInput) : bloque;

        switch (nombre) {
            case "calculadora":
                return ejecutarCalculadora(input);
            case "clima":
                return ejecutarClima(input);
            default:
                return "error: herramienta desconocida: " + nombre;
        }
    }

    private static String ejecutarCalculadora(String input) {
        String op = EjercicioA.extraerString(input, "\"operacion\"");
        long a = EjercicioA.extraerLong(input, "\"a\"");
        long b = EjercicioA.extraerLong(input, "\"b\"");

        switch (op) {
            case "sumar":
                return String.valueOf(a + b);
            case "restar":
                return String.valueOf(a - b);
            case "multiplicar":
                return String.valueOf(a * b);
            case "dividir":
                if (b == 0)
                    return "error: divisi\u00f3n por cero";
                return String.valueOf(a / b);
            default:
                return "error: operaci\u00f3n desconocida: " + op;
        }
    }

    private static String ejecutarClima(String input) {
        String ciudad = EjercicioA.extraerString(input, "\"ciudad\"");
        // Normalizar para comparar sin tildes
        String ciudadNorm = normalizarCiudad(ciudad);
        if (ciudadNorm.equals("bogota"))
            return "14 \u00b0C";
        if (ciudadNorm.equals("medellin"))
            return "24 \u00b0C";
        if (ciudadNorm.equals("cali"))
            return "26 \u00b0C";
        return "error: ciudad desconocida: " + ciudad;
    }

    private static String normalizarCiudad(String s) {
        String nfd = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return nfd.replaceAll("[\\u0300-\\u036f]", "").toLowerCase();
    }

    /** Extrae el texto del primer bloque de tipo "text" en la linea. */
    private static String extraerPrimerTexto(String linea) {
        for (String bloque : extraerBloques(linea)) {
            if (bloque.contains("\"text\"") && !bloque.contains("\"tool_use\"")) {
                String txt = EjercicioA.extraerString(bloque, "\"text\"");
                if (!txt.isEmpty())
                    return txt;
            }
        }
        return "";
    }

    /**
     * Divide el array "content" de la linea JSON en objetos individuales
     * siguiendo el nivel de llaves { }.
     */
    private static String[] extraerBloques(String linea) {
        int posContent = linea.indexOf("\"content\"");
        if (posContent < 0)
            return new String[0];
        int inicioArray = linea.indexOf('[', posContent);
        int finArray = linea.lastIndexOf(']');
        if (inicioArray < 0 || finArray < 0 || finArray <= inicioArray)
            return new String[0];

        String contenido = linea.substring(inicioArray + 1, finArray);
        List<String> bloques = new java.util.ArrayList<>();
        int nivel = 0;
        int inicio = -1;
        boolean enString = false;
        boolean escape = false;

        for (int i = 0; i < contenido.length(); i++) {
            char c = contenido.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"') {
                enString = !enString;
                continue;
            }
            if (enString)
                continue;

            if (c == '{') {
                if (nivel == 0)
                    inicio = i;
                nivel++;
            } else if (c == '}') {
                nivel--;
                if (nivel == 0 && inicio >= 0) {
                    bloques.add(contenido.substring(inicio, i + 1));
                    inicio = -1;
                }
            }
        }
        return bloques.toArray(new String[0]);
    }
}
