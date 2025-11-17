from typing import List, Optional
from pydantic import BaseModel, Field


class WeatherReq(BaseModel):
    temp_c: Optional[float] = Field(None, description="섭씨 온도")
    status: Optional[str] = Field(None, description="hot|warm|mild|cold|rain|snow|clear 등")
    lat: Optional[float] = Field(None, description="위도(서버에서 날씨 조회시 사용)")
    lon: Optional[float] = Field(None, description="경도(서버에서 날씨 조회시 사용)")


class PreferencesReq(BaseModel):
    likes: Optional[List[str]] = None
    dislikes: Optional[List[str]] = None
    sensitive_spicy: Optional[bool] = None


class RecentLogReq(BaseModel):
    food: str
    timestamp: str  # ISO8601


class RecommendRequest(BaseModel):
    user_id: str
    text: str
    weather: Optional[WeatherReq] = None
    recent_logs: Optional[List[RecentLogReq]] = None
    preferences: Optional[PreferencesReq] = None


class FoodScore(BaseModel):
    food: str
    score: float


class RecommendResponse(BaseModel):
    emotion: str
    score: Optional[float] = None
    top3: List[FoodScore]
