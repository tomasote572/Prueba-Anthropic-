import java.io.IOException;
import java.util.List;

/**
 * Ejercicio D: Limites de velocidad (Rate limits - API de Claude)
 *
 * Simula dos "token buckets" que se recargan continuamente:
 *
 *   Cubeta de solicitudes: capacidad 3,   recarga 1  token/segundo
 *   Cubeta de tokens:      capacidad 100, recarga 20 tokens/segundo
 *   Ambas empiezan llenas.
 *
 * Formato de entrada: REQUEST t nuevos de_cache
 *   t       -> tiempo en segundos (entero)
 *   nuevos  -> tokens de entrada nuevos que consume la solicitud
 *   de_cache -> tokens leidos de cache (NO cuentan para el limite de tokens)
 *
 * Si hay suficiente en ambas cubetas -> descontar y responder "ok"
 * Si no -> NO descontar nada y responder "429 retry-after=N"
 *          donde N es el techo de los segundos que faltan para que alcance.
 */
public class EjercicioD {

    public static void main(String[] args) throws IOException {
        EjercicioA.configurarSalida();
        List<String> lineas = EjercicioA.leerLineas();
        resolver(lineas);
    }

    public static void resolver(List<String> lineas) {
        // Estado de las dos cubetas
        // Usamos doubles porque la recarga puede ser fraccionaria entre eventos
        double capSol  = 3.0,  tasaSol = 1.0,  tokSol  = 3.0;
        double capTok  = 100.0, tasaTok = 20.0, tokTok  = 100.0;
        long tiempoAnterior = 0; // tiempo del ultimo evento procesado

        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty() || !linea.startsWith("REQUEST")) continue;

            // Parsear "REQUEST t nuevos de_cache"
            String[] partes = linea.split("\\s+");
            if (partes.length < 4) continue;

            long t      = Long.parseLong(partes[1]);
            long nuevos = Long.parseLong(partes[2]);
            // partes[3] es de_cache, no lo necesitamos para el calculo

            // 1. Recargar ambas cubetas segun el tiempo transcurrido
            long delta = t - tiempoAnterior;
            tokSol = Math.min(capSol, tokSol + tasaSol * delta);
            tokTok = Math.min(capTok, tokTok + tasaTok * delta);
            tiempoAnterior = t;

            // 2. Verificar si alcanza (1 solicitud + nuevos tokens)
            boolean hayCapacidad = (tokSol >= 1.0) && (tokTok >= nuevos);

            if (hayCapacidad) {
                tokSol -= 1.0;
                tokTok -= nuevos;
                System.out.println("ok");
            } else {
                // Calcular cuanto tiempo falta para cada cubeta
                double esperaSol = 0.0;
                double esperaTok = 0.0;

                if (tokSol < 1.0) {
                    esperaSol = (1.0 - tokSol) / tasaSol;
                }
                if (tokTok < nuevos) {
                    esperaTok = (nuevos - tokTok) / tasaTok;
                }

                // N = techo del maximo de las dos esperas
                long n = (long) Math.ceil(Math.max(esperaSol, esperaTok));
                System.out.println("429 retry-after=" + n);
                // No descontamos nada
            }
        }
    }
}
