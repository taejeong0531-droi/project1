import os
from typing import Optional

import firebase_admin
from firebase_admin import auth as fb_auth, credentials


_app_inited = False

def init_firebase_if_needed() -> None:
    global _app_inited
    if _app_inited:
        return
    cred_json = os.getenv("FIREBASE_CREDENTIALS_JSON")
    if cred_json:
        import json
        cred = credentials.Certificate(json.loads(cred_json))
        firebase_admin.initialize_app(cred)
        _app_inited = True
        return
    # GOOGLE_APPLICATION_CREDENTIALS 경로를 사용하거나, 기본 앱으로 초기화 시도
    try:
        firebase_admin.get_app()
        _app_inited = True
    except ValueError:
        try:
            firebase_admin.initialize_app()
            _app_inited = True
        except Exception:
            # 로컬/개발 모드에서 인증이 없는 경우를 대비해 무시
            _app_inited = False


def verify_firebase_token(id_token: str) -> Optional[str]:
    """
    유효하면 Firebase UID 반환, 실패/미초기화 시 None 반환.
    """
    init_firebase_if_needed()
    try:
        if not firebase_admin._apps:
            return None
        decoded = fb_auth.verify_id_token(id_token)
        return decoded.get("uid")
    except Exception:
        return None
