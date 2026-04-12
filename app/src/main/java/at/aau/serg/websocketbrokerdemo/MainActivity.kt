package at.aau.serg.websocketbrokerdemo

import MyStomp
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.R

class MainActivity : ComponentActivity(), Callbacks {
    lateinit var myStomp: MyStomp
    lateinit var response: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        myStomp = MyStomp(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_fullscreen)

        findViewById<Button>(R.id.connectbtn).setOnClickListener { myStomp.connect() }
        findViewById<Button>(R.id.hellobtn).setOnClickListener { myStomp.sendHello() }
        findViewById<Button>(R.id.jsonbtn).setOnClickListener { myStomp.sendJson() }
        response = findViewById(R.id.response_view)

        // ---> HIER KOMMT DEIN NEUER BUTTON REIN <---
        val btnTest: Button = findViewById(R.id.btnTestConsole)
        btnTest.setOnClickListener {
            // Dieser Code ist die "Verlinkung", die dich zur Konsole bringt!
            val intent = Intent(this, FakeConsoleActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResponse(res: String) {
        response.text = res
    }


}

