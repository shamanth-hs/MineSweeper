package com.shamanth.minesweeper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import com.example.shamanth.minesweeper.R
import java.lang.Exception


/**
 ** Created by Shamanth on 27-06-2020
 */
class DashBoardActivity :AppCompatActivity() {

    //declaring variables
    private lateinit var bestTime: TextView
    private lateinit var lastTime: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var makeCustomButton: Button
    private lateinit var startGame: Button
    private lateinit var customGroup: Group
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var radioButton: RadioButton
    private lateinit var rows: EditText
    private lateinit var columns: EditText
    private lateinit var mines: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_screen)
        Log.i("dashboard", "onCreate: ")
        initView()
        initSharedPreferences()
        listeners()
    }

    //initializing views
    private fun initView() {
        bestTime = findViewById(R.id.best_time)
        lastTime = findViewById(R.id.last_time)
        radioGroup = findViewById(R.id.radioGroup)
        makeCustomButton = findViewById(R.id.Custom_board)
        startGame = findViewById(R.id.button2)
        customGroup = findViewById(R.id.group)
        rows = findViewById(R.id.editTextRows)
        columns = findViewById(R.id.editTextColumns)
        mines = findViewById(R.id.editTextMines)
    }

    //initializing shared preferences and setting values in ui
    @SuppressLint("CommitPrefEdits", "SetTextI18n")
    private fun initSharedPreferences() {
        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        bestTime.text = "${sharedPreferences.getLong("bestTime", 0)}s"
        lastTime.text = "${sharedPreferences.getLong("lastTime", 0)}s"

    }

    //initializing onclick listeners
    private fun listeners() {
        radioButton = findViewById(R.id.easy)
        radioButton.isChecked = true
        radioGroup.setOnCheckedChangeListener { _, checkid ->
            radioButton = findViewById(checkid)
        }
        makeCustomButton.setOnClickListener {
            if (customGroup.visibility == View.VISIBLE)
                customGroup.visibility = View.GONE
            else
                customGroup.visibility = View.VISIBLE
        }

        startGame.setOnClickListener {
            if (customGroup.visibility != View.VISIBLE) {
                editor.putInt(countConstants.customStatus,0)
                editor.apply()
                handleCheckBox()
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                when {
                    getNumber(rows, countConstants.ROWS) == 0 -> return@setOnClickListener
                    getNumber(columns, countConstants.COLUMNS) == 0 -> return@setOnClickListener
                    getNumber(mines, countConstants.MINES) == 0 -> return@setOnClickListener
                    else -> startActivity(Intent(this, MainActivity::class.java))
                }
                editor.putInt(countConstants.customStatus,1)
                editor.apply()

            }
        }
    }

    //method to check weather the entered value is number and other validations
    private fun getNumber(editText: EditText, key: String): Int {
        var number = 0
        try {
            number = editText.text.toString().toInt()
            if(number>15){
                editText.error = "Enter Number below 15"
                number=0
            }
            editor.putInt(key, number)
        } catch (e: Exception) {
            editText.error = "Enter Valid Number"
        }
        return number
    }

    //to get which check box is checked for difficulty level
    private fun handleCheckBox() {
        when (radioButton.text) {
            "Easy" -> {
                editor.putInt("level", MainActivity.EASY)
            }
            "Medium" -> {
                editor.putInt("level", MainActivity.MEDIUM)
            }
            else -> {
                editor.putInt("level", MainActivity.HARD)
            }
        }
        editor.apply()
    }

}