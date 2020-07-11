package com.shamanth.minesweeper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.OnLongClickListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.shamanth.minesweeper.R
import java.util.*
import kotlin.math.roundToInt


/**
 * Created by Shamanth on 26-06-2020
 */
class MainActivity : AppCompatActivity(), View.OnClickListener, OnLongClickListener {
    private var grid: Array<Array<MyButton?>?>? = null
    private var horizontalLayout: Array<LinearLayout?>? = null
    var mainLayout: LinearLayout? = null
    private var minesBoard: TextView? = null
    private var scoreBoard: TextView? = null
    private var open: MediaPlayer? = null
    private var flag: MediaPlayer? = null
    private var unflag: MediaPlayer? = null
    private var won: MediaPlayer? = null
    private var lose: MediaPlayer? = null
    private var newgame: MediaPlayer? = null
    private var message: Toast? = null
    var rotationP: MenuItem? = null
    private var sharedPreferences: SharedPreferences? = null
    private var editor: Editor? = null
    private var muted = false
    private var rotation = false
    private var valuesX: IntArray? = intArrayOf(-1, -1, 0, 1, 1, 1, 0, -1)
    private var valuesY: IntArray? = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)
    private var MINES = 0
    var xSIZE = 8
    var ySIZE = 10
    var trueFlags = 0
    var firstClick = 0
    private var flagCounter = MINES
    var visibleBlocks = 0
    var score = 0
    var isGameOver = false
    var startTime :Long = 0
    var endTime:Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences?.edit()
        val rotation = !sharedPreferences?.getBoolean("rotation", false)!!
        requestedOrientation = if (!rotation) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_USER
        }
        mainLayout = findViewById(R.id.mainLayout)
        minesBoard = findViewById(R.id.mineBoard)
        scoreBoard = findViewById(R.id.scoreBoard)
        scoreBoard?.text = "$score" + " scored"
        minesBoard?.text = "$MINES left"

        //sounds
        newgame = MediaPlayer.create(this, R.raw.newgame)
        won = MediaPlayer.create(this, R.raw.won)
        lose = MediaPlayer.create(this, R.raw.lose)
        unflag = MediaPlayer.create(this, R.raw.unflag)
        open = MediaPlayer.create(this, R.raw.open)
        flag = MediaPlayer.create(this, R.raw.flag)
        setDimensions()
        newGame()
        muted = sharedPreferences?.getBoolean("sound", false)!!
        if (!muted) newgame?.start()
    }

    @SuppressLint("SetTextI18n")

    //this function setups new Game
    private fun newGame() {
        if (!muted) newgame?.start()
        setUpBoard(xSIZE, ySIZE)
        minesBoard?.setText("$MINES left")
        setUpValues(MINES)
        flagCounter = MINES
        isGameOver = false
        visibleBlocks = 0
        trueFlags = 0
        score = 0
        scoreBoard?.text = "$score scored"
        firstClick = 0
        startTime = System.currentTimeMillis()
    }


    //This function sets the dimension of board and port
    private fun setDimensions() {
        val listener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val pWidth = mainLayout?.width
                val pHeight = mainLayout?.height
                Log.i("main Activity", "onGlobalLayout: "+pWidth+" "+pHeight)
                if(sharedPreferences?.getInt(countConstants.customStatus,0)==0) {
                    if (pHeight != null) {
                        xSIZE = pHeight / 100
                    }
                    if (pWidth != null) {
                        ySIZE = pWidth / 100
                    }
                }else{
                    xSIZE = sharedPreferences!!.getInt(countConstants.COLUMNS,25)
                    ySIZE = sharedPreferences!!.getInt(countConstants.ROWS,12)
                }
                Log.i("main Activity", "onGlobalLayout: "+xSIZE+" "+ySIZE)

                newGame()
                mainLayout?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            }
        }
        mainLayout?.viewTreeObserver?.addOnGlobalLayoutListener(listener)
    }


    //Initializes board
    private fun setUpBoard(x: Int, y: Int) {

        //sets mines percent based on difficulty level
        if(sharedPreferences?.getInt(countConstants.customStatus,0)==0) {
            when (sharedPreferences?.getInt("level", EASY)) {
                EASY -> {
                    //15 percent
                    MINES = (xSIZE * ySIZE * 15 / 100.toFloat()).roundToInt()
                }
                MEDIUM -> {
                    //22 percent
                    MINES = (xSIZE * ySIZE * 22 / 100.toFloat()).roundToInt()
                }
                HARD -> {
                    // 35 percent
                    MINES = (xSIZE * ySIZE * 35 / 100.toFloat()).roundToInt()
                }
            }
        }else{
            MINES = sharedPreferences!!.getInt(countConstants.MINES,15)
        }

        //construction of board
        grid = Array(x) { arrayOfNulls<MyButton?>(y) }
        mainLayout?.removeAllViews()
        minesBoard?.text = "$MINES left"
        horizontalLayout = arrayOfNulls<LinearLayout?>(x)
        for (i in 0 until x) {
            horizontalLayout!![i] = LinearLayout(this)
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 5f / x)
            params.setMargins(0, 0, 0, 0)
            horizontalLayout!![i]?.layoutParams = params
            horizontalLayout!![i]?.orientation = LinearLayout.HORIZONTAL
            mainLayout?.addView(horizontalLayout!![i])
        }
        Log.i("Logs", "Layout Constructed ")

        //adding mines grid and click listners
        for (i in 0 until x) {
            for (j in 0 until y) {
                grid!![i]!![j] = MyButton(this@MainActivity)
                val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                params.setMargins(1, 1, 1, 1)
                grid!![i]?.get(j)?.layoutParams = params
                horizontalLayout!![i]!!.addView(grid!![i]!![j])
                grid!![i]!![j]!!.row = i
                grid!![i]!![j]!!.col   = j
                grid!![i]!![j]?.setPadding(0, 0, 0, 2)
                grid!![i]!![j]?.textSize = 20f
                grid!![i]!![j]?.setTextColor(ContextCompat.getColor(this, R.color.textColor))
                grid!![i]!![j]?.gravity = Gravity.CENTER
                grid!![i]!![j]?.applyChanges(ButtonConstants.DEFAULT)
                grid!![i]!![j]?.setOnClickListener(this)
                grid!![i]!![j]?.setOnLongClickListener(this)
            }
        }
        Log.i("Logs", "buttons added ")
    }

    //setups the mine values for adjecent blocks
    private fun setUpValues( n: Int) {
        //adding mines
        var n = n
        var row: Int
        var col: Int
        val num = Random()
        while (n != 0) {
            row = num.nextInt(xSIZE)
            col = num.nextInt(ySIZE)
            if (grid?.get(row)?.get(col)?.value != MINED) {
                grid?.get(row)?.get(col)?.value = MINED
                n--
                for (i in valuesX?.indices!!) {
                    val tempR = row + valuesX?.get(i)!!
                    val tempC = col + valuesY?.get(i)!!
                    if (tempC < 0 || tempR < 0 || tempC >= ySIZE || tempR >= xSIZE) continue
                    if (grid!![tempR]!![tempC]!!.value == MINED) continue
                    grid!![tempR]!![tempC]!!.validFlagCounter = ++grid!![tempR]!![tempC]!!.value
                }
            }
        }
        Log.i("Logs", "mines added")
    }

    //Reveals the mines
    private fun reveal(currentButton: MyButton?) {
        if (!muted) open?.start()
        score += currentButton!!.value
        if (currentButton.value == 0) {
            currentButton.text = ""
            currentButton.applyChanges(ButtonConstants.ZERO_VALUE)
        }
        if (currentButton.value != MINED && currentButton.value != 0) {
            currentButton.text = "" + currentButton.value
            currentButton.applyChanges(ButtonConstants.SOME_VALUE)
        }
        scoreBoard?.text = "$score scored"
        currentButton.isVisible = true
        visibleBlocks++
        if (currentButton.value == 0) {
            for (i in valuesX!!.indices) {
                val tempR = currentButton.row + valuesX?.get(i)!!
                val tempC = currentButton.col + valuesY?.get(i)!!
                if (tempC < 0 || tempR < 0 || tempC >= ySIZE || tempR >= xSIZE) continue
                if (grid!![tempR]!![tempC]!!.value == MINED) continue
                if (grid!![tempR]!![tempC]!!.isFlagged) continue
                if (!grid!![tempR]!![tempC]?.isVisible!!) reveal(grid!![tempR]?.get(tempC))
            }
        }
    }

    //reveal redundent block
    private fun redundantBlocks(currentButton: MyButton?) {
        for (i in valuesX!!.indices) {
            val tempR = currentButton!!.row + valuesX!![i]
            val tempC = currentButton.col + valuesY!![i]
            if (tempC < 0 || tempR < 0 || tempC >= ySIZE || tempR >= xSIZE) continue
            if (currentButton.validFlagCounter == 0 && !currentButton.isVisible) reveal(grid?.get(tempR)?.get(tempC))
        }
    }


    //if the flag setted is true
    private fun trueFlagValuesUpdater(currentButton: MyButton?, doIt: Boolean) {
        for (i in valuesX!!.indices) {
            val tempR = currentButton!!.row + valuesX!![i]
            val tempC = currentButton.col + valuesY!![i]
            if (tempC < 0 || tempR < 0 || tempC >= ySIZE || tempR >= xSIZE) continue
            if (grid!![tempR]!![tempC]!!.value == MINED) continue
            if (doIt) grid!![tempR]!![tempC]!!.validFlagCounter-- else grid?.get(tempR)?.get(tempC)!!.validFlagCounter++
        }
    }

    override fun onClick(v: View?) {
        val currentButton = v as MyButton?

        //first click handler
        if (firstClick == 0 && currentButton!!.value == MINED) {
            firstClick++
            return
        } else firstClick++
        if (trueFlags == MINES || visibleBlocks == xSIZE * ySIZE - MINES) {
            if (!muted) won!!.start()
            message = Toast.makeText(this, "Game Won", Toast.LENGTH_SHORT)
            message?.show()
            isGameOver = true
            endTime = System.currentTimeMillis()
            val tDelta: Long = endTime - startTime
            val totalTime = tDelta/1000.0
            editor?.putLong("lastTime",totalTime.toLong())
            editor?.apply()
            val bestTime = sharedPreferences?.getLong("bestTime",0)
            if (bestTime != null) {
                if(bestTime>totalTime.toLong()) {
                    editor?.putLong("bestTime", totalTime.toLong())
                    editor?.apply()
                }
            }
        }
        if (currentButton!!.isFlagged || currentButton.isVisible) return

        //redundant revealer
        if (currentButton.isVisible) {
            redundantBlocks(currentButton)
        } else reveal(currentButton)

        //game over
        if (currentButton.value == MINED) {
            isGameOver = true
            for (i in 0 until xSIZE) {
                for (j in 0 until ySIZE) {
                    if (grid?.get(i)?.get(j)!!.isFlagged   && grid!![i]!![j]!!.value == MINED) continue
                    if (grid!![i]!![j]!!.value == MINED) {
                        grid!![i]!![j]!!.applyChanges(ButtonConstants.BOMB)
                    } else {
                        if (grid!![i]!![j]!!.isFlagged) {
                            grid!![i]!![j]?.applyChanges(ButtonConstants.FALSEFLAG)
                        }
                        if (grid!![i]!![j]!!.value == 0) {
                            grid!![i]!![j]!!.text = ""
                            grid!![i]!![j]!!.applyChanges(ButtonConstants.ZERO_VALUE)
                        } else grid!![i]!![j]?.text = "${grid!![i]!![j]!!.value}"
                    }
                    grid!![i]!![j]?.isEnabled = false
                }
            }
            if (!muted) lose?.start()
            message = Toast.makeText(this, "Game Over", Toast.LENGTH_SHORT)
            scoreBoard?.text = "Lose"
            message?.show()
        }
    }


    //on long click function to mark it as flag
    override fun onLongClick(v: View?): Boolean {
        val currentButton = v as MyButton?
        if (currentButton!!.isVisible) return false
        if (!currentButton.isFlagged) {
            if (flagCounter > 0) {
                currentButton.isFlagged = true
                currentButton.applyChanges(ButtonConstants.FLAG)
                if (!muted) flag?.start()
                flagCounter--
                if (currentButton.value == MINED) trueFlags++
            }
        } else {
            currentButton.isFlagged = false
            flagCounter++
            if (!muted) unflag?.start()
            currentButton.applyChanges(ButtonConstants.DEFAULT)
            if (currentButton.value == MINED) trueFlags--
        }
        trueFlagValuesUpdater(currentButton, currentButton.isFlagged)
        minesBoard?.text = "$flagCounter left"
        if (trueFlags == MINES || visibleBlocks == xSIZE * ySIZE - MINES) {
            message = Toast.makeText(this, "Game Won", Toast.LENGTH_SHORT)
            message?.show()
            isGameOver = true
        }
        return true
    }

    //option menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        Log.i("Log", "onPrepareOptionsMenu: ")
        val currLevel = sharedPreferences?.getInt("level", EASY)
        var level = menu?.findItem(R.id.levelEasy)
        when (currLevel) {
            EASY -> {
                level = menu?.findItem(R.id.levelEasy)
            }
            MEDIUM -> {
                level = menu?.findItem(R.id.levelMed)
            }
            HARD -> {
                level = menu?.findItem(R.id.levelHard)
            }
        }
        level?.isChecked = true
        val rotation = menu?.findItem(R.id.rotation)
        rotation?.isChecked = !sharedPreferences?.getBoolean("rotation", false)!!
        val mute = menu?.findItem(R.id.sound)
        mute?.isChecked = !sharedPreferences?.getBoolean("sound", true)!!
        return true
    }


    //menu option click listeners
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.newGame) {
            newGame()
        }
        if (id == R.id.sound) {
            muted = item.isChecked
            editor?.putBoolean("sound", muted)
            editor?.commit()
        }

        //solve with shared preference
        if (id == R.id.rotation) {
            if (item.isChecked) {
                rotation = false
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                rotation = true
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
            }
            newGame()
            editor?.putBoolean("rotationPrefs", rotation)
            editor?.commit()
        }

        when (id) {
            R.id.levelEasy -> {
                MINES = xSIZE * ySIZE * 15 / 100
                editor?.putInt("level", EASY)
                editor?.commit()
                newGame()
            }
            R.id.levelMed -> {
                MINES = xSIZE * ySIZE * 22 / 100
                editor?.putInt("level", MEDIUM)
                editor?.commit()
                newGame()
            }
            R.id.levelHard -> {
                MINES = xSIZE * ySIZE * 35 / 100
                editor?.putInt("level", HARD)
                editor?.commit()
                newGame()
            }
        }
        return true
    }

    companion object {
        const val MINED = -1
        const val EASY = 0
        const val MEDIUM = 1
        const val HARD = 2
    }
}