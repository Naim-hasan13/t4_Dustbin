package com.dustbin.hindcash

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dustbin.hindcash.databinding.ActivityMenuBinding
import com.quotes.hindcash.TinyDB

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
binding.tempBalance.text =TinyDB.getString(this,"temp_balance","0");
        binding.balance.text =TinyDB.getString(this,"balance","0");

        binding.tvRupee.text ="₹"+(TinyDB.getString(this,"balance","0")!!.toInt()/1000).toString();

        if(TinyDB.getString(this,"balance","0")!!.toInt() > TinyDB.getString(this,"balance_withdrawal_limit","0")!!.toInt()){
           binding.tvRedeem.text="Redeem"
            binding.cvRedeem.setOnClickListener {
                val intent = Intent(this,RedeemActivity::class.java)
                startActivity(intent)
            }
        }
        binding.cvExchange.setOnClickListener {
            val intent = Intent(this,ExchangeActivity::class.java)
            startActivity(intent)
        }
    }
}