import datetime
from typing import List, Dict, Any, Tuple

try:
    from .external import search_spoonacular_foods, translate_titles_via_papago  # optional external API
except Exception:  # pragma: no cover
    def search_spoonacular_foods(queries: List[str], number: int = 10) -> List[str]:
        return []
    def translate_titles_via_papago(titles: List[str]) -> List[str]:
        return titles

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


def _emotion_queries(emotion: str) -> List[str]:
    e = emotion.lower()
    if e in ("stress", "angry"):
        return ["spicy soup", "ramen", "kimchi stew", "spicy chicken", "tteokbokki"]
    if e in ("sad",):
        return ["comfort soup", "porridge", "noodle soup", "tofu stew", "warm rice bowl"]
    if e in ("tired",):
        return ["salad bowl", "salmon rice bowl", "tofu", "bibimbap", "protein salad"]
    if e in ("lonely",):
        return ["fried chicken", "pasta", "pizza", "hamburger", "risotto"]
    return ["kimchi stew", "bibim noodles", "soft tofu stew", "sandwich"]


def get_candidates_by_emotion(emotion: str, score: float, top_k: int = 5) -> List[str]:
    # 1) Try external API
    queries = _emotion_queries(emotion)
    ext = search_spoonacular_foods(queries, number=top_k)
    if ext:
        try:
            ext = translate_titles_via_papago(ext)
        except Exception:
            pass
        import random
        random.shuffle(ext)
        return ext[:top_k]
    # 2) Fallback to internal pool
    pool = emotion_food_map.get(emotion, emotion_food_map['neutral'])
    import random
    pool = pool.copy()
    random.shuffle(pool)
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


def personalized_score(
    food: str,
    user_profile: Dict[str, Any],
    emotion: str,
    emotion_score: float,
    weather: Dict[str, Any],
    now: datetime.datetime,
    emotion_vector: Dict[str, int] | None = None,
    intensity: float | None = None,
) -> float:
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

    # 6) 감정 벡터/강도 반영(선택)
    if emotion_vector:
        try:
            joy = int(emotion_vector.get('joy', 0))
            energy = int(emotion_vector.get('energy', 0))
            social = int(emotion_vector.get('social', 0))
            calm = int(emotion_vector.get('calm', 0))
            focus = int(emotion_vector.get('focus', 0))
            # 저에너지/저평정(피로/스트레스)일수록 자극/무거운 음식 감점, 따뜻한 국물 가점
            if energy <= -1 and get_category(food) in {'spicy_soup', 'western'}:
                score -= 0.05
            if calm <= -1 and food in spicy_foods:
                score -= 0.05
            if energy <= -1 or calm <= -1:
                if get_category(food) in {'soup', 'noodle_soup', 'spicy_soup'}:
                    score += 0.04
            # 기쁨이 높으면 가벼운/양식 소폭 가산
            if joy >= 2 and get_category(food) in {'western', 'noodle'}:
                score += 0.03
            # 집중 낮으면 아주 무거운 음식 감점
            if focus <= -1 and food in heavy_foods:
                score -= 0.05
            # 외로움 높으면 따뜻/국물 선호 소폭 가산
            if social <= -1 and get_category(food) in {'soup', 'noodle_soup'}:
                score += 0.03
        except Exception:
            pass

    # 강도(0~1) 반영: 감정 매칭 가중 소폭 조정
    if intensity is not None:
        try:
            k = max(0.0, min(1.0, float(intensity)))
            score += 0.05 * (k - 0.5)  # -0.025 ~ +0.025
        except Exception:
            pass

    # 7) 날씨
    score += weather_suitability(food, weather)

    # 소량 난수 가산으로 동점 해소(다양성 확보)
    try:
        import random
        score += random.uniform(-0.01, 0.01)
    except Exception:
        pass

    return round(float(score), 4)


def recommend_final(
    user_profile: Dict[str, Any],
    text: str,
    provided_emotion: Dict[str, Any] | None,
    weather: Dict[str, Any],
    topn: int = 3,
    emotion_vector: Dict[str, int] | None = None,
    intensity: float | None = None,
) -> Dict[str, Any]:
    # 감정 분류: 입력으로 감정이 오면 사용, 없으면 텍스트에서 추론
    emo = provided_emotion or simple_emotion_classifier(text)
    emotion, emo_score = emo['emotion'], emo['score']

    now = datetime.datetime.now()
    candidates = get_candidates_by_emotion(emotion, emo_score, top_k=5)
    scored: List[Tuple[str, float]] = []
    for food in candidates:
        s = personalized_score(
            food,
            user_profile,
            emotion,
            emo_score,
            weather,
            now,
            emotion_vector=emotion_vector,
            intensity=intensity,
        )
        scored.append((food, s))
    scored.sort(key=lambda x: x[1], reverse=True)

    return {
        'emotion': emotion,
        'top3': [{'food': f, 'score': s} for f, s in scored[:topn]]
    }
