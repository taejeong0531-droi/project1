from typing import List, Optional, Dict
import os
import requests

# Simple rule-based recommender for fallback.
BASE_ITEMS: List[Dict] = [
    {"name": "비빔밥", "kcal": 540, "tags": ["korean", "balanced"]},
    {"name": "김치찌개", "kcal": 330, "tags": ["korean", "spicy"]},
    {"name": "샐러드", "kcal": 220, "tags": ["light", "veg"]},
    {"name": "라멘", "kcal": 600, "tags": ["japanese", "noodle"]},
    {"name": "치킨", "kcal": 820, "tags": ["fried", "protein"]},
    {"name": "연어 샐러드", "kcal": 350, "tags": ["light", "omega3"]},
    {"name": "된장찌개", "kcal": 290, "tags": ["korean", "mild"]},
]

# Map common mood keywords to a keyword for external API
EMOTION_TO_KEYWORD = {
    "happy": "dessert",
    "sad": "comfort food",
    "angry": "spicy food",
    "tired": "coffee",
    "stressed": "chocolate",
    "neutral": "healthy"
}

MOOD_TO_TAGS = {
    "행복": ["sweet", "light"],
    "기쁨": ["sweet", "light"],
    "좋음": ["light"],
    "화남": ["spicy"],
    "분노": ["spicy"],
    "스트레스": ["spicy"],
    "가벼움": ["light"],
    "무난": ["mild"],
    "중립": ["mild"],
    "neutral": ["mild"],
    "happy": ["sweet", "light"],
    "angry": ["spicy"],
}


def _fallback_by_tags(prefs: List[str], top_k: int) -> List[Dict]:
    items = BASE_ITEMS
    if prefs:
        filtered = [i for i in items if any(p in i.get("tags", []) for p in prefs)]
        if filtered:
            items = filtered
    return items[: max(1, top_k)]


def _spoonacular(keyword: str, top_k: int, api_key: str) -> List[Dict]:
    url = "https://api.spoonacular.com/recipes/complexSearch"
    params = {"query": keyword, "number": max(1, top_k), "apiKey": api_key}
    r = requests.get(url, params=params, timeout=10)
    r.raise_for_status()
    data = r.json() or {}
    out: List[Dict] = []
    for item in data.get("results", []):
        name = item.get("title")
        if not name:
            continue
        out.append({
            "name": name,
            "kcal": None,  # API에서 칼로리 미제공. 필요시 추가 API 호출.
            "tags": [keyword]
        })
    return out[: max(1, top_k)]


def recommend(mood: Optional[str], preferences: Optional[List[str]], top_k: int) -> List[Dict]:
    mood_norm = (mood or "").strip().lower()
    prefs = list(preferences or [])

    # Try external API if key exists
    api_key = os.getenv("SPOONACULAR_API_KEY")
    keyword = EMOTION_TO_KEYWORD.get(mood_norm, None)
    if api_key and keyword:
        try:
            results = _spoonacular(keyword, top_k, api_key)
            if results:
                return results
        except Exception:
            # fall back on any error
            pass

    # Fallback: rule-based using tags
    # derive tags from mood and merge with preferences
    mood_tags: List[str] = []
    for k, tags in MOOD_TO_TAGS.items():
        if k.lower() in mood_norm:
            mood_tags.extend(tags)
    merged_prefs = list(set(prefs + mood_tags))
    return _fallback_by_tags(merged_prefs, top_k)
