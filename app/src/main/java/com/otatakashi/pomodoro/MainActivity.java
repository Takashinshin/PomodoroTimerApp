package com.otatakashi.pomodoro;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    //1m * 25(25分のms)
    private static final int pomodoroTimer = 1000 * 60 * 25;
    //休憩用の分
    private static final int breakTimer = 1000 * 60 * 5;
    private static final int interval = 1000;
    private static final int MAX_SEEKBAR = 1500;
    private static final int BREAK_MAX_SEEKBAR = 300;
    private static final int POMODORO_MODE = 0;
    private static final int BREAK_MODE = 1;
    //UI
    private TextView mTimerView;
    private SeekBar  mSeekBar;
    private Button mResetBtn;
    private Button mStartBtn;
    private Button mChangeBtn;
    //0→25分, 1→5分
    private static int mModeFlag;
    /**
     * Broadcastでのfilter
     * COUNT_INTERVAL:指定したinterval事に呼ばれるACTION
     * COUNT_FINISHED:カウントが終了した際に呼ばれるACTION
     */
    public static final String INTERVAL_ACTION = "com.otatakashi.pomodoro.COUNT_INTERVAL";
    public static final String FINISHED_ACTION = "com.otatakashi.pomodoro.COUNT_FINISHED";
    public static final String INTERVAL_KEY = "INTERVAL_KEY";
    private CountDownReceiver mReceiver;
    //25分用のCountDownインスタンス
    private CountDown mPomodoroCountDown;
    //5分用のCountDownインスタンス
    private CountDown mBreakCountDown;
    //時間変換
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimerView = findViewById(R.id.timer);
        mSeekBar = findViewById(R.id.timeSeekBar);
        mChangeBtn = findViewById(R.id.changeModeBtn);
        mStartBtn = findViewById(R.id.startBtn);
        mResetBtn = findViewById(R.id.resetBtn);
        mReceiver = new CountDownReceiver();


        mModeFlag = POMODORO_MODE;
        validModeChange(true, false);

        //モードの変更
        mChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mModeFlag) {
                    case POMODORO_MODE:
                        if (mPomodoroCountDown != null) {
                            mPomodoroCountDown.cancel();
                        }
                        //休憩モードへの遷移
                        changeModeColorBtn(mModeFlag);
                        mModeFlag = BREAK_MODE;
                        mSeekBar.setMax(BREAK_MAX_SEEKBAR);
                        mSeekBar.setProgress(BREAK_MAX_SEEKBAR);
                        mTimerView.setText(R.string.breakTime);
                        break;
                    case BREAK_MODE:
                        if (mBreakCountDown != null) {
                            mBreakCountDown.cancel();
                        }
                        //ポモドーロモードへの遷移
                        changeModeColorBtn(mModeFlag);
                        mModeFlag = POMODORO_MODE;
                        mSeekBar.setMax(MAX_SEEKBAR);
                        mSeekBar.setProgress(MAX_SEEKBAR);
                        mTimerView.setText(R.string.pomodoroTime);
                        break;
                    default:
                        throw new IllegalArgumentException("No Mode!!");
                }
                //Btnを初期状態に戻す
                validModeChange(true, false);
            }
        });

        //タイマースタート
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mModeFlag) {
                    case POMODORO_MODE:
                        mPomodoroCountDown = new CountDown(pomodoroTimer, interval, getApplicationContext());
                        mPomodoroCountDown.start();
                        break;
                    case BREAK_MODE:
                        mBreakCountDown = new CountDown(breakTimer, interval, getApplicationContext());
                        mBreakCountDown.start();
                        break;
                    default:
                        throw new IllegalArgumentException("No Action!!");
                }
                validModeChange(false, true);
            }
        });

        //リセット
        mResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mModeFlag) {
                    case POMODORO_MODE:
                        mPomodoroCountDown.cancel();
                        mTimerView.setText(R.string.pomodoroTime);
                        //seekBarの初期化
                        mSeekBar.setProgress(MAX_SEEKBAR);
                        break;
                    case BREAK_MODE:
                        mBreakCountDown.cancel();
                        mTimerView.setText(R.string.breakTime);
                        mSeekBar.setProgress(BREAK_MAX_SEEKBAR);
                }
                validModeChange(true, false);
            }
        });

        //BroadcastReceiverの登録
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTERVAL_ACTION);
        intentFilter.addAction(FINISHED_ACTION);
        registerReceiver(mReceiver, intentFilter);
    }

    /**
     * Buttonの背景色変更
     */
    @SuppressLint("ResourceAsColor")
    private void changeModeColorBtn(final int mode) {
        if (mode > BREAK_MODE) return;
        final int RED = Color.rgb(255, 0, 0);
        final int PURPLE = Color.rgb(139, 0, 139);

        if (mode == POMODORO_MODE) {
            mChangeBtn.setBackgroundColor(RED);
            mStartBtn.setBackgroundColor(RED);
            mResetBtn.setBackgroundColor(RED);
        } else if (mode == BREAK_MODE) {
            mChangeBtn.setBackgroundColor(PURPLE);
            mStartBtn.setBackgroundColor(PURPLE);
            mResetBtn.setBackgroundColor(PURPLE);
        }
    }

    /**
     * Buttonの有効無効化処理
     */
    private void validModeChange(final boolean start, final boolean reset) {
        mStartBtn.setEnabled(start);
        mResetBtn.setEnabled(reset);
    }

    /**
     * カウントダウンの通知を受けるReceiver
     */
    class CountDownReceiver extends BroadcastReceiver {
        private final String  RECEIVER_TAG = CountDownReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case INTERVAL_ACTION:
                    Log.d(RECEIVER_TAG, "Receive INTERVAL_ACTION");
                    long nowTime = intent.getLongExtra(INTERVAL_KEY, 0);
                    mSeekBar.setProgress((int) nowTime / 1000);
                    mTimerView.setText(dateFormat.format(nowTime));
                    break;
                case FINISHED_ACTION:
                    Log.d(RECEIVER_TAG, "Receive FINISHED_ACTION");
                    if (mModeFlag == POMODORO_MODE) {
                        //25:00を表示して、popupで通知
                        mTimerView.setText(R.string.pomodoroTime);
                        mSeekBar.setProgress(MAX_SEEKBAR);
                        Toast.makeText(getApplicationContext(), "25分経ちました！！", Toast.LENGTH_LONG).show();
                    } else if (mModeFlag == BREAK_MODE) {
                        mTimerView.setText(R.string.breakTime);
                        mSeekBar.setProgress(BREAK_MAX_SEEKBAR);
                        Toast.makeText(getApplicationContext(), "5分経ちました！！", Toast.LENGTH_LONG).show();
                    }
                    validModeChange(true, false);
                    break;
                default:
                    throw new IllegalArgumentException("No Action");
            }
        }
    }
}