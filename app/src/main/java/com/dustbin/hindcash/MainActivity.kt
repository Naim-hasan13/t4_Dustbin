package com.dustbin.hindcash

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dustbin.hindcash.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var score = 0
    private var timer: CountDownTimer? = null
    private val gameDuration = 30000L // 30 seconds

    private var kachraStartX = 0f
    private var kachraStartY = 0f

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
