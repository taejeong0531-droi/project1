import os
from typing import Dict, Any, Optional
import requests


def fetch_weather_by_latlon(lat: float, lon: float, api_key: Optional[str] = None) -> Dict[str, Any]:
    api_key = api_key or os.getenv('OPENWEATHER_API_KEY')
    # Fast default in case of missing key or network failure
    default_weather: Dict[str, Any] = {'temp_c': 20.0, 'status': 'mild'}
    if not api_key:
        return default_weather
    url = 'https://api.openweathermap.org/data/2.5/weather'
    params = {
        'lat': lat,
        'lon': lon,
        'appid': api_key,
        'units': 'metric'
    }
    try:
        r = requests.get(url, params=params, timeout=3)
        r.raise_for_status()
        data = r.json()
        temp_c = data['main']['temp']
        cond = data['weather'][0]['main'].lower()  # 'rain' | 'snow' | 'clear' | ...
        status = 'rain' if 'rain' in cond else ('snow' if 'snow' in cond else ('clear' if 'clear' in cond else 'mild'))
        return {'temp_c': float(temp_c), 'status': status}
    except Exception:
        return default_weather
