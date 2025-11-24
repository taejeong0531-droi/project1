import os
from typing import List
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

import requests


def search_spoonacular_foods(queries: List[str], number: int = 10) -> List[str]:
    # Allow disabling external calls entirely
    if os.getenv("USE_EXTERNAL_APIS", "false").lower() not in ("1", "true", "yes"): 
        return []
    api_key = os.getenv("SPOONACULAR_API_KEY")
    if not api_key:
        return []
    titles: List[str] = []
    seen = set()
    # Strict overall time budget
    budget_sec = float(os.getenv("EXTERNAL_API_BUDGET_SECONDS", "2.5"))
    deadline = time.time() + budget_sec
    max_workers = max(1, min(int(os.getenv("EXTERNAL_MAX_WORKERS", "4")), 8))

    def _fetch(q: str) -> List[str]:
        if time.time() >= deadline:
            return []
        try:
            resp = requests.get(
                "https://api.spoonacular.com/recipes/complexSearch",
                params={"query": q, "number": number, "apiKey": api_key},
                timeout=min(1.5, max(0.5, deadline - time.time())),
            )
            if resp is None or resp.status_code != 200:
                return []
            data = resp.json() or {}
            return [str((it or {}).get("title") or "").strip() for it in data.get("results", [])]
        except Exception:
            return []

    # Run queries in parallel within budget
    with ThreadPoolExecutor(max_workers=max_workers) as ex:
        futures = {ex.submit(_fetch, q): q for q in queries}
        while futures and time.time() < deadline and len(titles) < number:
            timeout_left = max(0.1, deadline - time.time())
            done = []
            try:
                for f in as_completed(list(futures.keys()), timeout=timeout_left):
                    done.append(f)
            except Exception:
                # timeout waiting; break to check deadline/loop
                pass
            for f in done:
                q = futures.pop(f, None)
                try:
                    results = f.result() or []
                except Exception:
                    results = []
                for title in results:
                    if title and title not in seen:
                        seen.add(title)
                        titles.append(title)
                        if len(titles) >= number:
                            break
                if len(titles) >= number or time.time() >= deadline:
                    break
    return titles[:number]


def translate_titles_via_papago(titles: List[str]) -> List[str]:
    if os.getenv("USE_EXTERNAL_APIS", "false").lower() not in ("1", "true", "yes"):
        return titles
    cid = os.getenv("PAPAGO_CLIENT_ID")
    csecret = os.getenv("PAPAGO_CLIENT_SECRET")
    if not cid or not csecret:
        return titles
    out: List[str] = []
    budget_sec = float(os.getenv("EXTERNAL_API_BUDGET_SECONDS", "2.5"))
    deadline = time.time() + budget_sec
    max_workers = max(1, min(int(os.getenv("EXTERNAL_MAX_WORKERS", "4")), 8))

    def _translate(text: str) -> str:
        if time.time() >= deadline:
            return text
        try:
            resp = requests.post(
                "https://openapi.naver.com/v1/papago/n2mt",
                data={"source": "en", "target": "ko", "text": text},
                headers={
                    "X-Naver-Client-Id": cid,
                    "X-Naver-Client-Secret": csecret,
                },
                timeout=min(1.2, max(0.5, deadline - time.time())),
            )
            if resp.status_code == 200:
                data = resp.json() or {}
                tr = (
                    data.get("message", {})
                    .get("result", {})
                    .get("translatedText")
                )
                return tr or text
            return text
        except Exception:
            return text

    # Run translations in parallel but preserve order
    with ThreadPoolExecutor(max_workers=max_workers) as ex:
        future_map = {ex.submit(_translate, t): idx for idx, t in enumerate(titles)}
        results_map = {}
        while future_map and time.time() < deadline:
            timeout_left = max(0.1, deadline - time.time())
            done = []
            try:
                for f in as_completed(list(future_map.keys()), timeout=timeout_left):
                    done.append(f)
            except Exception:
                pass
            for f in done:
                idx = future_map.pop(f, None)
                try:
                    results_map[idx] = f.result()
                except Exception:
                    results_map[idx] = titles[idx]
            if time.time() >= deadline:
                break
        # Any not-done futures fallback to original
        for f, idx in list(future_map.items()):
            results_map[idx] = titles[idx]
        out = [results_map.get(i, titles[i]) for i in range(len(titles))]
    return out
