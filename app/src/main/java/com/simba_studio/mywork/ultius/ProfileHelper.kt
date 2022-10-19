package com.simba_studio.mywork.ultius

import java.io.IOException
import java.io.InputStream
import android.content.Context
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

object ProfileHelper {
    fun getAllSectors(context: Context):ArrayList<String>{
        val temArray = ArrayList<String>()
        try{

            val inputStream : InputStream = context.assets.open("worksToProfiles.json")
            val size:Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonFile = String(bytesArray)
            val jsonObject = JSONObject(jsonFile)
            val sectorsNames = jsonObject.names()
            if (sectorsNames != null) {
                for (n in 0 until sectorsNames.length()) {
                    temArray.add(sectorsNames.getString(n))
                }
            }

        } catch (e:IOException){

        }
        return temArray
    }

    fun getAllCities(sector : String, context: Context):ArrayList<String>{
        var temArray = ArrayList<String>()
        try{

            val inputStream : InputStream = context.assets.open("worksToProfiles.json")
            val size:Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonFile = String(bytesArray)
            val jsonObject = JSONObject(jsonFile)
            val citiesNames = jsonObject.getJSONArray(sector)

                for (n in 0 until citiesNames.length()) {
                    temArray.add(citiesNames.getString(n))
                }
        } catch (e:IOException){

        }
        return temArray
    }

    fun filterListData(list : ArrayList<String>, searchText : String?) : ArrayList<String>{
        val tempList = ArrayList<String>()
        tempList.clear()
        if(searchText == null){
            tempList.add("No result")
            return tempList
        }
        for(selection : String in list){
            if(selection.lowercase(Locale.ROOT).startsWith(searchText.lowercase(Locale.ROOT)))
                tempList.add(selection)
        }
        if(tempList.size == 0)tempList.add("No result")
        return tempList
    }
}