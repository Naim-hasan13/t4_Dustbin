package com.dustbin.hindcash.extrazz;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;

public class AdmobX {
    public static void loadNativeMediaumX(Context context, TemplateView template, String adUnit) {
        template.setVisibility(View.VISIBLE);
        AdLoader adLoader = new AdLoader.Builder(context, adUnit)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        NativeTemplateStyle styles = new
                                NativeTemplateStyle.Builder().build();
                        template.setStyles(styles);
                        template.setNativeAd(nativeAd);


                    }

                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        template.setVisibility(View.GONE);
                        Toast.makeText(context, loadAdError.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        template.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                    }

                    @Override
                    public void onAdOpened() {
                        super.onAdOpened();
                        template.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onAdSwipeGestureClicked() {
                        super.onAdSwipeGestureClicked();
                    }
                })
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());


    }
}
