from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
from .logic import recommend as logic_recommend
from dotenv import load_dotenv

# Load environment variables from server/.env if present
load_dotenv()

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
    # Delegate to logic module (replaceable with real model logic)
    results = logic_recommend(req.mood, req.preferences, req.top_k)
    items = [FoodItem(name=r.get("name"), kcal=r.get("kcal"), tags=r.get("tags", [])) for r in results]
    return RecommendResponse(items=items)


# Run: uvicorn server.main:app --reload --host 0.0.0.0 --port 8000
