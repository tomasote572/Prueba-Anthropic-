# Prueba Anthropic · Hackatón Individual — API de Claude

Solución en **Python 3** para los 5 ejercicios de la prueba técnica.  
Cada ejercicio lee su entrada por `stdin` y escribe la respuesta por `stdout`.

---

## Estructura del proyecto

```
Prueba-Anthropic-/
├── ejercicio_a.py     # A · Respuesta en streaming (SSE)
├── ejercicio_b.py     # B · Uso de herramientas (Tool use)
├── ejercicio_c.py     # C · Costos con caché de prompts
├── ejercicio_d.py     # D · Límites de velocidad (Rate limits)
├── ejercicio_e.py     # E · Evaluación de respuestas
├── entrada.txt        # Aquí pegas tu entrada oficial
└── README.md
```

---

## Requisitos

- **Python 3.10 o superior** instalado  
  Verifica con: `python --version`

---

## 🚀 Paso a paso para ejecutar (guía rápida)

> **Resumen:** abrir HTML → poner nombre y código → copiar entrada → pegar en `entrada.txt` → correr el `.py` → copiar la salida al jurado. ¡Repetir por cada ejercicio!

### Paso 1 · Abrir la página de la prueba

1. Ve a la carpeta del proyecto y haz **doble clic** en el archivo:  
   `Prueba Anthropic · La API de Claude · Hackatón individual.html`  
   Se abrirá en tu navegador (Chrome, Edge, Firefox, etc.).

---

### Paso 2 · Generar tu entrada oficial

1. En la página, baja hasta la sección **"Tu entrada oficial"**.  
2. Escribe tu **nombre** exactamente como te inscribió el jurado.  
3. Escribe el **código de la prueba** que está en el tablero.  
4. Verás que cambia a **"Entradas de [tu nombre] listas"** en verde. ✅

---

### Paso 3 · Elegir un ejercicio

1. En el menú lateral izquierdo haz clic en el ejercicio que quieras resolver:  
   - **A** — Respuesta en streaming  
   - **B** — Uso de herramientas  
   - **C** — Costos con caché  
   - **D** — Límites de velocidad  
   - **E** — Evaluación de respuestas  
2. Abajo del enunciado aparece un recuadro **naranja** con tu entrada personalizada.  
3. Haz clic en **"Copiar"** para copiar esa entrada al portapapeles.

---

### Paso 4 · Pegar la entrada en `entrada.txt`

1. Abre el archivo **`entrada.txt`** con VS Code, Notepad o cualquier editor.  
2. **Borra todo** lo que tenga dentro.  
3. **Pega** (`Ctrl+V`) la entrada que copiaste.  
4. **Guarda** el archivo (`Ctrl+S`).  
   > ⚠️ Asegúrate de que el archivo se guarde en codificación **UTF-8** (VS Code lo hace por defecto).

---

### Paso 5 · Abrir la terminal

1. Abre **VS Code** y abre la carpeta del proyecto (`Archivo > Abrir carpeta...`).  
2. Abre la terminal integrada con `` Ctrl+` `` (o menú `Terminal > Nueva terminal`).  
3. Verifica que estés en la carpeta correcta. Deberías ver algo como:  
   ```
   PS C:\Users\Isabel\Documents\Prueba-Anthropic->
   ```

---

### Paso 6 · Correr el ejercicio

Escribe el comando según el ejercicio que quieras resolver:

**Ejercicio A — Streaming:**
```powershell
Get-Content entrada.txt -Raw -Encoding UTF8 | python -X utf8 ejercicio_a.py
```

**Ejercicio B — Herramientas:**
```powershell
Get-Content entrada.txt -Raw -Encoding UTF8 | python -X utf8 ejercicio_b.py
```

**Ejercicio C — Costos caché:**
```powershell
Get-Content entrada.txt -Raw -Encoding UTF8 | python -X utf8 ejercicio_c.py
```

**Ejercicio D — Rate limits:**
```powershell
Get-Content entrada.txt -Raw -Encoding UTF8 | python -X utf8 ejercicio_d.py
```

**Ejercicio E — Evaluaciones:**
```powershell
Get-Content entrada.txt -Raw -Encoding UTF8 | python -X utf8 ejercicio_e.py
```

> 💡 Si `python` no te funciona, prueba con la ruta completa:  
> `C:\Users\Isabel\AppData\Local\Programs\Python\Python312\python.exe`

La salida aparecerá directamente en la terminal. **Cópiala y entrégasela al jurado.**

---

### Paso 7 · (Opcional) Guardar la salida en un archivo

Si prefieres guardar la salida en un archivo para entregarlo:

**PowerShell:**
```powershell
Get-Content entrada.txt -Raw -Encoding UTF8 | python -X utf8 ejercicio_a.py | Out-File salida.txt -Encoding UTF8
```

**CMD:**
```cmd
python -X utf8 ejercicio_a.py < entrada.txt > salida.txt
```

---

### Paso 8 · Repetir para cada ejercicio

1. Vuelve al navegador.  
2. Selecciona el **siguiente ejercicio** (B, C, D o E).  
3. Copia su entrada naranja.  
4. Pégala en `entrada.txt` (borrando lo anterior).  
5. Corre el `.py` correspondiente.  
6. Entrega la salida al jurado.  

> 🏆 **Tip:** No tienes que resolverlos en orden. Empieza por el que te parezca más fácil.

---

### Resumen de comandos rápidos

| Ejercicio            | Comando PowerShell |
|----------------------|--------------------|
| A — Streaming        | `Get-Content entrada.txt -Raw -Encoding UTF8 \| python -X utf8 ejercicio_a.py` |
| B — Herramientas     | `Get-Content entrada.txt -Raw -Encoding UTF8 \| python -X utf8 ejercicio_b.py` |
| C — Costos caché     | `Get-Content entrada.txt -Raw -Encoding UTF8 \| python -X utf8 ejercicio_c.py` |
| D — Rate limits      | `Get-Content entrada.txt -Raw -Encoding UTF8 \| python -X utf8 ejercicio_d.py` |
| E — Evaluaciones     | `Get-Content entrada.txt -Raw -Encoding UTF8 \| python -X utf8 ejercicio_e.py` |

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
- **`calculadora`** — sumar, restar, multiplicar, dividir (entera truncada hacia cero). División por cero → `error: división por cero`.
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
> Los cálculos se hacen con enteros para evitar errores de redondeo con flotantes.

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

## Decisiones de diseño

- **Sin librerías externas**: solo usa `json`, `sys`, `math`, `re` y `unicodedata` de la librería estándar.
- **Costos con enteros**: el Ejercicio C trabaja en centésimas de microdólar para evitar errores de punto flotante.
- **UTF-8 explícito**: se usa `-X utf8` para que las tildes y el símbolo °C se manejen correctamente en Windows.
