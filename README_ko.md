# 감정 기반 + 개인화 하이브리드 음식 추천 시스템 (Colab 데모)

이 프로젝트는 다음 파이프라인으로 동작합니다.

1) 감정 분석 → 2) 감정→음식 1차 매핑 → 3) 개인화 점수화 → 4) 최종 추천

- 실제 서비스 구조에 맞게 모듈화되어 있으며, 간단한 규칙 기반부터 시작해 모델/데이터 소스로 확장할 수 있습니다.
- Colab에서 바로 실행 가능한 노트북 `emotion_food_recommender.ipynb` 가 포함되어 있습니다.

## 파일 구조
- `emotion_food_recommender.ipynb` — 데모용 Colab 노트북 (권장 실행 환경: Google Colab)
- `README_ko.md` — 한국어 설명서
- `requirements.txt` — (선택) 로컬 실행 시 참고할 수 있는 패키지 목록

## 빠른 시작 (Colab)
1. Google Colab을 열고, `emotion_food_recommender.ipynb` 파일을 업로드합니다.
2. 상단 메뉴에서 런타임 → 런타임 다시 시작을 눌러 초기화합니다(선택).
3. 순서대로 셀을 실행합니다.
   - 이 데모는 추가 설치 없이 동작합니다.
   - 한국어 감정 모델/Firestore 연동 등을 사용하려면 노트북 상단의 설치 셀을 참고하세요.

## 아키텍처 개요
- 감정 분석 모델
  - 입력: 텍스트(일기/대화/메모), STT(음성→텍스트), (선택) 얼굴 표정 분석 결과
  - 모델 후보: KoBERT, KLUE RoBERTa, KoELECTRA, 임베딩+분류기 등
  - 출력 예: `{ "emotion": "stress", "score": 0.93 }`
- 감정→음식 1차 매핑
  - 감정별 회복 음식 데이터셋(e.g., stress→매콤/단백/풍미) 구성
  - 데모에선 파이썬 딕셔너리, 실제 서비스에선 Firestore/Supabase 권장
- 개인화 레이어
  - 입력: 선호도/비선호, 최근 섭취, 시간대, 건강/체질, (선택) 날씨 등
  - 결과: 각 음식 후보에 대한 개인화 점수 계산
- 최종 추천
  - 점수 기준 정렬 후 상위 N개 반환

## 점수 공식(데모)
최종 점수 =
- 감정 매칭 점수(0.3)
- 사용자 선호도(0.3)
- 시간대 적합도(0.1)
- 건강/체질 적합도(0.1)
- 최근 섭취 패널티(0.2)

각 요소는 0~1 범위로 가감되며, 세부 규칙/가중치는 비즈니스 요구에 맞게 조정 가능합니다.

## Firestore 스키마 예시(권장)
- `emotion_food_map/{emotion}` → `foods: string[]`
  - 예) `/emotion_food_map/stress → ["짬뽕", "삼겹살", "떡볶이"]`
- `users/{uid}/profile` → 알레르기/건강 정보, 민감도 등
- `users/{uid}/preferences` → likes[], dislikes[]
- `users/{uid}/recent_food_logs` → 최근 섭취 내역(타임스탬프 포함)

## 확장 아이디어
- STT 연동: Google Cloud Speech-to-Text 또는 Whisper API → 감정 분석 입력으로 사용
- 한국어 감정 분류 고도화: KLUE/KoBERT 파인튜닝 or 임베딩+로지스틱 회귀
- 날씨/위치 반영: OpenWeatherMap API → 추울 때 국물류, 더울 때 냉면/샐러드 가중치
- 반복 방지 고도화: 최근 24~72시간, 1주일 단위 패널티, 다양성(serendipity) 제약 추가
- 모델 v2: LightGBM 회귀로 직접 score 예측(피처: 감정, 시간, 날씨, 유저 선호/최근 로그 등)
- "기분따라밥 GPT 1.0": 감정 분석 + 추천 + 스토리텔링을 LLM으로 통합, 웹/모바일 챗봇 구현

## 주의 사항
- 데모의 `simple_emotion_classifier`는 규칙 기반입니다. 실제 서비스에서는 머신러닝 모델로 교체하세요.
- 매운맛 민감 등 건강/체질 규칙은 비즈니스/의학 자문 하에 구체화하는 것이 좋습니다.
- 로컬 환경에서 `requirements.txt` 설치 시 PyTorch 버전은 GPU/OS에 맞게 조정 필요합니다.
