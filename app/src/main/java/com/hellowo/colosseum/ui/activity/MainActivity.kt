package com.hellowo.colosseum.ui.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.hellowo.colosseum.R
import com.hellowo.colosseum.ui.adapter.SwipeStackAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeStack.adapter = SwipeStackAdapter(this, Arrays.asList("1", "2", "3"))
    }
}
