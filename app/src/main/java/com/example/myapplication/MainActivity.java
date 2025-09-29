package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel; // ViewModel 선언 (초기화는 onCreate에서)
A
    private EditText editTextId;
    private EditText editTextPassword;
    private Button buttonAction; // 로그인 또는 감정분석하기 버튼
    private Button buttonSignup;
    private Button buttonFindId;
    private Button buttonFindPassword;
    private LinearLayout layoutAuthHelpers; // 회원가입, 아이디/비밀번호 찾기 버튼 그룹

    private ActivityResultLauncher<Intent> loginActivityResultLauncher;

    public static final String PREFS_NAME = "MyLoginPrefs";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    // MainActivity의 기본 생성자는 일반적으로 명시적으로 만들 필요가 없습니다.
    // public MainActivity() {
    // }

/* <<<<<<<<<<<<<<  ✨ Windsurf Command ⭐ >>>>>>>>>>>>>>>> */
    /**
     * Activity의 기본 생성자
     * <p> 이 메서드는 {@link MainViewModel}을 초기화하고, UI 요소를 초기화합니다.
     * 그리고 {@link ActivityResultContracts.StartActivityForResult()}를 사용하여 {@link LoginActivity}로부터 결과를 받습니다.
     * </p>
     * <p> 이 메서드는 SharedPreferences에서 이전 로그인 상태를 불러옵니다.
     * 그리고 로그인 성공/실패 결과에 따라 UI를 업데이트합니다.
     * </p>
/* <<<<<<<<<<  2ed79157-eca4-45de-95ec-fe2e34c0046e  >>>>>>>>>>> */
    @Override00000000
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ViewModel 초기화 (올바른 위치)
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // UI 요소 초기화
        editTextId = findViewById(R.id.textid);
        editTextPassword = findViewById(R.id.textpassword);
        buttonAction = findViewById(R.id.menuBtn);
        buttonSignup = findViewById(R.id.signupBtn);
        buttonFindId = findViewById(R.id.passwordfindid);
        buttonFindPassword = findViewById(R.id.passwordfindBtn);

        // authHelperLayout ID를 가진 LinearLayout이 XML에 있는지 확인하고 연결합니다.
        // XML에 <LinearLayout android:id="@+id/authHelperLayout" ...> 와 같이 정의되어 있어야 합니다.
        View layoutView = findViewById(R.id.authHelperLayout); // 실제 XML ID로 변경 필요할 수 있음
        if (layoutView instanceof LinearLayout) {
            layoutAuthHelpers = (LinearLayout) layoutView;
        }

        // LoginActivity로부터 결과를 받기 위한 설정
        loginActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // LoginActivity에서 로그인 성공 결과를 받음
                        mainViewModel.performLogin(); // ViewModel에 로그인 성공 알림
                        saveLoginState(true);    // SharedPreferences에 로그인 상태 저장
                        Toast.makeText(this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

        // ViewModel의 isLoggedIn LiveData 관찰하여 UI 업데이트
        mainViewModel.isLoggedIn.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoggedIn) {
                updateUI(isLoggedIn);
            }
        });

        // 앱 시작 시 SharedPreferences에서 이전 로그인 상태 불러오기
        boolean isLoggedInPreviously = getLoginState();
        if (isLoggedInPreviously) {
            mainViewModel.performLogin(); // ViewModel 상태 동기화 (UI는 observe를 통해 자동 업데이트됨)
        } else {
            updateUI(false); // 로그아웃 상태 UI로 초기화 (필요시 호출, observe가 초기값도 처리)
        }

        // 초기 버튼 리스너 설정 (로그아웃 상태 기준)
        setupInitialButtonListeners();

    } // ### onCreate() 메소드 닫는 괄호 ###

    private void setupInitialButtonListeners() {
        // 이 함수는 로그인 상태에 따라 리스너 내용이 바뀌므로,
        // update