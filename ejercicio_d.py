import sys, math

lineas = sys.stdin.read().splitlines()

# Cubeta de solicitudes: capacidad 3, recarga 1/s, empieza llena
# Cubeta de tokens de entrada: capacidad 100, recarga 20/s, empieza llena
# Los tokens de cache NO cuentan para el limite de entrada

req_cap = 3
req_rate = 1  # por segundo
tok_cap = 100
tok_rate = 20  # por segundo

req_tok = 3.0  # tokens actuales en cubeta de requests
tok_tok = 100.0  # tokens actuales en cubeta de tokens
last_t = 0

for linea in lineas:
    linea = linea.strip()
    if not linea:
        continue
    partes = linea.split()
    # REQUEST t nuevos de_cache
    t = int(partes[1])
    nuevos = int(partes[2])
    de_cache = int(partes[3])

    # Recargar cubetas desde last_t hasta t
    elapsed = t - last_t
    req_tok = min(req_cap, req_tok + req_rate * elapsed)
    tok_tok = min(tok_cap, tok_tok + tok_rate * elapsed)
    last_t = t

    # Verificar si alcanza
    need_req = 1
    need_tok = nuevos

    if req_tok >= need_req and tok_tok >= need_tok:
        req_tok -= need_req
        tok_tok -= need_tok
        print("ok")
    else:
        # Calcular cuántos segundos esperar
        wait = 0
        if req_tok < need_req:
            wait = max(wait, math.ceil((need_req - req_tok) / req_rate))
        if tok_tok < need_tok:
            wait = max(wait, math.ceil((need_tok - tok_tok) / tok_rate))
        print(f"429 retry-after={wait}")

