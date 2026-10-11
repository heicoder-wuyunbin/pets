import json, urllib.request

TOKEN = "88f732c5d522dd4c9a3189c84d1356218279de19"
BASE = "http://192.168.100.128:30000"
REPO = "/api/v1/repos/pets/pets/actions/runs"

def req(method, path, data=None):
    url = f"{BASE}{path}" + (f"?token={TOKEN}" if "?" not in path else f"&token={TOKEN}")
    body = json.dumps(data).encode() if data is not None else None
    r = urllib.request.Request(url, data=body, method=method,
                               headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(r, timeout=20) as resp:
            return resp.status, resp.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()

# list runs
status, txt = req("GET", f"{REPO}?limit=50&page=1")
runs = json.loads(txt).get("data", [])
print(f"total runs returned: {len(runs)}")
canceled = []
for run in runs:
    st = run.get("status")
    rid = run.get("index") or run.get("id")
    if st in ("running", "waiting", "blocked"):
        s, _ = req("POST", f"{REPO}/{rid}/cancel")
        print(f"  run #{rid} status={st} -> cancel http {s}")
        canceled.append(rid)
    else:
        print(f"  run #{rid} status={st} (skip)")
print("canceled:", canceled)
