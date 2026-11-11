import java.io.IOException;
import java.util.List;

/**
 * Main: punto de entrada unico.
 *
 * Lee toda la entrada de System.in y detecta automaticamente que ejercicio es
 * mirando la primera linea no vacia:
 *   - Empieza con "event:" o "data:"  -> Ejercicio A (Streaming SSE)
 *   - Empieza con "{"                 -> puede ser B, C o E (mira el contenido)
 *   - Empieza con "REQUEST"           -> Ejercicio D (Rate limits)
 *
 * Para distinguir B, C y E entre ellos:
 *   - Tiene "stop_reason"             -> Ejercicio B (Tool use)
 *   - Tiene "input_tokens"            -> Ejercicio C (Costos con cache)
 *   - Tiene "categoria" o "esperado"  -> Ejercicio E (Evaluaciones)
 *
 * Uso:
 *   java Main < entrada.txt
 */
public class Main {

    public static void main(String[] args) throws IOException {
        EjercicioA.configurarSalida();
        List<String> lineas = EjercicioA.leerLineas();

        // Buscar la primera linea no vacia para detectar el ejercicio
        String primera = "";
        for (String l : lineas) {
            String t = l.trim();
            if (!t.isEmpty()) { primera = t; break; }
        }

        char ejercicio = detectar(primera, lineas);

        switch (ejercicio) {
            case 'A': EjercicioA.resolver(lineas); break;
            case 'B': EjercicioB.resolver(lineas); break;
            case 'C': EjercicioC.resolver(lineas); break;
            case 'D': EjercicioD.resolver(lineas); break;
            case 'E': EjercicioE.resolver(lineas); break;
            default:
                System.err.println("No se pudo detectar el tipo de ejercicio.");
                System.err.println("Usa directamente: java EjercicioA/B/C/D/E < entrada.txt");
        }
    }

    private static char detectar(String primera, List<String> lineas) {
        if (primera.startsWith("event:") || primera.startsWith("data:")) {
            return 'A';
        }
        if (primera.startsWith("REQUEST")) {
            return 'D';
        }
        if (primera.startsWith("{")) {
            // Revisar el contenido de las primeras lineas para distinguir B, C, E
            String muestra = unirPrimeras(lineas, 5);
            if (muestra.contains("\"stop_reason\""))                        return 'B';
            if (muestra.contains("\"input_tokens\""))                       return 'C';
            if (muestra.contains("\"categoria\"") || muestra.contains("\"esperado\"")) return 'E';
        }
        return '?';
    }

    /** Une las primeras n lineas en una sola cadena para facilitar la busqueda. */
    private static String unirPrimeras(List<String> lineas, int n) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String l : lineas) {
            if (l.trim().isEmpty()) continue;
            sb.append(l);
            if (++count >= n) break;
        }
        return sb.toString();
    }
}
