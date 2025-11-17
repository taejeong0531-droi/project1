import datetime
from typing import List, Dict, Any, Tuple

# ------------------------------
# 1) 감정 분석 (데모용 규칙 기반)
# ------------------------------

def simple_emotion_classifier(text: str) -> Dict[str, Any]:
    t = text.lower()
    if any(k in t for k in ['스트레스', 'stress', '짜증', '빡침']):
        return {'emotion': 'stress', 'score': 0.9}
    if any(k in t for k in ['슬프', 'sad', '우울', '눈물']):
        return {'emotion': 'sad', 'score': 0.85}
    if any(k in t for k in ['피곤', 'tired', '졸려', '지침']):
        return {'emotion': 'tired', 'score': 0.8}
    if any(k in t for k in ['외로', 'lonely', '쓸쓸', '허전']):
        return {'emotion': 'lonely', 'score': 0.8}
    return {'emotion': 'neutral', 'score': 0.6}


# ------------------------------
# 2) 감정 -> 음식 매핑 및 후보 선정
# ------------------------------

emotion_food_map: Dict[str, List[str]] = {
    'stress': ['짬뽕', '삼겹살', '떡볶이', '마라탕', '불고기', '카레'],
    'sad': ['미역국', '닭죽', '칼국수', '된장찌개', '솥밥'],
    'tired': ['닭가슴살 샐러드', '연어덮밥', '두부구이', '콩나물국밥', '비빔밥'],
    'lonely': ['치킨', '파스타', '피자', '햄버거', '크림리조또'],
    'neutral': ['김치찌개', '비빔국수', '순두부찌개', '샌드위치']
}


def get_candidates_by_emotion(emotion: str, score: float, top_k: int = 5) -> List[str]:
    pool = emotion_food_map.get(emotion, emotion_food_map['neutral'])
    if score >= 0.8:
        return pool[:top_k]
    return pool


# ------------------------------
# 3) 음식 카테고리 (다양성/날씨 가중치용)
# ------------------------------

food_category: Dict[str, str] = {
    # 면/국물
    '짬뽕': 'spicy_soup', '칼국수': 'noodle_soup', '콩나물국밥': 'soup', '된장찌개': 'soup', '미역국': 'soup',
    # 고기/단백질
    '삼겹살': 'meat', '불고기': 'meat', '닭가슴살 샐러드': 'salad', '두부구이': 'protein',
    # 분식/자극
    '떡볶이': 'k_snack', '마라탕': 'spicy_soup', '김치찌개': 'soup',
    # 한식/덮밥/밥류
    '솥밥': 'rice', '비빔밥': 'rice', '연어덮밥': 'rice', '카레': 'rice',
    # 양식/패스트푸드
    '파스타': 'western', '피자': 'western', '햄버거': 'western', '크림리조또': 'western', '샌드위치': 'western',
    # 기타
    '순두부찌개': 'soup', '비빔국수': 'noodle'
}


def get_category(food: str) -> str:
    return food_category.get(food, 'other')


# ------------------------------
# 4) 날씨 적합도
# ------------------------------

cold_favor = {'soup', 'spicy_soup', 'noodle_soup'}
hot_favor = {'salad', 'noodle', 'western'}
rain_snow_favor = {'soup', 'noodle_soup', 'spicy_soup'}


def weather_suitability(food: str, weather: Dict[str, Any]) -> float:
    cat = get_category(food)
    temp = weather.get('temp_c', 20)
    status = weather.get('status', 'mild')
    s = 0.0
    if temp is not None:
        if temp <= 5 and cat in cold_favor:
            s += 0.06
        if temp >= 28 and cat in hot_favor:
            s += 0.06
    if status in ('rain', 'snow') and cat in rain_snow_favor:
        s += 0.04
    return min(max(s, -0.1), 0.1)


# ------------------------------
# 5) 반복 섭취 패널티 (최근성/빈도/다양성)
# ------------------------------

def time_diff_hours(a: datetime.datetime, b: datetime.datetime) -> float:
    return abs((a - b).total_seconds()) / 3600.0


def time_decay(hours_diff: float, half_life: float = 24.0) -> float:
    return 0.5 ** (hours_diff / half_life)


def recent_repetition_penalty(food: str, logs: List[Dict[str, Any]], now: datetime.datetime, window_hours: float = 72.0) -> float:
    penalty = 0.0
    for log in logs:
        h = time_diff_hours(now, log['time'])
        if h <= window_hours and log['food'] == food:
            penalty += 0.2 * time_decay(h)
    return -min(penalty, 0.2)


def frequency_penalty(food: str, logs: List[Dict[str, Any]], now: datetime.datetime, window_hours: float = 168.0) -> float:
    cnt = 0
    for log in logs:
        h = time_diff_hours(now, log['time'])
        if h <= window_hours and log['food'] == food:
            cnt += 1
    return -min(0.05 * cnt, 0.1)


def category_diversity_adjust(food: str, logs: List[Dict[str, Any]], now: datetime.datetime, window_hours: float = 72.0) -> float:
    cat = get_category(food)
    recent = [log for log in logs if time_diff_hours(now, log['time']) <= window_hours]
    recent_sorted = sorted(recent, key=lambda x: x['time'], reverse=True)[:5]
    same = sum(1 for l in recent_sorted if l.get('category') == cat)
    if same >= 2:
        return -0.05
    if same == 0:
        return 0.02
    return 0.0


# ------------------------------
# 6) 개인화 점수 & 추천 함수
# ------------------------------

morning_foods = {'샌드위치', '닭가슴살 샐러드', '죽', '미역국'}
heavy_foods = {'삼겹살', '치킨', '피자'}
spicy_foods = {'짬뽕', '떡볶이', '마라탕', '김치찌개'}


def personalized_score(food: str, user_profile: Dict[str, Any], emotion: str, emotion_score: float, weather: Dict[str, Any], now: datetime.datetime) -> float:
    score = 0.0

    # 1) 감정 점수
    if food in emotion_food_map.get(emotion, []):
        score += 0.3

    # 2) 선호도
    if food in user_profile.get('likes', []):
        score += 0.3
    if food in user_profile.get('dislikes', []):
        score -= 0.3

    # 3) 시간대
    hour = now.hour
    if hour < 11 and food in morning_foods:
        score += 0.1
    if hour > 20 and food in heavy_foods:
        score -= 0.1

    # 4) 건강/체질
    if user_profile.get('sensitive_spicy') and food in spicy_foods:
        score -= 0.1

    # 5) 최근 섭취(고도화)
    logs = user_profile.get('recent_logs', [])
    score += recent_repetition_penalty(food, logs, now)  # 최대 -0.2
    score += frequency_penalty(food, logs, now)          # 최대 -0.1
    score += category_diversity_adjust(food, logs, now)  # -0.05 ~ +0.02

    # 6) 날씨
    score += weather_suitability(food, weather)

    return round(float(score), 4)


def recommend_final(user_profile: Dict[str, Any], text: str, provided_emotion: Dict[str, Any] | None, weather: Dict[str, Any], topn: int = 3) -> Dict[str, Any]:
    # 감정 분류: 입력으로 감정이 오면 사용, 없으면 텍스트에서 추론
    emo = provided_emotion or simple_emotion_classifier(text)
    emotion, emo_score = emo['emotion'], emo['score']

    now = datetime.datetime.now()
    candidates = get_candidates_by_emotion(emotion, emo_score, top_k=5)
    scored: List[Tuple[str, float]] = []
    for food in candidates:
        s = personalized_score(food, user_profile, emotion, emo_score, weather, now)
        scored.append((food, s))
    scored.sort(key=lambda x: x[1], reverse=True)

    return {
        'emotion': emotion,
        'top3': [{'food': f, 'score': s} for f, s in scored[:topn]]
    }
