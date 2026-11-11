import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Ejercicio E: Evaluacion de respuestas (LLM evals)
 * Cada linea es un JSON con: id, categoria, esperado, respuesta
 * Una respuesta PASA si, al normalizar ambas cadenas
 * (minusculas, sin tildes/diacriticos, todo lo que no sea letra/numero ->
 * espacio),
 * el valor "esperado" aparece como PALABRAS COMPLETAS dentro de "respuesta".
 * Salida:
 * <categoria>: aprobadas/total (en orden alfabetico)
 * total: a/n (p%) (porcentaje con 1 decimal)
 */
public class EjercicioE {

    public static void main(String[] args) throws IOException {
        EjercicioA.configurarSalida();
        List<String> lineas = EjercicioA.leerLineas();
        resolver(lineas);
    }

    public static void resolver(List<String> lineas) {
        // categoria -> [aprobadas, total]
        TreeMap<String, int[]> categorias = new TreeMap<>();
        int totalAprobadas = 0;
        int totalCasos = 0;
        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty())
                continue;
            String categoria = EjercicioA.extraerString(linea, "\"categoria\"");
            String esperado = EjercicioA.extraerString(linea, "\"esperado\"");
            String respuesta = EjercicioA.extraerString(linea, "\"respuesta\"");
            boolean pasa = contienePalabraCompleta(normalizar(respuesta), normalizar(esperado));
            categorias.computeIfAbsent(categoria, k -> new int[] { 0, 0 });
            categorias.get(categoria)[1]++;
            if (pasa) {
                categorias.get(categoria)[0]++;
                totalAprobadas++;
            }
            totalCasos++;
        }
        // Imprimir por categoria en orden alfabetico
        for (String cat : categorias.keySet()) {
            int[] v = categorias.get(cat);
            System.out.println(cat + ": " + v[0] + "/" + v[1]);
        }

        // Total con un decimal de porcentaje (punto decimal, no coma)
        double pct = totalCasos == 0 ? 0.0 : (100.0 * totalAprobadas / totalCasos);
        System.out.printf(java.util.Locale.US, "total: %d/%d (%.1f%%)%n", totalAprobadas, totalCasos, pct);
    }

    /**
     * Normaliza una cadena:
     * 1. Descompone caracteres Unicode (NFD) para separar diacriticos
     * 2. Elimina todos los diacriticos (marcas de acento, etc.)
     * 3. Convierte a minusculas
     * 4. Reemplaza todo lo que no sea letra o digito por espacio
     * 5. Colapsa multiples espacios en uno
     * 6. Elimina espacios al inicio y al final
     */
    static String normalizar(String s) {
        // NFD: descomponer letras con tilde en letra + diacritico
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        // Eliminar marcas diacriticas (rango Unicode 0300-036F)
        String sinTildes = nfd.replaceAll("[\\u0300-\\u036f]", "");
        // Minusculas
        String minus = sinTildes.toLowerCase();
        // Reemplazar no-alfanumericos por espacio
        String limpio = minus.replaceAll("[^a-z0-9]+", " ");
        return limpio.trim();
    }

    /**
     * Comprueba si "esperado" aparece como palabras completas dentro de "texto".
     * Ambas cadenas ya deben estar normalizadas.
     * Tecnica: buscar " esperado " en " texto " (con espacios al borde).
     */
    static boolean contienePalabraCompleta(String texto, String esperado) {
        if (esperado.isEmpty())
            return true;
        return (" " + texto + " ").contains(" " + esperado + " ");
    }
}
