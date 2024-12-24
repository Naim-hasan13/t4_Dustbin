package com.dustbin.hindcash

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dustbin.hindcash.databinding.ActivityMainBinding
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
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var score = 0
    private var timer: CountDownTimer? = null
    private val gameDuration = 30000L // 30 seconds

    private var kachraStartX = 0f
    private var kachraStartY = 0f

    var isApiCallable = true
    external fun Hatbc(): String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startUpDownAnimation()
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupGame()



        binding.lottieAnimationView.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupGame() {
        binding.llHome2.setOnClickListener {
            binding.llHome.visibility = android.view.View.GONE
            binding.llGame.visibility = android.view.View.VISIBLE
            startGame()
        }

        binding.cvRetry.setOnClickListener {
            restartGame()
        }

        binding.cvHome.setOnClickListener {
            binding.llAfterGame.visibility = android.view.View.GONE
            binding.llHome.visibility = android.view.View.VISIBLE
        }

        // Touch event for dragging kachra
        binding.ivKachra.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    kachraStartX = view.x
                    kachraStartY = view.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val x = motionEvent.rawX - view.width / 2
                    val y = motionEvent.rawY - view.height / 2
                    view.x = x
                    view.y = y
                }
                MotionEvent.ACTION_UP -> {
                    checkCollision()
                }
            }
            true
        }
    }
    private fun startUpDownAnimation() {
        // Animate ivLogo to move up and down
        val animator = ObjectAnimator.ofFloat(
            binding.ivLogo, // Target the ivLogo ImageView
            "translationY", // Move vertically
            -50f, 50f // Values to animate (up: -50f, down: 50f)
        ).apply {
            duration = 1000 // 1 second for one direction
            repeatMode = ObjectAnimator.REVERSE // Reverse direction after reaching end
            repeatCount = ObjectAnimator.INFINITE // Repeat indefinitely
        }

        animator.start() // Start the animation
    }

    private fun startGame() {
        score = 0
        binding.tvCount.text = "0"
        startTimer()
        resetKachraPosition()

        // Wait until the layout is ready to calculate positions
        binding.llGame.post {
            randomizeDustbinPosition()
        }
    }

    private fun resetKachraPosition() {
        binding.ivKachra.x = binding.llGame.width / 2f - binding.ivKachra.width / 2f
        binding.ivKachra.y = binding.llGame.height - binding.ivKachra.height * 2f
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(gameDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                addPoint(score)
                binding.llGame.visibility = android.view.View.GONE
                binding.llAfterGame.visibility = android.view.View.VISIBLE
                binding.count.text = "x $score"
            }
        }.start()
    }

    private fun checkCollision() {
        val kachra = binding.ivKachra
        val dustbin = binding.ivDustbin

        val kachraX = kachra.x
        val kachraY = kachra.y
        val dustbinX = dustbin.x
        val dustbinY = dustbin.y

        if (kachraX < dustbinX + dustbin.width &&
            kachraX + kachra.width > dustbinX &&
            kachraY < dustbinY + dustbin.height &&
            kachraY + kachra.height > dustbinY
        ) {
            // Collision detected
            score++
            binding.tvCount.text = score.toString()
            resetKachraPosition()
            randomizeDustbinPosition()
        } else {
            // Return to start position
            resetKachraPosition()
        }
    }

    private fun randomizeDustbinPosition() {
        val parentLayout = binding.llGame
        val dustbin = binding.ivDustbin

        // Ensure valid range for random placement
        val maxX = parentLayout.width - dustbin.width
        val maxY = parentLayout.height - dustbin.height

        if (maxX > 0 && maxY > 0) {
            val randomX = Random.nextInt(0, maxX)
            val randomY = Random.nextInt(0, maxY)

            dustbin.x = randomX.toFloat()
            dustbin.y = randomY.toFloat()
        }
    }

    private fun addPoint(coin:Int) {
        Utils.showLoadingPopUp(this)
        if (TinyDB.getString(this, "play_limit", "0") == "0") {
            Utils.dismissLoadingPopUp()
            Toast.makeText(this, "Today's Limit End, Come Back Tomorrow !", Toast.LENGTH_SHORT)
                .show()

        } else {
            val deviceid: String = Settings.Secure.getString(
                contentResolver, Settings.Secure.ANDROID_ID
            )
            val time = System.currentTimeMillis()

            val url3 = "${Companions.siteUrl}play_temp_point.php"
            val email = TinyDB.getString(this, "email", "")

            val queue3: RequestQueue = Volley.newRequestQueue(this)
            val stringRequest =
                object : StringRequest(Method.POST, url3, { response ->

                    val yes = Base64.getDecoder().decode(response)
                    val res = String(yes, Charsets.UTF_8)
                    Utils.dismissLoadingPopUp()

                    if (res.contains(",")) {
                        val alldata = res.trim().split(",")
                        TinyDB.saveString(this, "play_limit", alldata[2])
                        TinyDB.saveString(this, "temp_balance", alldata[1])
                        isApiCallable = true
                    } else {
                        Toast.makeText(this, res, Toast.LENGTH_SHORT).show()

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
                        val coin32 = videoplayyer.encrypt(coin.toString(), Hatbc()).toString()

                        val den64 = Base64.getEncoder().encodeToString(dbit32.toByteArray())
                        val ten64 = Base64.getEncoder().encodeToString(tbit32.toByteArray())
                        val email64 = Base64.getEncoder().encodeToString(email.toByteArray())
                        val coin64 = Base64.getEncoder().encodeToString(coin32.toByteArray())

                        val encodemap: MutableMap<String, String> = HashMap()
                        encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                        encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64
                        encodemap["fdvbdfbhbrthyjsafewwt5yt5"] = email64
                        encodemap["cfgcfxxghggujrzeawsrthyuyikhikfu"] = coin64


                        val jason = Json.encodeToString(encodemap)

                        val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())

                        val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                        params["dase"] = final

                        val encodedAppID = Base64.getEncoder()
                            .encodeToString(
                                Companions.APP_ID.toString().toByteArray()
                            )
                        params["app_id"] = encodedAppID

                        return params
                    }
                }

            queue3.add(stringRequest)
        }


    }

    private fun restartGame() {
        binding.llAfterGame.visibility = android.view.View.GONE
        binding.llGame.visibility = android.view.View.VISIBLE
        startGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
