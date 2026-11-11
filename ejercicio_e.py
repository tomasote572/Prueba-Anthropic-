import sys, json, unicodedata, re

lineas = sys.stdin.read().splitlines()

def normalizar(s):
    """Minúsculas, sin tildes, todo lo que no sea letra o número se vuelve espacio."""
    s = unicodedata.normalize('NFD', s)
    s = ''.join(c for c in s if unicodedata.category(c) != 'Mn')
    s = s.lower()
    s = re.sub(r'[^a-z0-9]+', ' ', s)
    s = s.strip()
    return s

categorias = {}

for linea in lineas:
    linea = linea.strip()
    if not linea:
        continue
    obj = json.loads(linea)
    cat = obj["categoria"]
    esperado = normalizar(obj["esperado"])
    respuesta = normalizar(obj["respuesta"])

    # Verificar si esperado aparece como palabras completas en respuesta
    # Buscamos " esperado " dentro de " respuesta "
    pasa = (' ' + esperado + ' ') in (' ' + respuesta + ' ')

    if cat not in categorias:
        categorias[cat] = [0, 0]
    categorias[cat][1] += 1
    if pasa:
        categorias[cat][0] += 1

total_ok = 0
total_n = 0

for cat in sorted(categorias.keys()):
    ok, n = categorias[cat]
    print(f"{cat}: {ok}/{n}")
    total_ok += ok
    total_n += n

pct = (total_ok / total_n * 100) if total_n > 0 else 0
print(f"total: {total_ok}/{total_n} ({pct:.1f}%)")

