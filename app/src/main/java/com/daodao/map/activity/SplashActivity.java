package com.daodao.map.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.daodao.map.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;



public class SplashActivity extends Activity {

    private Handler handler;

    private InterstitialAd mInterstitialAd;
//    private CountDownTimer mCountDownTimer;
//    private long mTimerMilliseconds;

//    private TextView tvTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        tvTime = (TextView) findViewById(R.id.activity_splash_time);

        MobileAds.initialize(this, "ca-app-pub-8949214993931766~2994762737");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_loading_unit_id));

//        mInterstitialAd.setAdListener(new AdListener() {
//            @Override
//            public void onAdClosed() {
//                super.onAdClosed();
//                startGame();
//            }
//        });


//        mCountDownTimer = new CountDownTimer(5000, 50) {
//            @Override
//            public void onTick(long millisUnitFinished) {
////                mTimerMilliseconds = millisUnitFinished;
//                tvTime.setText(((millisUnitFinished / 1000) + 1) + " seconds");
//            }
//
//            @Override
//            public void onFinish() {
////                mGameIsInProgress = false;
////                textView.setText("done!");
////                mRetryButton.setVisibility(View.VISIBLE);
//                Intent intent = new Intent(SplashActivity.this, RouteActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        };



        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    Intent intent = new Intent(SplashActivity.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        handler.sendEmptyMessageDelayed(1, 4000);

        startGame();

    }

    private void startGame() {
        // Request a new ad if one isn't already loaded, hide the button, and kick off the timer.
        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder().addTestDevice(getString(R.string.banner_ad_loading_unit_id)).build();
//            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }
        mInterstitialAd.show();
//        if (mCountDownTimer != null) {
//            mCountDownTimer.start();
//        }

//        mRetryButton.setVisibility(View.INVISIBLE);
//        resumeGame(GAME_LENGTH_MILLISECONDS);
    }
}
