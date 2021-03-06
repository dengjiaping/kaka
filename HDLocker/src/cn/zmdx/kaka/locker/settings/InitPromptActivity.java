
package cn.zmdx.kaka.locker.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import cn.zmdx.kaka.locker.R;
import cn.zmdx.kaka.locker.settings.config.PandoraUtils;

import com.umeng.analytics.MobclickAgent;

public class InitPromptActivity extends Activity {

    private LinearLayout mCloseSystemLockerView;

    private LinearLayout mV5CloseSystemLockerView;

    private LinearLayout mV5AllowFloatWindowView;

    private LinearLayout mV5TrustView;

    private LinearLayout mV6CloseSystemLockerView;

    private LinearLayout mV6AllowFloatWindowView;

    private LinearLayout mV6TrustView;

    private boolean isMIUI;

    private String mMIUIVersion;

    public static final int PROMPT_CLOSE_SYSTEM_LOCKER = 1;

    public static final int PROMPT_ALLOW_FLOAT_WINDOW = 2;

    public static final int PROMPT_TRRST = 3;

    private int mPromptType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init_prompt_activity);
        isMIUI = getIntent().getBooleanExtra("isMIUI", false);
        mMIUIVersion = getIntent().getStringExtra("mMIUIVersion");
        mPromptType = getIntent().getIntExtra("type", PROMPT_CLOSE_SYSTEM_LOCKER);
        initView();
        showView();
    }

    private void initView() {
        if (isMIUI) {
            if (PandoraUtils.MUIU_V6.equals(mMIUIVersion)) {
                findViewById(R.id.init_setting_close_systemlocker_prompt).setVisibility(View.GONE);
                findViewById(R.id.init_setting_MIUI_V5).setVisibility(View.GONE);
                findViewById(R.id.init_setting_MIUI_V6).setVisibility(View.VISIBLE);
                mV6CloseSystemLockerView = (LinearLayout) findViewById(R.id.init_setting_V6_close_systemlocker_prompt);
                mV6AllowFloatWindowView = (LinearLayout) findViewById(R.id.init_setting_V6_allow_floating_window_prompt);
                mV6TrustView = (LinearLayout) findViewById(R.id.init_setting_V6_trust_prompt);
            } else {
                findViewById(R.id.init_setting_close_systemlocker_prompt).setVisibility(View.GONE);
                findViewById(R.id.init_setting_MIUI_V5).setVisibility(View.VISIBLE);
                findViewById(R.id.init_setting_MIUI_V6).setVisibility(View.GONE);
                mV5CloseSystemLockerView = (LinearLayout) findViewById(R.id.init_setting_V5_close_systemlocker_prompt_miui);
                mV5AllowFloatWindowView = (LinearLayout) findViewById(R.id.init_setting_V5_allow_floating_window_prompt);
                mV5TrustView = (LinearLayout) findViewById(R.id.init_setting_V5_trust_prompt);
            }
        } else {
            findViewById(R.id.init_setting_MIUI_V5).setVisibility(View.GONE);
            findViewById(R.id.init_setting_MIUI_V6).setVisibility(View.GONE);
            mCloseSystemLockerView = (LinearLayout) findViewById(R.id.init_setting_close_systemlocker_prompt);
        }
    }

    private void showView() {
        if (isMIUI) {
            if (PandoraUtils.MUIU_V6.equals(mMIUIVersion)) {

                switch (mPromptType) {
                    case PROMPT_CLOSE_SYSTEM_LOCKER:
                        mV6CloseSystemLockerView.setVisibility(View.VISIBLE);
                        break;
                    case PROMPT_ALLOW_FLOAT_WINDOW:
                        mV6AllowFloatWindowView.setVisibility(View.VISIBLE);
                        break;
                    case PROMPT_TRRST:
                        mV6TrustView.setVisibility(View.VISIBLE);
                        break;

                    default:
                        break;
                }

            } else {

                switch (mPromptType) {
                    case PROMPT_CLOSE_SYSTEM_LOCKER:
                        mV5CloseSystemLockerView.setVisibility(View.VISIBLE);
                        break;
                    case PROMPT_ALLOW_FLOAT_WINDOW:
                        mV5AllowFloatWindowView.setVisibility(View.VISIBLE);
                        break;
                    case PROMPT_TRRST:
                        mV5TrustView.setVisibility(View.VISIBLE);
                        break;

                    default:
                        break;
                }

            }
        } else {
            mCloseSystemLockerView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        onBackPressed();
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.umeng_fb_slide_in_from_left,
                R.anim.umeng_fb_slide_out_from_right);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("InitPromptActivity"); // 统计页面
        MobclickAgent.onResume(this); // 统计时长
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("InitPromptActivity"); // 保证 onPageEnd
                                                       // 在onPause之前调用,因为
        // onPause 中会保存信息
        MobclickAgent.onPause(this);
    }
}
