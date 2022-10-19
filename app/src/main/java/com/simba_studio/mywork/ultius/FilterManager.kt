package com.simba_studio.mywork.ultius

import com.simba_studio.mywork.model.Ad
import com.simba_studio.mywork.model.AdFilter

object FilterManager {
    fun createFilter(ad: Ad): AdFilter{
        return AdFilter(
            ad.time,
            "${ad.category}_${ad.time}",
            "${ad.category}_${ad.work}_${ad.buisnessTrip}_${ad.time}",
            "${ad.category}_${ad.work}_${ad.profile}_${ad.buisnessTrip}_${ad.time}",
            "${ad.category}_${ad.buisnessTrip}_${ad.time}",

            "${ad.work}_${ad.buisnessTrip}_${ad.time}",
            "${ad.work}_${ad.profile}_${ad.buisnessTrip}_${ad.time}",
            "${ad.buisnessTrip}_${ad.time}",
        )
    }

    fun getFilter(filter: String): String{
        val sBuilderNode = StringBuilder()
        val sBuilderFilter = StringBuilder()
        val temArray = filter.split("_")

        if(temArray[0] != "empty") {
            sBuilderNode.append("work_")
            sBuilderFilter.append("${temArray[0]}_")
        }

        if(temArray[1] != "empty") {
            sBuilderNode.append("profile_")
            sBuilderFilter.append("${temArray[1]}_")
        }

        sBuilderFilter.append(temArray[2])
        sBuilderNode.append("trip_time")
        return "$sBuilderNode | $sBuilderFilter"
    }
}