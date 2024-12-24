package com.dustbin.hindcash

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
import com.dustbin.hindcash.databinding.ActivityRedeemBinding
import com.kabir.moneytree.extrazz.videoplayyer
import com.quotes.hindcash.Companions
import com.quotes.hindcash.TinyDB
import com.quotes.hindcash.Utils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class RedeemActivity : AppCompatActivity() {
    lateinit var binding: ActivityRedeemBinding
    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedeemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnRedeem.setOnClickListener {
            val upi = binding.etUpi.text.toString()
            if (upi.isNotEmpty()) {
                redeemPoint(upi)
            } else {
                binding.etUpi.error = "Enter UPI"
            }
        }
    }

    private fun redeemPoint(upi: String) {
        Utils.showLoadingPopUp(this)
        if (upi.isEmpty()) {
            Toast.makeText(this, "Enter UPI", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceid: String = Settings.Secure.getString(
            this.contentResolver, Settings.Secure.ANDROID_ID
        )
        val time = System.currentTimeMillis()

        val url3 = "${Companions.siteUrl}redeem_point.php"
        val email = TinyDB.getString(this, "email", "")

        val queue3: RequestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(Method.POST, url3, { response ->

            val yes = Base64.getDecoder().decode(response)
            val res = String(yes, Charsets.UTF_8)

            if (res.contains(",")) {
                Utils.dismissLoadingPopUp()
                val alldata = res.trim().split(",")

                TinyDB.saveString(this, "balance", alldata[1])
                Toast.makeText(this, alldata[0], Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    Utils.dismissLoadingPopUp()

                    finish()

                }, 1000)

            } else {
                Toast.makeText(this, res, Toast.LENGTH_LONG).show()
                finish()
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
                val upi32 = videoplayyer.encrypt(upi.toString(), Hatbc()).toString()

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