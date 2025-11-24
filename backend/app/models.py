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


class EmotionVector(BaseModel):
    joy: int
    energy: int
    social: int
    calm: int
    focus: int


class RecommendRequest(BaseModel):
    user_id: str
    text: Optional[str] = None
    weather: Optional[WeatherReq] = None
    recent_logs: Optional[List[RecentLogReq]] = None
    preferences: Optional[PreferencesReq] = None
    # 추가: 클라이언트 측 감정 분석 결과 전달용(선택)
    emotion_label: Optional[str] = None  # e.g., "happy" | "angry" | "neutral" 등
    emotion_vector: Optional[EmotionVector] = None
    score_intensity: Optional[float] = None  # 0.0~1.0


class FoodScore(BaseModel):
    food: str
    score: float


class RecommendResponse(BaseModel):
    emotion: str
    score: Optional[float] = None
    items: List[FoodScore]
