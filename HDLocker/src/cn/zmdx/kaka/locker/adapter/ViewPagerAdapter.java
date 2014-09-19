
package cn.zmdx.kaka.locker.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import cn.zmdx.kaka.locker.R;
import cn.zmdx.kaka.locker.settings.InitSettingActivity;
import cn.zmdx.kaka.locker.settings.MainSettingsActivity;

/**
 * 引导页面适配器
 */
public class ViewPagerAdapter extends PagerAdapter {

    // 界面列表
    private List<View> mList;

    private Activity mActivity;

    private static final String SHAREDPREFERENCES_NAME = "first_pref";

    public ViewPagerAdapter(List<View> views, Activity activity) {
        this.mList = views;
        this.mActivity = activity;
    }

    // 销毁arg1位置的界面
    @Override
    public void destroyItem(View arg0, int arg1, Object arg2) {
        ((ViewPager) arg0).removeView(mList.get(arg1));
    }

    @Override
    public void finishUpdate(View arg0) {
    }

    // 获得当前界面数
    @Override
    public int getCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    // 初始化arg1位置的界面
    @Override
    public Object instantiateItem(View arg0, int arg1) {
        ((ViewPager) arg0).addView(mList.get(arg1), 0);
        if (arg1 == mList.size() - 1) {
            ImageView mStartWeiboImageButton = (ImageView) arg0.findViewById(R.id.iv_start_locker);
            mStartWeiboImageButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // 设置已经引导
                    setGuided();
                    goHome();

                }

            });
        }
        return mList.get(arg1);
    }

    private void goHome() {
        // 跳转
        Intent intent = new Intent(mActivity, InitSettingActivity.class);
        intent.putExtra("isFirst", true);
        mActivity.startActivity(intent);
        mActivity.finish();
    }

    /**
     * 设置已经引导过了，下次启动不用再次引导
     */
    private void setGuided() {
        SharedPreferences preferences = mActivity.getSharedPreferences(SHAREDPREFERENCES_NAME,
                Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        // 存入数据
        editor.putBoolean("isFirstIn", false);
        // 提交修改
        editor.commit();
    }

    // 判断是否由对象生成界面
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return (arg0 == arg1);
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(View arg0) {
    }

}