package com.simba_studio.mywork.model

import java.io.Serializable

data class Ad(
    val work: String? = null,
    val profile: String? = null,
    val tel: String? = null,
    val alternativeCommunication: String? = null,
    val buisnessTrip: String? = null,
    val category: String? = null,
    val title: String? = null,
    val price: String? = null,
    val description: String? = null,
    val email: String? = null,
    val mainImage: String = "empty",
    val image2: String = "empty",
    val image3: String = "empty",
    val key: String? = null,
    var favCounter: String = "0",
    val uid: String? = null,
    val time: String = "0",

    var isFav: Boolean = false,

    var viewsCounter: String = "0",
    var emailsCounter: String? = "0",
    var callsCounter: String? = "0"
): Serializable
