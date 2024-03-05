package com.example.gdprsample

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {
    private var adImpression = 0
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var consentInformation: ConsentInformation
    // Use an atomic boolean to initialize the Google Mobile Ads SDK and load ads once.
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestConsentInfoUpdate()
    }
    private fun requestConsentInfoUpdate()
    {
        // Create a ConsentRequestParameters object.
        val params = ConsentRequestParameters
            .Builder()
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this@MainActivity
                )
                { loadAndShowError ->
                        if(loadAndShowError !=null)
                        {
                            Snackbar.make(
                                findViewById(R.id.btnShowAds),
                                loadAndShowError.message,
                                Snackbar.LENGTH_INDEFINITE
                            ).apply {
                                setAction("Reload"){requestConsentInfoUpdate()}
                                    .setActionTextColor(getColor(android.R.color.holo_orange_dark))
                                show()
                            }
                        }else {
                            isMobileAdsInitializeCalled.getAndSet(true)
                            loadAds()
                        }
                }

            },
            {
                    requestConsentError ->
                Toast.makeText(this,requestConsentError.message,Toast.LENGTH_LONG).show()

            })
    }
    private fun loadAds()
    {
        if(consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED)
        {
            if(isMobileAdsInitializeCalled.getAndSet(true))
            {

                val adRequest = AdRequest.Builder().build()

                InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        adError.toString().let { Log.d(TAG, it) }
                        mInterstitialAd = null
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(TAG, "Ad was loaded.")
                        mInterstitialAd = interstitialAd
                        adsCallback()
                    }
                })
            }
        }
    }
    private fun adsCallback()
    {
        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                mInterstitialAd = null
                loadAds()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                mInterstitialAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
                adImpression+=1
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }
    }
    private fun showAds()
    {
        if (mInterstitialAd !=null)
        {
            mInterstitialAd?.show(this)
        }
        else{
            Toast.makeText(this,"ads are right here",Toast.LENGTH_LONG).show()
        }

    }

    fun displayAds() {
        showAds()
    }
}