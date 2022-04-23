package com.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsonRoom(
    val name: String,
    val capacity: Int,
    val level: String
)

typealias JsonRoomData = Map<String, JsonRoom>

@Serializable
data class JsonSession(
    val title: String,
    val description: String,
    val language: String? = null,
    val complexity: String? = null,
    val speakers: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val icon: String? = null,
    val platformUrl: String? = null,
    val feedback: String? = null,
    val slido: String? = null,
)

typealias JsonSessionData = Map<String, JsonSession>

@Serializable
data class JsonSpeaker(
    val order: Float? = null,
    val featured: Boolean = false,
    val name: String,
    val bio: String,
    val country: String?,
    val companyLogo: String? = null,
    val company: String? = null,
    val socials: List<JsonSocial>,
    val photoUrl: String
)

typealias JsonSpeakerData = Map<String, JsonSpeaker>

@Serializable
data class JsonSocial(
    val icon: String,
    val link: String,
    val name: String
)

@Serializable
class JsonScheduleData(val slots: JsonSlots)

@Serializable
class JsonSlots(
    val all: List<JsonSlot>
)

@Serializable
data class JsonSlot(
    val endDate: String,
    val startDate: String,
    val roomId: String,
    val sessionId: String
)

@Serializable
data class JsonTimeslot(
    val startTime: String,
    val endTime: String,
    val sessions: List<JsonTimeslotSession>,
)

@Serializable
data class JsonTimeslotSession(
    val items: List<String>,
    val extend: Int? = null,
)

@Serializable
data class JsonTrack(
    val title: String,
    val infos: String,
)

typealias JsonPartnerData = Map<String, JsonPartnerGroup>

@Serializable
data class JsonPartnerGroup(
    val order: Int,
    val title: String,
    @SerialName("items-firestoreCollection")
    val items: Map<String, JsonPartner>
)

@Serializable
data class JsonPartner(
    val order: Int,
    val name: String,
    val logoUrl: String,
    val url: String
)

