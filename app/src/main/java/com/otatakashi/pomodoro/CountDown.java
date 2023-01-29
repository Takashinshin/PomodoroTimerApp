package com.otatakashi.pomodoro;

import static com.otatakashi.pomodoro.MainActivity.FINISHED_ACTION;
import static com.otatakashi.pomodoro.MainActivity.INTERVAL_ACTION;
import static com.otatakashi.pomodoro.MainActivity.INTERVAL_KEY;


import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * カウントダウンを行うクラス
 */
public class CountDown extends CountDownTimer {
    private static final String TAG = CountDown.class.getSimpleName();

    private Context mContext;

    /**
     * コンストラクタ
     */
    public CountDown(long setMinTime, long countDownInterval, Context context) {
        super(setMinTime, countDownInterval);
        mContext = context;
    }

    /**
     * intervalで呼ばれる
     * @param millisUntilFinished
     */
    @Override
    public void onTick(long millisUntilFinished) {
        Log.d(TAG, "millisUntilFinished:" + millisUntilFinished);
        long nowTime = millisUntilFinished;
        //String nowTime = dateFormat.format(millisUntilFinished);
        //intervalを残り秒数と一緒に投げる
        Intent intent = new Intent(INTERVAL_ACTION);
        intent.putExtra(INTERVAL_KEY, nowTime);
        mContext.sendBroadcast(intent);
    }

    /**
     * 終わった際に呼ばれる
     */
    @Override
    public void onFinish() {
        Log.d(TAG, "onFinished()");
        //完了通知を投げる
        Intent intent = new Intent(FINISHED_ACTION);
        mContext.sendBroadcast(intent);
    }
}
