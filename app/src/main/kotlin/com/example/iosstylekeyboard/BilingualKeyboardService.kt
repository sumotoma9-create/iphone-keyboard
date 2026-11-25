package com.example.iosstylekeyboard

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.util.TypedValue

class BilingualKeyboardService : InputMethodService() {
    private var isArabic = false
    private var isShifted = false
    private var isSymbols = false
    private var isMoreSymbols = false
    private var soundsEnabled = true
    private lateinit var root: LinearLayout

    private lateinit var soundPool: SoundPool
    private var keySound = 0
    private var deleteSound = 0
    private var spaceSound = 0
    private var enterSound = 0

    private val englishRows = listOf(
        listOf("q","w","e","r","t","y","u","i","o","p"),
        listOf("a","s","d","f","g","h","j","k","l"),
        listOf("shift","z","x","c","v","b","n","m","delete")
    )
    private val arabicRows = listOf(
        listOf("ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج"),
        listOf("ش","س","ي","ب","ل","ا","ت","ن","م","ك"),
        listOf("shift","ئ","ء","ؤ","ر","لا","ى","ة","و","delete")
    )
    private val symbolRows = listOf(
        listOf("1","2","3","4","5","6","7","8","9","0"),
        listOf("@","#","$","%","&","-","+","(",")","/"),
        listOf("more","=","*","\"","'",":",";","!","?","delete")
    )
    private val moreSymbolRows = listOf(
        listOf("[","]","{","}","#","%","^","*","+","="),
        listOf("_","\\","|","~","<",">","€","£","¥","•"),
        listOf("123","…","—","·",",",".","؟","!","delete")
    )

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("keyboard_settings", MODE_PRIVATE)
        soundsEnabled = prefs.getBoolean("sounds_enabled", true)

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attributes)
            .build()
        keySound = soundPool.load(this, R.raw.key_click, 1)
        deleteSound = soundPool.load(this, R.raw.delete_click, 1)
        spaceSound = soundPool.load(this, R.raw.space_click, 1)
        enterSound = soundPool.load(this, R.raw.enter_click, 1)
    }

    override fun onDestroy() {
        if (::soundPool.isInitialized) soundPool.release()
        super.onDestroy()
    }

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(3), dp(6), dp(3), dp(7))
            setBackgroundColor(Color.rgb(29, 30, 33))
        }
        drawKeyboard()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        drawKeyboard()
    }

    private fun drawKeyboard() {
        if (!::root.isInitialized) return
        root.removeAllViews()

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), 0, dp(8), 0)
        }
        val brand = TextView(this).apply {
            text = "iOS Style"
            setTextColor(Color.rgb(190, 192, 198))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            gravity = Gravity.CENTER_VERTICAL
        }
        toolbar.addView(brand, LinearLayout.LayoutParams(0, dp(28), 1f))
        val soundToggle = TextView(this).apply {
            text = if (soundsEnabled) "♪ Sound on" else "♪ Sound off"
            setTextColor(Color.rgb(210, 212, 218))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            gravity = Gravity.CENTER
            setPadding(dp(10), 0, dp(10), 0)
            setOnClickListener {
                soundsEnabled = !soundsEnabled
                getSharedPreferences("keyboard_settings", MODE_PRIVATE)
                    .edit().putBoolean("sounds_enabled", soundsEnabled).apply()
                drawKeyboard()
            }
        }
        toolbar.addView(soundToggle, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, dp(28)
        ))
        root.addView(toolbar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(30)
        ))

        val rows = when {
            !isSymbols && isArabic -> arabicRows
            !isSymbols -> englishRows
            isMoreSymbols -> moreSymbolRows
            else -> symbolRows
        }

        rows.forEach { keys ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            keys.forEach { key ->
                val special = key in listOf("shift", "delete", "more", "123")
                val label = when (key) {
                    "shift" -> "⇧"
                    "delete" -> "⌫"
                    "more" -> "﹤"
                    else -> if (!isArabic && !isSymbols && isShifted && key.length == 1) key.uppercase() else key
                }
                val weight = when (key) {
                    "shift", "delete" -> 1.45f
                    "more", "123" -> 1.35f
                    else -> 1f
                }
                val button = makeKey(label, special)
                button.setOnClickListener { onKeyPressed(key) }
                row.addView(button, LinearLayout.LayoutParams(0, dp(42), weight).apply {
                    setMargins(dp(2), dp(3), dp(2), dp(3))
                })
            }
            root.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)
            ))
        }
        drawBottomRow()
    }

    private fun drawBottomRow() {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        addBottomKey(row, if (isSymbols) "ABC" else "123", 1.35f, true) {
            play(keySound)
            if (isSymbols) {
                isSymbols = false
                isMoreSymbols = false
            } else {
                isSymbols = true
            }
            drawKeyboard()
        }
        addBottomKey(row, "😊", 1.1f, true) {
            play(keySound)
            currentInputConnection?.commitText("😊", 1)
        }
        addBottomKey(row, "🌐", 1.1f, true) {
            play(keySound)
            isArabic = !isArabic
            isSymbols = false
            isMoreSymbols = false
            isShifted = false
            drawKeyboard()
        }
        addBottomKey(row, if (isArabic) "،" else ",", 0.85f, true) {
            play(keySound)
            currentInputConnection?.commitText(if (isArabic) "،" else ",", 1)
        }
        addBottomKey(row, if (isArabic) "مسافة" else "English", 3.7f, false) {
            play(spaceSound)
            currentInputConnection?.commitText(" ", 1)
        }
        addBottomKey(row, if (isArabic) "؟" else ".", 0.85f, true) {
            play(keySound)
            currentInputConnection?.commitText(if (isArabic) "؟" else ".", 1)
        }
        addBottomKey(row, "↵", 1.25f, true) { performEnter() }
        root.addView(row, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(49)
        ))
    }

    private fun addBottomKey(
        row: LinearLayout, label: String, weight: Float, secondary: Boolean, action: () -> Unit
    ) {
        val view = makeKey(label, secondary, if (label == "English" || label == "مسافة") 13f else 18f)
        view.setOnClickListener { action() }
        row.addView(view, LinearLayout.LayoutParams(0, dp(42), weight).apply {
            setMargins(dp(2), dp(3), dp(2), dp(2))
        })
    }

    private fun makeKey(label: String, secondary: Boolean, size: Float = 20f): TextView {
        return TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            setTextColor(if (secondary) Color.rgb(245, 245, 247) else Color.WHITE)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            background = rounded(
                if (secondary) Color.rgb(67, 69, 74) else Color.rgb(91, 93, 98),
                dp(6)
            )
            elevation = if (secondary) 0f else dp(1).toFloat()
            isClickable = true
            isFocusable = true
        }
    }

    private fun onKeyPressed(key: String) {
        val connection = currentInputConnection ?: return
        when (key) {
            "shift" -> {
                play(keySound)
                isShifted = !isShifted
                drawKeyboard()
            }
            "delete" -> {
                play(deleteSound)
                connection.deleteSurroundingText(1, 0)
            }
            "more" -> {
                play(keySound)
                isMoreSymbols = true
                drawKeyboard()
            }
            "123" -> {
                play(keySound)
                isMoreSymbols = false
                drawKeyboard()
            }
            else -> {
                play(keySound)
                val value = if (!isArabic && !isSymbols && isShifted && key.length == 1) key.uppercase() else key
                connection.commitText(value, 1)
                if (isShifted && !isArabic && !isSymbols) {
                    isShifted = false
                    drawKeyboard()
                }
            }
        }
    }

    private fun performEnter() {
        val connection = currentInputConnection ?: return
        play(enterSound)
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        if (action != null && action != EditorInfo.IME_ACTION_NONE &&
            action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            connection.performEditorAction(action)
        } else {
            connection.commitText("\n", 1)
        }
    }

    private fun play(soundId: Int) {
        if (soundsEnabled && soundId != 0 && ::soundPool.isInitialized) {
            soundPool.play(soundId, 0.55f, 0.55f, 1, 0, 1.0f)
        }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
