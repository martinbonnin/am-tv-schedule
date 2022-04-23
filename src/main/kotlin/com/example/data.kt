package com.example

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.time.OffsetDateTime

private val client = OkHttpClient()
private val json = Json {
    ignoreUnknownKeys = true
}

private fun <T : Any> getResource(resourceName: String, block: (InputStream) -> T): T {
    return File("/Users/mbonnin/git/android-makers-2022/data/database/$resourceName")
        .inputStream()
        .use {
            block(it)
        }
}

val schedule = getResource("schedule-app.json") {
    json.decodeFromStream<JsonScheduleData>(it)
}

val sessions = getResource("sessions.json") {
    json.decodeFromStream<JsonSessionData>(it)
}

val speakers = getResource("speakers.json") {
    json.decodeFromStream<JsonSpeakerData>(it)
}

val talks: List<TVTalk> =  schedule.slots.all.mapNotNull { slot ->
    val sessionId = slot.sessionId
    val session = sessions.entries.firstOrNull { it.key == sessionId }?.value

    if (session == null) {
        println("No session found for $sessionId")
        return@mapNotNull null
    }

    TVTalk(
        title = session.title,
        roomId = slot.roomId,
        id = sessionId,
        speakers = session.speakers.map { speakerId ->
            val speaker = speakers[speakerId]!!
            TVSpeaker(
                name = speaker.name,
                photoUrl = speaker.photoUrl,
                id = speakerId
            )
        },
        startEpochSecond = OffsetDateTime.parse(slot.startDate).toEpochSecond(),
        endEpochSecond =  OffsetDateTime.parse(slot.endDate).toEpochSecond(),
    )
}

class TVTalk(
    val title: String,
    val roomId: String,
    val id: String,
    val speakers: List<TVSpeaker>,
    val startEpochSecond: Long,
    val endEpochSecond: Long
)

class TVSpeaker(val id: String, val name: String, val photoUrl: String)