import sys, json

lineas = sys.stdin.read().splitlines()

input_tokens = 0
output_tokens = 0
stop_reason = ""
bloques = {}
event_type = None

for linea in lineas:
    linea = linea.strip()
    if not linea:
        continue
    if linea.startswith("event: "):
        event_type = linea[7:]
        continue
    if linea.startswith("data: "):
        data = json.loads(linea[6:])
        t = data.get("type", "")

        if t == "message_start":
            input_tokens = data["message"]["usage"]["input_tokens"]
            bloques = {}
        elif t == "content_block_delta":
            idx = data["index"]
            delta = data.get("delta", {})
            if delta.get("type") == "text_delta":
                bloques[idx] = bloques.get(idx, "") + delta["text"]
        elif t == "message_delta":
            stop_reason = data["delta"]["stop_reason"]
            output_tokens = data["usage"]["output_tokens"]
        elif t == "message_stop":
            texto = "".join(bloques[i] for i in sorted(bloques))
            print(f"texto: {texto}")
            print(f"stop_reason: {stop_reason}")
            print(f"input_tokens: {input_tokens}")
            print(f"output_tokens: {output_tokens}")

