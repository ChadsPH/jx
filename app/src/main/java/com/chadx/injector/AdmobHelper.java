package com.chadx.injector;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import java.util.Timer;
import java.util.TimerTask;

public class AdmobHelper
{

    private Context context;
    private AdRequest.Builder adRequest;
    private InterstitialAd interstitial;
    private AdRequest adR;
    private AdView adView;
    public RewardedVideoAd mRewardedVideoAd;
    private boolean mShowInterAdsAuto = false;
    private RelativeLayout bannerView;
    private String bannerId = "";
    private String intertialId = "";
    private String mRewardedId = "";
    private static boolean timerads = false; 



    public AdmobHelper(Context c)
    {
        context = c;
        adRequest = new AdRequest.Builder();
        adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        adView = new AdView(context);
        adView.setAdListener(bal);
        interstitial = new InterstitialAd(context);
        interstitial.setAdListener(al);
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);

        // Insert the Ad Unit ID
    }

    public AdmobHelper setMobileAdsId(String id)
    {
        MobileAds.initialize(context, id);
        return this;
    }

    public AdmobHelper setBannerView(RelativeLayout v)
    {
        this.bannerView = v;
        bannerView.addView(adView);
        //bannerView.addView(tv);
        return this;
    }

    public AdmobHelper setBannerId(String id)
    {
        bannerId = id;
        adView.setAdUnitId(id);
        return this;
    }

    public AdmobHelper setBannerSize(AdSize size)
    {
        adView.setAdSize(size);
        return this;
    }

    public AdmobHelper setIntertitialId(String id)
    {
        intertialId = id;
        interstitial.setAdUnitId(id);
        return this;
    }

    public AdmobHelper setRewardedId(String id)
    {
        mRewardedId = id;
        return this;
    }

    public AdmobHelper setTestDevice(String device_id)
    {
        adRequest.addTestDevice(device_id);
        return this;
    }

    public AdmobHelper setShowInterAdsAuto(boolean status) {
        mShowInterAdsAuto = status;
        return this;
    }

    public AdmobHelper setAdsListener(AdListener listener)
    {
        if (adView != null)
        {
            adView.setAdListener(listener);
        }
        return this;
    }

    public AdmobHelper buildAdsRequest()
    {
        adR = adRequest.build();
        return this;
    }

    public AdmobHelper loadBannerAdsRequest()
    {
        if (!bannerId.isEmpty())
        {
            adView.loadAd(adR);
        }
        return this;
    }

    public AdmobHelper loadAdsRequest()
    {
        if (!bannerId.isEmpty())
        {
            adView.loadAd(adR);
        }
        if (!intertialId.isEmpty())
        {
            interstitial.loadAd(adR);
        }

        return this;
    }

    public void loadIntertitial() {
        if (!intertialId.isEmpty())
        {
            interstitial.loadAd(adR);
        }
    }


    public AdmobHelper initTimerAds(int time)
    {
        timerads = true;
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run()
            {
                handler.post(new Runnable() {
                        public void run()
                        {
                            if (adR != null)
                            {
                                if (!bannerId.isEmpty())
                                {
                                    adView.loadAd(adR);
                                }
                                if (!intertialId.isEmpty())
                                {
                                    interstitial.loadAd(adR);
                                }
                            }
                        }
                    });
            }
        };
        timer.schedule(doAsynchronousTask, 0, time);
        return this;
    }

    public void reloadAds()
    {
        if (!timerads)
        {
            if (adR != null)
            {
                if (!bannerId.isEmpty())
                {
                    adView.loadAd(adR);
                }
                if (!intertialId.isEmpty())
                {
                    interstitial.loadAd(adR);
                }
            }
        }
    }

    public void showIntertitialAds()
    {
        if (interstitial != null)
        {
            if (interstitial.isLoaded())
            {
                interstitial.show();
            }
        }
    }

    public void showRewardedAds()
    {
        if (!mRewardedId.isEmpty())
        {
            mRewardedVideoAd.show();
        }
    }

    AdListener bal = new AdListener() {

        @Override
        public void onAdLoaded()
        {
            // Code to be executed when an ad finishes loading.
            bannerView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAdFailedToLoad(int errorCode)
        {
            // Code to be executed when an ad request fails.
            bannerView.setVisibility(View.GONE);
        }

        @Override
        public void onAdOpened()
        {
            //Toast.makeText(context, "", 0).show();
            // Code to be executed when an ad opens an overlay that
            // covers the screen.
        }

        @Override
        public void onAdLeftApplication()
        {
            // Code to be executed when the user has left the app.
        }

        @Override
        public void onAdClosed()
        {

            //Toast.makeText(context, "Thanks for supporting us.", 0).show();
            // Code to be executed when when the user is about to return
            // to the app after tapping on an ad.
            //reloadAds();
        }
    };

    AdListener al = new AdListener() {

        @Override
        public void onAdLoaded()
        {
            // Code to be executed when an ad finishes loading.
            if(mShowInterAdsAuto) {
                interstitial.show();
            }
        }

        @Override
        public void onAdFailedToLoad(int errorCode)
        {
            // Code to be executed when an ad request fails.
        }

        @Override
        public void onAdOpened()
        {
            //Toast.makeText(context, "", 0).show();
            // Code to be executed when an ad opens an overlay that
            // covers the screen.
        }

        @Override
        public void onAdLeftApplication()
        {
            // Code to be executed when the user has left the app.
        }

        @Override
        public void onAdClosed()
        {
            if(!mShowInterAdsAuto) {
                loadAdsRequest();
            }
            //Toast.makeText(context, "Thanks for supporting us.", 0).show();
            // Code to be executed when when the user is about to return
            // to the app after tapping on an ad.
            //reloadAds();
        }
    };
}








