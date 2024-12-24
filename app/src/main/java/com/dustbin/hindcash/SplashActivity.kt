package com.dustbin.hindcash

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dustbin.hindcash.databinding.ActivitySplashBinding
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.google.android.material.card.MaterialCardView
import com.kabir.moneytree.extrazz.videoplayyer
import com.quotes.hindcash.Companions
import com.quotes.hindcash.TinyDB
import com.quotes.hindcash.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load animation
        val animation = AnimationUtils.loadAnimation(this, R.anim.scale_animation)
        binding.ivLogo.startAnimation(animation)

        getUserValue()

        // Delay and navigate to MainActivity

    }

    fun getUserValue() {

        val deviceid: String = Settings.Secure.getString(
            this.contentResolver, Settings.Secure.ANDROID_ID
        )
        val time = System.currentTimeMillis()

        val url2 = "${Companions.siteUrl}getuservalue.php"
        val email = TinyDB.getString(this, "email", "")

        val queue1: RequestQueue = Volley.newRequestQueue(this)
        val stringRequest =
            object : StringRequest(Method.POST, url2, { response ->
                val ytes = Base64.getDecoder().decode(response)
                val res = String(ytes, Charsets.UTF_8)

                if (res.contains(",")) {
                    val alldata = res.trim().split(",")
                    TinyDB.saveString(this, "phone", alldata[0])
                    TinyDB.saveString(this, "maintenance", alldata[1])
                    TinyDB.saveString(this, "version", alldata[2])
                    TinyDB.saveString(this, "balance", alldata[3])
                    TinyDB.saveString(this, "earning_ad_limit", alldata[4])
                    TinyDB.saveString(this, "telegram_link", alldata[5])
                    TinyDB.saveString(this, "refer_code", alldata[6])
                    TinyDB.saveString(this, "sponsor_link", alldata[7])
                    TinyDB.saveString(this, "app_link", alldata[8])
                    TinyDB.saveString(this, "play_limit", alldata[9])
                    TinyDB.saveString(this, "show_ads_after_each", alldata[10])
                    TinyDB.saveString(this, "adx_app_id", alldata[11])
                    TinyDB.saveString(this, "adx_inter", alldata[12])
                    TinyDB.saveString(this, "adx_native", alldata[13])
                    TinyDB.saveString(this, "balance_show_limit", alldata[14])
                    TinyDB.saveString(this, "balance_withdrawal_limit", alldata[15])
                    TinyDB.saveString(this, "adx_banner", alldata[16])
                    TinyDB.saveString(this, "adx_app_open", alldata[18])
                    TinyDB.saveString(this, "temp_balance", alldata[19])


                    updateAdMobAppId(alldata[11])
                    //setBalanceText()
                    if (alldata[2].toInt() > Companions.APP_VERSION) {
                        showUpdatePopup()
                    } else if (alldata[1] == "1") {
                        showMaintaincePopup()
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Close SplashActivity
                    }, 2000) // 2 seconds

                } else {
                    Toast.makeText(this, res, Toast.LENGTH_LONG).show()
                    finish()
                }


            }, { _ ->
                Utils.dismissLoadingPopUp()
                Toast.makeText(this, "Internet Slow", Toast.LENGTH_SHORT).show()
                finish()
            }) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()

                    val dbit32 = videoplayyer.encrypt(deviceid, Hatbc()).toString()
                    val tbit32 = videoplayyer.encrypt(time.toString(), Hatbc()).toString()
                    val email = videoplayyer.encrypt(email.toString(), Hatbc()).toString()

                    val den64 = Base64.getEncoder().encodeToString(dbit32.toByteArray())
                    val ten64 = Base64.getEncoder().encodeToString(tbit32.toByteArray())
                    val email64 = Base64.getEncoder().encodeToString(email.toByteArray())

                    val encodemap: MutableMap<String, String> = HashMap()
                    encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                    encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64

                    val jason = Json.encodeToString(encodemap)

                    val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())

                    val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                    params["dase"] = final

                    val encodedAppID = Base64.getEncoder()
                        .encodeToString(Companions.APP_ID.toString().toByteArray())
                    params["app_id"] = encodedAppID

                    return params
                }
            }

        queue1.add(stringRequest)
    }

    private fun updateAdMobAppId(adMobAppId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val applicationInfo = packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                )
                val metaData = applicationInfo.metaData

                if (metaData != null) {
                    metaData.putString("com.google.android.gms.ads.APPLICATION_ID", adMobAppId)
                    println("AdMob App ID updated: $adMobAppId")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            MobileAds.initialize(this@SplashActivity) {

            }

        }
    }

    private fun showMaintaincePopup() {
        AlertDialog.Builder(this, R.style.updateDialogTheme).setView(R.layout.popup_maintaince)
            .setCancelable(false).create().apply {
                show()
                findViewById<MaterialCardView>(R.id.cv_okay)?.setOnClickListener {
                    Utils.openUrl(
                        this@SplashActivity,
                        TinyDB.getString(this@SplashActivity, "telegram_link", "0")!!
                    )
                }
            }
    }

    private fun showUpdatePopup() {
        AlertDialog.Builder(this, R.style.updateDialogTheme).setView(R.layout.popup_newupdate)
            .setCancelable(false).create().apply {
                show()
                findViewById<MaterialCardView>(R.id.cv_okay)?.setOnClickListener {
                    Utils.openUrl(
                        this@SplashActivity,
                        TinyDB.getString(this@SplashActivity, "app_link", "")!!
                    )

                }
            }

    }


}
