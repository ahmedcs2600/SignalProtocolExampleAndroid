package com.app.signalprotocolimplementation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.app.signalprotocolimplementation.ui.group.GroupMessaging
import com.app.signalprotocolimplementation.ui.single.SingleMessaging

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.singleChat).setOnClickListener {
            startActivity(Intent(
                this@MainActivity,
                SingleMessaging::class.java
            ))
        }

        findViewById<Button>(R.id.groupChat).setOnClickListener {
            startActivity(Intent(
                this@MainActivity,
                GroupMessaging::class.java
            ))
        }
    }
}