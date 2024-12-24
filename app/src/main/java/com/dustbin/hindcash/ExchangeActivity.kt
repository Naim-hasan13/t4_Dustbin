package com.dustbin.hindcash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dustbin.hindcash.databinding.ActivityExchangeBinding
import com.dustbin.hindcash.databinding.ActivitySplashBinding
import com.kabir.moneytree.extrazz.videoplayyer
import com.quotes.hindcash.Companions
import com.quotes.hindcash.TinyDB
import com.quotes.hindcash.Utils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class ExchangeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExchangeBinding
    
    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityExchangeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.cvExchange2.setOnClickListener{
            if(TinyDB.getString(this,"temp_balance","0")!!.toInt() < 100){
                binding.etEnterValue.error="Minimum 100 Needed"
//                binding.etEnterValue.setText("")

            }else{
                exchangePoint(binding.etEnterValue.text.toString())}
            }


        binding.cvDustbin.setOnClickListener {
            val intent = Intent(this,MenuActivity::class.java)
            startActivity(intent)
        }
    }

    private fun exchangePoint(coin: String) {
        Utils.showLoadingPopUp(this)
        if (coin.isEmpty()) {
            Toast.makeText(this, "Enter Amount", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceid: String = Settings.Secure.getString(
            this.contentResolver, Settings.Secure.ANDROID_ID
        )
        val time = System.currentTimeMillis()

        val url3 = "${Companions.siteUrl}exchange_point.php"
        val email = TinyDB.getString(this, "email", "")

        val queue3: RequestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(Method.POST, url3, { response ->

            val yes = Base64.getDecoder().decode(response)
            val res = String(yes, Charsets.UTF_8)

            if (res.contains(",")) {
                Utils.dismissLoadingPopUp()
                val alldata = res.trim().split(",")

                TinyDB.saveString(this, "temp_balance", alldata[1])
                TinyDB.saveString(this, "balance", alldata[2])
                Toast.makeText(this, alldata[0], Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    Utils.dismissLoadingPopUp()

                    finish()

                }, 1000)

            } else {
                Toast.makeText(this, res, Toast.LENGTH_LONG).show()
            }

        }, { error ->
            Utils.dismissLoadingPopUp()
            Toast.makeText(this, "Internet Slow", Toast.LENGTH_SHORT).show()
            // requireActivity().finish()
        }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()

                val dbit32 = videoplayyer.encrypt(deviceid, Hatbc()).toString()
                val tbit32 = videoplayyer.encrypt(time.toString(), Hatbc()).toString()
                val email = videoplayyer.encrypt(email.toString(), Hatbc()).toString()
                val upi32 = videoplayyer.encrypt(coin.toString(), Hatbc()).toString()

                val den64 = Base64.getEncoder().encodeToString(dbit32.toByteArray())
                val ten64 = Base64.getEncoder().encodeToString(tbit32.toByteArray())
                val email64 = Base64.getEncoder().encodeToString(email.toByteArray())
                val upi64 = Base64.getEncoder().encodeToString(upi32.toByteArray())

                val encodemap: MutableMap<String, String> = HashMap()
                encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64
                encodemap["fdvbdfbhbrthyjsafewwt5yt5"] = email64
                encodemap["defsdfefsefwefwefewfwefvfvdfbdbd"] = upi64

                val jason = Json.encodeToString(encodemap)

                val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())

                val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                params["dase"] = final

                val encodedAppID = Base64.getEncoder().encodeToString(
                    Companions.APP_ID.toString().toByteArray()
                )
                params["app_id"] = encodedAppID

                return params
            }
        }

        queue3.add(stringRequest)


    }

}