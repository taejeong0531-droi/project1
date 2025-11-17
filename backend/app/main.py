import os
import datetime
from typing import Dict, Any, List, Optional

from fastapi import FastAPI, Depends, HTTPException, Header
from fastapi.middleware.cors import CORSMiddleware

from .models import RecommendRequest, RecommendResponse, FoodScore
from .auth import verify_firebase_token
from .weather import fetch_weather_by_latlon
from . import firestore as fs
from .recommender import (
    recommend_final,
    get_category,
)

app = FastAPI(title="Emotion Food Recommender API", version="1.0.0")

# CORS (필요에 따라 도메인 제한)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


async def get_uid(authorization: Optional[str] = Header(None)) -> Optional[str]:
    """Authorization: Bearer <idToken> 에서 Firebase UID 추출."""
    if not authorization or not authorization.lower().startswith("bearer "):
        return None
    token = authorization.split(" ", 1)[1].strip()
    uid = verify_firebase_token(token)
    return uid


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/recommend", response_model=RecommendResponse)
async def recommend(body: RecommendRequest, uid: Optional[str] = Depends(get_uid)):
    # 인증 필수: uid가 없으면 401
    if not uid:
        raise HTTPException(status_code=401, detail="Unauthorized")

    # user_id 일치성 체크(선택)
    if body.user_id and body.user_id != uid:
        raise HTTPException(status_code=403, detail="Forbidden: user mismatch")

    # 1) Firestore에서 데이터 가져오기(있으면 사용)
    emotion_food_map_fs = fs.load_emotion_food_map()  # 추천 후보 커스텀화 가능(현재 recommender 내부 기본도 존재)
    user_profile_fs = fs.load_user_profile(uid)
    user_prefs_fs = fs.load_user_preferences(uid)
    user_logs_fs = fs.load_user_recent_logs(uid)

    # 2) 요청 본문 데이터와 병합
    # preferences 병합
    likes: List[str] = []
    dislikes: List[str] = []
    sensitive_spicy = False
    if user_prefs_fs:
        likes.extend(user_prefs_fs.get("likes", []) or [])
        dislikes.extend(user_prefs_fs.get("dislikes", []) or [])
        sensitive_spicy = bool(user_prefs_fs.get("sensitive_spicy", False))
    if body.preferences:
        if body.preferences.likes:
            likes.extend(body.preferences.likes)
        if body.preferences.dislikes:
            dislikes.extend(body.preferences.dislikes)
        if body.preferences.sensitive_spicy is not None:
            sensitive_spicy = body.preferences.sensitive_spicy

    # recent_logs 병합(요청 우선)
    logs: List[Dict[str, Any]] = []
    # Firestore logs
    for l in user_logs_fs:
        t = l.get('time')
        # firestore Timestamp는 그대로 사용 가능(파이썬 datetime 유사)
        item = {
            'food': l.get('food'),
            'time': t,
            'category': l.get('category') or get_category(l.get('food'))
        }
        logs.append(item)
    # Request logs
    if body.recent_logs:
        for l in body.recent_logs:
            try:
                dt = datetime.datetime.fromisoformat(l.timestamp.replace('Z', '+00:00'))
            except Exception:
                dt = datetime.datetime.utcnow()
            item = {
                'food': l.food,
                'time': dt,
                'category': get_category(l.food)
            }
            logs.append(item)

    user_profile: Dict[str, Any] = {
        'uid': uid,
        'likes': list(dict.fromkeys(likes)),
        'dislikes': list(dict.fromkeys(dislikes)),
        'sensitive_spicy': sensitive_spicy,
        'recent_logs': logs,
    }

    # 3) 날씨 구성
    weather: Dict[str, Any] = {}
    if body.weather and (body.weather.temp_c is not None or body.weather.status):
        weather = {'temp_c': body.weather.temp_c, 'status': body.weather.status}
    elif body.weather and body.weather.lat is not None and body.weather.lon is not None:
        # 서버에서 OpenWeatherMap 호출
        weather = fetch_weather_by_latlon(body.weather.lat, body.weather.lon)
    else:
        # 기본값
        weather = {'temp_c': 20.0, 'status': 'mild'}

    # 4) 추천 실행
    # 간단화를 위해 recommender의 내부 emotion_food_map을 사용하되,
    # Firestore에서 map이 있으면 향후 recommender를 확장하여 주입 가능.
    result = recommend_final(user_profile, body.text, provided_emotion=None, weather=weather, topn=3)

    # 5) 응답 포맷
    return RecommendResponse(
        emotion=result['emotion'],
        score=None,
        top3=[FoodScore(**it) for it in result['top3']]
    )


# Uvicorn로 직접 실행 시
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=int(os.getenv("PORT", 8080)))
