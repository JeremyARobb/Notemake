package com.example.notemake.Displays.EditWordsDisplay

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.notemake.R
import java.io.IOException

class Translator {

    fun englishToVietnamese655(context: Context, sharedPref: SharedPreferences) {
        try {
            val inputStream = context.resources.openRawResource(R.raw.vietnamese)
            inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val (english, vietnamese) = line.split("|").map { it.trim() }
                    with(sharedPref.edit()) {
                        putString("$english|$vietnamese", "$english|$vietnamese")
                        apply()
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("Translator", "Error reading vietnamese.txt", e)
        }
    }

    fun englishToSpanish655(context: Context, sharedPref: SharedPreferences) {
        val editor = sharedPref.edit()
        val inputStream = context.resources.openRawResource(R.raw.spanish)
        inputStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if(line.contains("|"))
                {
                    val parts = line.split("|").map { it.trim() }
                    if (parts.size == 2) {
                        val englishWord = parts[0]
                        val spanishWord = parts[1]
                        editor.putString(englishWord, spanishWord)
                    }
                }
            }
        }
        editor.apply()
    }
}