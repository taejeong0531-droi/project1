import os
from typing import Dict, Any, List, Optional
from google.cloud import firestore

_db: Optional[firestore.Client] = None


def get_db() -> Optional[firestore.Client]:
    global _db
    if _db is not None:
        return _db
    try:
        _db = firestore.Client()
        return _db
    except Exception:
        return None


def load_emotion_food_map() -> Dict[str, List[str]]:
    """
    Firestore: collection `emotion_food_map`, doc id = emotion, field `foods: string[]`
    실패 시 빈 dict 반환.
    """
    db = get_db()
    if not db:
        return {}
    try:
        col = db.collection('emotion_food_map')
        docs = col.stream()
        result: Dict[str, List[str]] = {}
        for d in docs:
            data = d.to_dict() or {}
            foods = data.get('foods') or []
            if isinstance(foods, list):
                result[d.id] = [str(x) for x in foods]
        return result
    except Exception:
        return {}


def load_user_profile(uid: str) -> Dict[str, Any]:
    db = get_db()
    profile: Dict[str, Any] = {}
    if not db:
        return profile
    try:
        # users/{uid}/profile
        doc = db.collection('users').document(uid).collection('profile').document('main').get()
        if doc.exists:
            profile.update(doc.to_dict() or {})
    except Exception:
        pass
    return profile


def load_user_preferences(uid: str) -> Dict[str, Any]:
    db = get_db()
    prefs: Dict[str, Any] = {}
    if not db:
        return prefs
    try:
        # users/{uid}/preferences
        doc = db.collection('users').document(uid).collection('preferences').document('main').get()
        if doc.exists:
            prefs.update(doc.to_dict() or {})
    except Exception:
        pass
    return prefs


def load_user_recent_logs(uid: str, limit: int = 20) -> List[Dict[str, Any]]:
    db = get_db()
    logs: List[Dict[str, Any]] = []
    if not db:
        return logs
    try:
        # users/{uid}/recent_food_logs/* with fields: food:string, timestamp:Timestamp, category:string
        col = db.collection('users').document(uid).collection('recent_food_logs')
        q = col.order_by('timestamp', direction=firestore.Query.DESCENDING).limit(limit)
        for d in q.stream():
            data = d.to_dict() or {}
            item = {
                'food': data.get('food'),
                'time': data.get('timestamp'),
                'category': data.get('category')
            }
            logs.append(item)
    except Exception:
        pass
    return logs
