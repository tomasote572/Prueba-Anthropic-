import java.io.IOException;
import java.util.List;

/**
 * Ejercicio C: Costos con cache de prompts (API de Claude)
 *
 * Precios (por millon de tokens):
 *   Entrada normal       -> $3    = 300 micro-centavos / token
 *   Cache creation       -> $3.75 = 375 micro-centavos / token  (1.25x entrada)
 *   Cache read           -> $0.30 = 30  micro-centavos / token  (0.1x entrada)
 *   Salida               -> $15   = 1500 micro-centavos / token
 *
 * Trabajamos en micro-centavos de dolar (millones de dollarcitos) para evitar
 * errores de redondeo con doubles.
 *
 * Por cada linea imprime: solicitud i: $X.XXXXXX
 * Al final:
 *   total: $X.XXXXXX
 *   sin_cache: $X.XXXXXX
 *   ahorro: $X.XXXXXX
 *   aciertos_de_cache: k/n
 */
public class EjercicioC {

    // Precios en micro-dollares por token (precio_por_millon / 1_000_000 * 1_000_000 = precio_por_millon)
    // Usamos: micro$ = tokens * precio_por_millon / 1_000_000
    // Para evitar division, trabajamos en "nano$" = tokens * precio_por_millon
    // y al final dividimos entre 1_000_000 para mostrar dolares.
    // Mas simple: guardamos todo en "micro$" = tokens * (precio_USD_por_millon / 1_000_000) * 1_000_000
    //           = tokens * precio_por_millon   <-- en unidades de 1e-12 USD * 1e6 = 1e-6 USD
    // Precio entrada normal:  $3 / 1e6 tokens = 3e-6 USD/token
    // En micro$: 3  (por token)   => costo_micro = tokens * 3
    // Precio cache_creation: $3.75 / 1e6 tokens => costo_micro = tokens * 3.75  => *375/100
    // Precio cache_read:     $0.30 / 1e6 tokens => costo_micro = tokens * 0.30  => *30/100
    // Precio salida:         $15   / 1e6 tokens => costo_micro = tokens * 15
    //
    // Para no usar floats: multiplicamos todo por 100 y trabajamos en "centi-micro$"
    //   entrada:        tokens * 300
    //   cache_creation: tokens * 375
    //   cache_read:     tokens * 30
    //   salida:         tokens * 1500
    // Al final dividimos entre 100 para obtener micro$ y formateamos como $entero.6decimales

    public static void main(String[] args) throws IOException {
        EjercicioA.configurarSalida();
        List<String> lineas = EjercicioA.leerLineas();
        resolver(lineas);
    }

    public static void resolver(List<String> lineas) {
        long totalCosto    = 0; // en centi-micro$
        long totalSinCache = 0; // en centi-micro$
        int  aciertosCache = 0;
        int  numSolicitud  = 0;

        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty()) continue;

            long input    = EjercicioA.extraerLong(linea, "\"input_tokens\"");
            long creation = EjercicioA.extraerLong(linea, "\"cache_creation_input_tokens\"");
            long read     = EjercicioA.extraerLong(linea, "\"cache_read_input_tokens\"");
            long output   = EjercicioA.extraerLong(linea, "\"output_tokens\"");

            // Costo real con cache (en centi-micro$)
            long costo = input * 300L
                       + creation * 375L
                       + read * 30L
                       + output * 1500L;

            // Costo sin cache: todo cobrado como entrada normal
            long sinCache = (input + creation + read) * 300L
                          + output * 1500L;

            totalCosto    += costo;
            totalSinCache += sinCache;
            if (read > 0) aciertosCache++;
            numSolicitud++;

            System.out.println("solicitud " + numSolicitud + ": " + formatear(costo));
        }

        System.out.println("total: "            + formatear(totalCosto));
        System.out.println("sin_cache: "         + formatear(totalSinCache));
        System.out.println("ahorro: "            + formatear(totalSinCache - totalCosto));
        System.out.println("aciertos_de_cache: " + aciertosCache + "/" + numSolicitud);
    }

    /**
     * Convierte centi-micro$ a la cadena "$X.XXXXXX".
     * centi-micro$ / 100 = micro$
     * micro$ / 1_000_000 = dolares enteros
     * micro$ % 1_000_000 = parte decimal (6 digitos)
     */
    static String formatear(long centiMicro) {
        // Dividimos entre 100 para obtener micro$
        long microDolares = centiMicro / 100L;
        long entero  = microDolares / 1_000_000L;
        long decimal = microDolares % 1_000_000L;
        // Manejar negativos (para ahorro nunca deberia ser negativo, pero por si acaso)
        if (decimal < 0) decimal = -decimal;
        return String.format("$%d.%06d", entero, decimal);
    }
}
