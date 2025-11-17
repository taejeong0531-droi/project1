import os
from typing import List
import time

import requests


def search_spoonacular_foods(queries: List[str], number: int = 10) -> List[str]:
    api_key = os.getenv("SPOONACULAR_API_KEY")
    if not api_key:
        return []
    titles: List[str] = []
    seen = set()
    for q in queries:
        try:
            resp = None
            # simple retries (reduced)
            for attempt in range(2):
                try:
                    resp = requests.get(
                        "https://api.spoonacular.com/recipes/complexSearch",
                        params={"query": q, "number": number, "apiKey": api_key},
                        timeout=6,
                    )
                    if resp.status_code == 200:
                        break
                except Exception:
                    resp = None
                time.sleep(0.2 * (attempt + 1))
            if resp is None:
                continue
            if resp.status_code != 200:
                continue
            data = resp.json() or {}
            results = data.get("results", [])
            for item in results:
                title = str(item.get("title") or "").strip()
                if title and title not in seen:
                    seen.add(title)
                    titles.append(title)
        except Exception:
            continue
        if len(titles) >= number:
            break
    return titles[:number]


def translate_titles_via_papago(titles: List[str]) -> List[str]:
    cid = os.getenv("PAPAGO_CLIENT_ID")
    csecret = os.getenv("PAPAGO_CLIENT_SECRET")
    if not cid or not csecret:
        return titles
    out: List[str] = []
    for t in titles:
        try:
            resp = None
            for attempt in range(2):
                try:
                    resp = requests.post(
                        "https://openapi.naver.com/v1/papago/n2mt",
                        data={"source": "en", "target": "ko", "text": t},
                        headers={
                            "X-Naver-Client-Id": cid,
                            "X-Naver-Client-Secret": csecret,
                        },
                        timeout=5,
                    )
                    if resp.status_code == 200:
                        break
                except Exception:
                    resp = None
                time.sleep(0.2 * (attempt + 1))
            if resp is None:
                out.append(t)
                continue
            if resp.status_code == 200:
                data = resp.json() or {}
                tr = (
                    data.get("message", {})
                    .get("result", {})
                    .get("translatedText")
                )
                out.append(tr or t)
            else:
                out.append(t)
        except Exception:
            out.append(t)
    return out
