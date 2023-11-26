package com.example.cosmetologistmanager

import android.R
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.example.cosmetologistmanager.databinding.ActivityAppointmentViewBinding


class AppointmentView : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityAppointmentViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAppointmentViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var hash: String? = "";

        val intent = this.intent
        if (intent != null) {
            val name = intent.getStringExtra("name")
            hash = intent.getStringExtra("hash")
            binding.detailName.setText(name)

        }

        binding.editAppointment.setOnClickListener {
            val intent = Intent(this, EditAppointmentActivity::class.java)
            intent.putExtra("hash", hash)
            startActivity(intent)
        }
    }
}