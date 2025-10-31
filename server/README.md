# Food Recommender FastAPI Server

This is a lightweight API wrapper around your notebook logic (to be integrated) so the Android app can call `/recommend`.

## Endpoints
- `GET /health` → `{ "status": "ok" }`
- `POST /recommend` → `{ items: [{ name, kcal?, tags[] }] }`

Request body example:
```json
{
  "mood": "가벼운",
  "preferences": ["light"],
  "top_k": 5
}
```

## Quick start (Windows PowerShell)
```powershell
# From project root
Set-Location "C:\Users\comso-1407\project1"

# Create venv (optional but recommended)
python -m venv .venv
. .venv\Scripts\Activate.ps1

# Install deps
pip install -r server\requirements.txt

# Run server (host visible to Android emulator)
uvicorn server.main:app --reload --host 0.0.0.0 --port 8000
```

- Android emulator base URL is `http://10.0.2.2:8000/` (already set in `ApiClient.kt`).
- For a physical device on the same Wi‑Fi, set `ApiClient.BASE_URL` to your PC's LAN IP, e.g. `http://192.168.0.10:8000/`.

## Integrate notebook logic
Place your existing logic from `mainfunction/finction.ipynb` into `server/main.py` (inside `/recommend`). Recommended options:
- Export core logic to a Python module (e.g., `server/logic.py`) and import it in `main.py`.
- If you have a trained model, load it once at startup (global) and reuse in the `/recommend` handler.

## Notes
- CORS is enabled for development; tighten for production.
- Consider adding input validation and error handling when real logic is added.
