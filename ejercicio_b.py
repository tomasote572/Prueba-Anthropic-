import sys, json

lineas = sys.stdin.read().splitlines()

CIUDADES = {"Bogotá": 14, "Medellín": 24, "Cali": 26}
llamadas = 0
errores = 0

def div_trunc(a, b):
    """División entera truncada hacia cero (como Math.trunc en JS)."""
    return int(a / b)

for linea in lineas:
    linea = linea.strip()
    if not linea:
        continue
    obj = json.loads(linea)
    sr = obj["stop_reason"]
    content = obj["content"]

    if sr == "end_turn":
        for bloque in content:
            if bloque["type"] == "text":
                print(f"final: {bloque['text']}")
    elif sr == "tool_use":
        for bloque in content:
            if bloque["type"] != "tool_use":
                continue
            tid = bloque["id"]
            name = bloque["name"]
            inp = bloque["input"]
            llamadas += 1

            if name == "calculadora":
                op = inp["operacion"]
                a = inp["a"]
                b = inp["b"]
                if op == "sumar":
                    res = str(a + b)
                elif op == "restar":
                    res = str(a - b)
                elif op == "multiplicar":
                    res = str(a * b)
                elif op == "dividir":
                    if b == 0:
                        res = "error: división por cero"
                    else:
                        res = str(div_trunc(a, b))
                else:
                    res = f"error: operación desconocida: {op}"
            elif name == "clima":
                ciudad = inp["ciudad"]
                if ciudad in CIUDADES:
                    res = f"{CIUDADES[ciudad]} °C"
                else:
                    res = f"error: ciudad desconocida: {ciudad}"
            else:
                res = f"error: herramienta desconocida: {name}"

            if res.startswith("error:"):
                errores += 1
            print(f"tool_result {tid} {res}")

print(f"resumen: {llamadas} llamadas, {errores} errores")
