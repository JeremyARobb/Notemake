package com.example.notemake.Displays.Helper

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class helperUtil {
    companion object {
        fun saveData(context: Context, data: MutableMap<String, List<List<String>>>) {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            val jsonObject = JSONObject()
            for ((key, value) in data) {
                val outerArray = JSONArray()
                for (innerList in value) {
                    val innerArray = JSONArray()
                    for (string in innerList) {
                        innerArray.put(string)
                    }
                    outerArray.put(innerArray)
                }
                jsonObject.put(key, outerArray)
            }

            editor.putString("myDataKey", jsonObject.toString())
            editor.apply()
        }

        fun loadData(context: Context): Map<String, List<List<String>>>? {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
            val jsonString = sharedPreferences.getString("myDataKey", null) ?: return null

            val jsonObject = JSONObject(jsonString)
            val data = mutableMapOf<String, List<List<String>>>()

            jsonObject.keys().forEach { key ->
                val outerArray = jsonObject.getJSONArray(key)
                val listOfLists = mutableListOf<List<String>>()
                for (i in 0 until outerArray.length()) {
                    val innerArray = outerArray.getJSONArray(i)
                    val innerList = mutableListOf<String>()
                    for (j in 0 until innerArray.length()) {
                        innerList.add(innerArray.getString(j))
                    }
                    listOfLists.add(innerList)
                }
                data[key] = listOfLists
            }

            return data
        }

        //this needs work still to split between commas
        fun addData(context: Context, key: String, newList: List<String>) {
            val data = loadData(context)?.toMutableMap() ?: mutableMapOf()
            val existingList = data[key]?.toMutableList() ?: mutableListOf()
            existingList.add(newList)
            data[key] = existingList
            saveData(context, data)
        }

        fun addStat(context: Context, word: String, modify: Int) {
            val data = loadData(context)?.toMutableMap() ?: mutableMapOf()
            val existingList = data[word]?.toMutableList() ?: mutableListOf()
            var listOfStats = existingList[1]
            var currentNumber = listOfStats.get(0).toInt()
            currentNumber += modify
            if (currentNumber > 10) {
                currentNumber = 10
            } else if (currentNumber < 0) {
                currentNumber = 0
            }

            val newListStat = listOf<String>(currentNumber.toString())
            val newList = mutableListOf<List<String>>()
            newList.add(existingList[0])
            newList.add(newListStat)
            data[word] = newList

            saveData(context, data)
        }
    }
}
