# Emotion Food Recommender 백엔드 (옵션 A)

FastAPI + Firestore + Firebase Auth + OpenWeatherMap 기반 추천 API 서버입니다.
Android 앱은 이 서버의 `/recommend` 엔드포인트를 호출해 최종 추천을 받습니다.

## 디렉터리
- `backend/app/main.py` — FastAPI 엔트리, `/recommend` 엔드포인트
- `backend/app/models.py` — 요청/응답 Pydantic 모델
- `backend/app/auth.py` — Firebase ID 토큰 검증
- `backend/app/weather.py` — OpenWeatherMap 날씨 조회
- `backend/app/firestore.py` — Firestore 읽기(감정-음식 맵, 유저 프로필/선호/최근로그)
- `backend/app/recommender.py` — 추천 로직(감정/개인화/날씨/반복)
- `backend/requirements.txt` — 서버 의존성
- `backend/Dockerfile` — 컨테이너 배포용
- `backend/.env.example` — 환경변수 예시

## 요구 사항
- Python 3.11+
- Google Cloud 프로젝트 + Firestore(Database) 활성화
- Firebase 프로젝트(같은 GCP 혹은 연동) + Firebase Auth(Email/OAuth 등)
- OpenWeatherMap API 키(선택: 서버에서 날씨 조회 시)

## 환경 변수 설정
`.env`를 만들고 다음 값을 설정하세요(또는 컨테이너/배포 설정에 주입).

```env
# Firebase Admin 인증(둘 중 택1)
# GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
# FIREBASE_CREDENTIALS_JSON={"type":"service_account", ...}

# OpenWeatherMap(선택, 서버에서 날씨 조회 시 필요)
OPENWEATHER_API_KEY=your_openweather_api_key

PORT=8080
```

## 로컬 실행(개발용)
```bash
cd backend
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\\Scripts\\activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8080 --reload
```

헬스 체크:
```bash
curl http://localhost:8080/health
```

## Docker 실행
```bash
cd backend
docker build -t emotion-food-api:latest .
docker run --rm -p 8080:8080 --env-file .env emotion-food-api:latest
```

## Cloud Run 배포(예시)
```bash
# gcloud 로그인/프로젝트/리전 설정 후
cd backend
gcloud builds submit --tag gcr.io/PROJECT_ID/emotion-food-api

gcloud run deploy emotion-food-api \
  --image gcr.io/PROJECT_ID/emotion-food-api \
  --platform managed \
  --region asia-northeast3 \
  --allow-unauthenticated=false \
  --set-env-vars OPENWEATHER_API_KEY=your_key \
  --update-secrets FIREBASE_CREDENTIALS_JSON=firebase-sa:latest
```
- 보안: Cloud Run을 비공개로 두고, Firebase 인증 프록시나 Cloud Endpoints/Identity-Aware Proxy 연동 가능
- 간단히 하려면 앱에서 Firebase ID 토큰을 발급받아 Authorization 헤더로 전송하고, 서버가 검증

## Firestore 스키마 권장
- `emotion_food_map/{emotion}` → `{ foods: string[] }`
  - 예) `/emotion_food_map/stress` → `{ foods: ["짬뽕","삼겹살","떡볶이"] }`
- `users/{uid}/profile/main` → `{ sensitive_spicy: bool, health_flags: {...} }`
- `users/{uid}/preferences/main` → `{ likes: string[], dislikes: string[], sensitive_spicy: bool }`
- `users/{uid}/recent_food_logs/{autoId}` → `{ food: string, timestamp: Timestamp, category: string }`

> 현재 `app/recommender.py`는 내부 기본 `emotion_food_map`도 가지고 있습니다.
> Firestore의 값을 우선 반영하려면 해당 모듈로 주입하는 구조로 확장하세요.

## API 사용법
- URL: `POST /recommend`
- 인증: `Authorization: Bearer <Firebase ID Token>`
- 요청(JSON):
```json
{
  "user_id": "<Firebase UID>",
  "text": "오늘 스트레스가 너무 쌓였어…",
  "weather": {
    "temp_c": 3.0,
    "status": "cold",
    "lat": 37.57,
    "lon": 126.98
  },
  "recent_logs": [
    {"food": "비빔밥", "timestamp": "2025-11-16T18:30:00Z"}
  ],
  "preferences": {
    "likes": ["삼겹살", "파스타"],
    "dislikes": ["마라탕"],
    "sensitive_spicy": true
  }
}
```
- 응답(JSON):
```json
{
  "emotion": "stress",
  "score": null,
  "top3": [
    {"food": "삼겹살", "score": 0.82},
    {"food": "짬뽕", "score": 0.79},
    {"food": "떡볶이", "score": 0.73}
  ]
}
```

## 안드로이드 연동 팁
- Retrofit Base URL을 배포 주소(예: Cloud Run URL)로 설정
- Firebase Auth로 로그인 후 `getIdToken()`으로 ID 토큰을 받아 `Authorization: Bearer <token>` 헤더에 추가
- 위치/날씨
  - 권장: 앱은 위도/경도만 서버로 전달, 서버가 OpenWeatherMap 호출
  - 수동: 앱이 직접 날씨를 얻어 `temp_c`/`status`로 전달(키 노출 주의)
- 최근 로그/선호도는 Firestore 동기화 또는 요청 본문으로 함께 전송

## CORS
현재 `app/main.py`는 `allow_origins=["*"]`로 설정(개발 편의). 운영에서는 도메인을 제한하세요.

## 로깅/모니터링 제안
- 추천 점수 구성요소(감정/선호/시간대/건강/반복/날씨) 로그를 구조화하여 A/B 실험 및 튜닝에 활용
- 오류/성능 지표를 Cloud Logging + Error Reporting으로 집계

## 라이선스
내부 프로젝트/PoC 용도로 시작하며, 서비스 출시에 맞춰 라이선스와 제3자 모델/데이터의 약관을 검토하세요.
