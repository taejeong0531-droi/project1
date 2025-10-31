from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional

app = FastAPI(title="Food Recommender API", version="0.1.0")

# Allow Android emulator/device access during development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # tighten for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class RecommendRequest(BaseModel):
    mood: Optional[str] = None
    preferences: Optional[List[str]] = None  # e.g., ["spicy", "no_pork"]
    top_k: int = 5


class FoodItem(BaseModel):
    name: str
    kcal: Optional[int] = None
    tags: List[str] = []


class RecommendResponse(BaseModel):
    items: List[FoodItem]


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/recommend", response_model=RecommendResponse)
def recommend(req: RecommendRequest):
    """
    TODO: Replace this stub with logic from `mainfunction/finction.ipynb`.
    For now, returns simple mock items based on mood/preferences so Android can integrate.
    """
    base_items = [
        FoodItem(name="비빔밥", kcal=540, tags=["korean", "balanced"]),
        FoodItem(name="김치찌개", kcal=330, tags=["korean", "spicy"]),
        FoodItem(name="샐러드", kcal=220, tags=["light", "veg"]),
        FoodItem(name="라멘", kcal=600, tags=["japanese", "noodle"]),
        FoodItem(name="치킨", kcal=820, tags=["fried", "protein"]),
    ]

    items = base_items
    if req.mood:
        if "매움" in req.mood or "화난" in req.mood or "spicy" in str(req.preferences or []):
            items = [i for i in base_items if "spicy" in i.tags] or base_items
        if "가벼운" in req.mood:
            items = [i for i in base_items if "light" in i.tags] or base_items

    if req.preferences:
        # simple filter if any tag matches preference keyword
        prefs = set([p.lower() for p in req.preferences])
        filtered = [i for i in items if any(p in i.tags for p in prefs)]
        if filtered:
            items = filtered

    items = items[: max(1, req.top_k)]
    return RecommendResponse(items=items)


# Run: uvicorn server.main:app --reload --host 0.0.0.0 --port 8000
