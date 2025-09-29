package com.example.myapplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<Boolean> _isLoggedIn = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoggedIn = _isLoggedIn;

    // 로그인 상태 설정
    public void setLoggedIn(boolean isLoggedIn) {
        _isLoggedIn.setValue(isLoggedIn);
    }

    // 로그인 수행
    public void performLogin() {
        _isLoggedIn.setValue(true);
    }

    // 로그아웃 수행
    public void performLogout() {
        _isLoggedIn.setValue(false);
    }
}
