package com.nemesiss.dev.app1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.nemesiss.dev.lib.Person

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val person = Person("Nemesiss.Lin", 22)
        Log.d("MainActivityApp1", person.toString())
    }
}