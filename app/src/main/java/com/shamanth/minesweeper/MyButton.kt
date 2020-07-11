package com.shamanth.minesweeper

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatButton
import com.example.shamanth.minesweeper.R

/**
 ** Created by Shamanth on 27-06-2020
 */
class MyButton(var c: Context?) : AppCompatButton(c) {
    var value = 0
    var isFlagged = false
    var isVisible = false
    var validFlagCounter = 0
    var row = 0
    var col = 0
    fun applyChanges(state: Int) {
        when (state) {
            ButtonConstants.DEFAULT -> super.setBackground(ContextCompat.getDrawable(context, R.drawable.button))
            ButtonConstants.FALSEFLAG, ButtonConstants.ZERO_VALUE -> super.setBackground(ContextCompat.getDrawable(context, R.drawable.null_box))
            ButtonConstants.FLAG -> super.setBackground(ContextCompat.getDrawable(context, R.drawable.flag))
            ButtonConstants.BOMB -> super.setBackground(ContextCompat.getDrawable(context, R.drawable.bombed))
        }
    }

}