import sys, json

lineas = sys.stdin.read().splitlines()

# Precios por millon de tokens (en microcentavos / millonésimas de dólar)
# input: $3/M  -> 3_000_000 microdolares por millon = 3 microdolares por token
# output: $15/M -> 15 microdolares por token
# cache write: 1.25x input = 3.75 microdolares por token
# cache read: 0.1x input = 0.3 microdolares por token

total = 0
sin_cache_total = 0
hits = 0
n = 0

for linea in lineas:
    linea = linea.strip()
    if not linea:
        continue
    n += 1
    u = json.loads(linea)
    inp = u["input_tokens"]
    cw = u["cache_creation_input_tokens"]
    cr = u["cache_read_input_tokens"]
    out = u["output_tokens"]

    # Costo con cache (en microdolares, *100 para evitar decimales)
    # input: 3 * inp, cache_write: 3.75 * cw, cache_read: 0.3 * cr, output: 15 * out
    # Multiplicamos todo por 100 para trabajar con enteros:
    # costo_x100 = 300*inp + 375*cw + 30*cr + 1500*out
    costo = inp * 300 + cw * 375 + cr * 30 + out * 1500  # en centésimas de microdólar? no...

    # Mejor: trabajemos en millonésimas de dólar directamente
    # $3 por millón de tokens de entrada = 3/1_000_000 dólares por token = 3 microdólares por token
    # Pero para tener 6 decimales necesitamos unidades de 10^-6 dólares = microdólares
    # input: 3 microdólares/token => inp * 3
    # cache_write: 3*1.25 = 3.75 microdólares/token => cw * 3.75 (no entero!)
    # cache_read: 3*0.1 = 0.3 microdólares/token => cr * 0.3 (no entero!)
    # output: 15 microdólares/token => out * 15

    # Para evitar decimales: trabajemos en centésimas de microdólar (10^-8 dólares)
    # input: 300 por token, cache_write: 375 por token, cache_read: 30 por token, output: 1500 por token
    costo_100 = inp * 300 + cw * 375 + cr * 30 + out * 1500

    # sin_cache: todo como entrada normal ($3/M) + salida ($15/M)
    sin_cache_100 = (inp + cw + cr) * 300 + out * 1500

    # El costo en microdólares = costo_100 / 100
    total += costo_100
    sin_cache_total += sin_cache_100

    if cr > 0:
        hits += 1

    # Formatear: costo_100 / 100 = microdólares, / 1_000_000 = dólares
    # $X.YYYYYY donde YYYYYY son 6 decimales
    # costo_100 son centésimas de microdólar
    # dólares = costo_100 / 100_000_000
    dolares = costo_100 // 100  # microdólares
    d_int = dolares // 1_000_000
    d_frac = dolares % 1_000_000
    print(f"solicitud {n}: ${d_int}.{d_frac:06d}")

d_total = total // 100
d_sc = sin_cache_total // 100
d_ahorro = (sin_cache_total - total) // 100

print(f"total: ${d_total // 1_000_000}.{d_total % 1_000_000:06d}")
print(f"sin_cache: ${d_sc // 1_000_000}.{d_sc % 1_000_000:06d}")
print(f"ahorro: ${d_ahorro // 1_000_000}.{d_ahorro % 1_000_000:06d}")
print(f"aciertos_de_cache: {hits}/{n}")

