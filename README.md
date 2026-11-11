# Prueba Anthropic · Hackatón Individual — API de Claude

Solución en **Java estándar** (sin librerías externas) para los 5 ejercicios de la prueba técnica.  
Cada ejercicio lee su entrada por `stdin` y escribe la respuesta por `stdout`.

---

## Estructura del proyecto

```
Prueba-Anthropic-/
├── Main.java          # Punto de entrada: detecta automáticamente el ejercicio y lo delega
├── EjercicioA.java    # A · Respuesta en streaming (SSE)
├── EjercicioB.java    # B · Uso de herramientas (Tool use)
├── EjercicioC.java    # C · Costos con caché de prompts
├── EjercicioD.java    # D · Límites de velocidad (Rate limits)
├── EjercicioE.java    # E · Evaluación de respuestas
├── entrada.txt        # Aquí pegas tu entrada oficial
└── README.md
```

---

## Requisitos

- **Java 11 o superior** instalado y en el PATH  
  Verifica con: `java -version`

---

## Paso a paso para ejecutar

### 1 · Obtener tu entrada oficial

1. Abre el archivo **`Prueba Anthropic · La API de Claude · Hackatón individual.html`** en tu navegador.  
2. Escribe tu **nombre** y el **código de la prueba** que dio el jurado.  
3. Selecciona el ejercicio (A, B, C, D o E) en el menú lateral.  
4. Haz clic en **Copiar** dentro del recuadro naranja "Tu entrada oficial".

---

### 2 · Guardar la entrada

Abre `entrada.txt`, **borra todo** lo que tenga, pega tu entrada oficial y guarda (`Ctrl+S`).  
Asegúrate de guardarlo en codificación **UTF-8** (VS Code lo hace por defecto).

---

### 3 · Compilar

Abre una terminal (**PowerShell** o **CMD**) en esta carpeta y ejecuta:

```powershell
javac -encoding UTF-8 *.java
```

Deberías ver silencio (sin errores). Solo hay que compilar una vez.

---

### 4 · Ejecutar

#### Opción A — Con `Main` (detecta el ejercicio automáticamente)

**PowerShell:**
```powershell
Get-Content entrada.txt -Encoding UTF8 | java "-Dstdout.encoding=UTF-8" Main
```

**CMD / bash:**
```cmd
java "-Dstdout.encoding=UTF-8" Main < entrada.txt
```

---

#### Opción B — Ejecutar un ejercicio específico

Sustituye `EjercicioA` por el que necesites (A, B, C, D o E):

**PowerShell:**
```powershell
Get-Content entrada.txt -Encoding UTF8 | java "-Dstdout.encoding=UTF-8" EjercicioA
```

**CMD / bash:**
```cmd
java "-Dstdout.encoding=UTF-8" EjercicioA < entrada.txt
```

| Ejercicio | Clase          |
|-----------|----------------|
| A — Streaming    | `EjercicioA` |
| B — Herramientas | `EjercicioB` |
| C — Costos caché | `EjercicioC` |
| D — Rate limits  | `EjercicioD` |
| E — Evaluaciones | `EjercicioE` |

---

### 5 · Guardar la salida para el jurado

**PowerShell:**
```powershell
Get-Content entrada.txt -Encoding UTF8 | java "-Dstdout.encoding=UTF-8" Main | Out-File salida.txt -Encoding UTF8
```

**CMD:**
```cmd
java "-Dstdout.encoding=UTF-8" Main < entrada.txt > salida.txt
```

---

## Qué hace cada ejercicio

### A · Respuesta en streaming
Lee eventos `event:` / `data:` (formato SSE). Reconstruye cada mensaje completo acumulando los `content_block_delta` por índice. Al llegar `message_stop` imprime 4 líneas:
```
texto: <texto completo>
stop_reason: <razón>
input_tokens: N
output_tokens: N
```
Los eventos `ping` se ignoran.

---

### B · Uso de herramientas
Cada línea es un JSON de respuesta del modelo.  
- `stop_reason: "tool_use"` → ejecutar las herramientas `calculadora` y `clima` e imprimir `tool_result <id> <resultado>`.  
- `stop_reason: "end_turn"` → imprimir `final: <texto>`.  
Al terminar: `resumen: N llamadas, E errores`.

Herramientas soportadas:
- **`calculadora`** — sumar, restar, multiplicar, dividir (entera). División por cero → `error: división por cero`.
- **`clima`** — Bogotá 14 °C, Medellín 24 °C, Cali 26 °C. Ciudad desconocida → `error: ciudad desconocida: X`.

---

### C · Costos con caché de prompts
Calcula el costo de cada llamada usando precios escalados:

| Tipo de token       | Precio / millón |
|---------------------|-----------------|
| Entrada normal      | \$3.00          |
| Cache creation      | \$3.75 (×1.25)  |
| Cache read          | \$0.30 (×0.10)  |
| Salida              | \$15.00         |

Imprime `solicitud i: $X.XXXXXX` por línea.  
Al final: `total`, `sin_cache`, `ahorro` y `aciertos_de_cache: k/n`.  
> Los cálculos se hacen con enteros para evitar errores de redondeo con `double`.

---

### D · Límites de velocidad
Simula dos **cubetas de tokens** (token bucket) que se recargan continuamente:

| Cubeta       | Capacidad | Recarga   |
|--------------|-----------|-----------|
| Solicitudes  | 3         | 1 / seg   |
| Tokens entr. | 100       | 20 / seg  |

Formato de entrada: `REQUEST t nuevos de_cache`  
- Los tokens de caché **no** consumen cupo de la cubeta de tokens.  
- Si hay cupo → descontar y responder `ok`.  
- Si no hay cupo → **no descontar nada** y responder `429 retry-after=N` (techo de los segundos que faltan).

---

### E · Evaluación de respuestas
Una respuesta **pasa** si, al normalizar ambas cadenas (minúsculas, sin tildes, no-alfanumérico → espacio), el valor esperado aparece como **palabras completas** dentro de la respuesta.  
Ejemplo: `"42"` **no** pasa con `"420"` porque no es una palabra completa.

Imprime por categoría en orden alfabético: `categoria: aprobadas/total`  
Al final: `total: a/n (p%)` con un decimal.

---

## Decisiones de diseño (estilo junior)

- **Sin librerías externas**: todo usa el JDK estándar (`BufferedReader`, `TreeMap`, `Normalizer`, `Math`).
- **Sin parser JSON completo**: se usa un mini-parser manual que busca claves/valores por posición de caracteres. Es suficiente para el formato plano que usa la API de Claude.
- **Costos con enteros**: el Ejercicio C trabaja en "centi-microdólares" para evitar errores de punto flotante.
- **UTF-8 explícito**: tanto la lectura de stdin como la escritura en stdout se fuerzan a UTF-8 para que las tildes y el símbolo °C lleguen bien en Windows.
- **Reutilización**: `EjercicioA` contiene las funciones de lectura y parsing; los demás ejercicios las llaman directamente.
