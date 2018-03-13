package com.hellowo.colosseum.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import com.hellowo.colosseum.R
import com.hellowo.colosseum.utils.log


class GoodButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr) {
    lateinit var textView: TextView
    lateinit var progressBar: ProgressBar
    lateinit var text: String

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_good_button, this)
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.GoodButton, 0, 0)
            try {
                text = a.getString(R.styleable.GoodButton_text)
            } finally {
                a.recycle()
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        textView = findViewById(R.id.textView)
        progressBar = findViewById(R.id.progressBar)
        textView.text = text
        setCardBackgroundColor(Color.BLACK)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}