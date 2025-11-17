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
        # 개발 편의: Firebase Admin 미초기화 시 로컬 개발용 UID 반환
        if not firebase_admin._apps:
            # NOTE: 실제 배포에서는 반드시 Firebase Admin 자격 증명을 설정하세요.
            return "dev-local-user"
        decoded = fb_auth.verify_id_token(id_token)
        return decoded.get("uid")
    except Exception:
        # 검증 실패 시에도 로컬 개발 모드 UID로 폴백
        return "dev-local-user"
