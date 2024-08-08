package com.example.notemake.Displays.EditWordsDisplay

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.notemake.Displays.Helper.helperUtil
import com.example.notemake.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class Translator {

    fun englishToVietnamese655(context: Context, fileName: String) {
        try {
            var inputStream = context.resources.openRawResource(R.raw.duolingotest)
            if (fileName == "Vietnamese") {
                inputStream = context.resources.openRawResource(R.raw.vietnamese)
            }
            if (fileName == "Small Test") {
                inputStream = context.resources.openRawResource(R.raw.smallervietnamese)
            }

            val initialStat = listOf<String>("5")

            val reader = BufferedReader(InputStreamReader(inputStream))
            val firstLine = reader.readLine()
            val data = mutableMapOf<String, List<List<String>>>()

            if (firstLine.contains("|")) {
                val (firstEnglish, firstVietnamese) = firstLine.split("|").map { it.trim() }
                val firstEnglishWords = firstEnglish.split(",")
                val firstInitialList = mutableListOf<List<String>>()
                firstInitialList.add(firstEnglishWords)
                firstInitialList.add(initialStat)
                data[firstVietnamese] = firstInitialList

                reader.useLines { lines ->
                    lines.forEach { line ->
                        val (english, vietnamese) = line.split("|").map { it.trim() }
                        val englishWords = english.split(",")
                        val initialList = mutableListOf<List<String>>()
                        initialList.add(englishWords)
                        initialList.add(initialStat)
                        data[vietnamese] = initialList
                    }
                }
            }
            else {
                var vietnameseWord = firstLine
                reader.useLines { lines ->
                    lines.forEach { line ->
                        if (vietnameseWord.isEmpty()) {
                            vietnameseWord = line
                        }
                        else {
                            val englishWords = line.split(",")
                            val initialList = mutableListOf<List<String>>()
                            initialList.add(englishWords)
                            initialList.add(initialStat)
                            data[vietnameseWord] = initialList
                            vietnameseWord = ""
                        }
                    }
                }
            }
            helperUtil.saveData(context, data)

        } catch (e: IOException) {
            Log.e("Translator", "Error reading vietnamese.txt", e)
        }
    }



    fun englishToSpanish655(context: Context, sharedPref: SharedPreferences) {
        try {
            val inputStream = context.resources.openRawResource(R.raw.spanish)
            inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val (english, spanish) = line.split("|").map { it.trim() }
                    with(sharedPref.edit()) {
                        putString("$english|$spanish", "$english|$spanish")
                        apply()
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("Translator", "Error reading spanish.txt", e)
        }
    }
}