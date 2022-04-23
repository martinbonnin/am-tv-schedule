package com.example

import Svg
import SvgCircle
import SvgClipPath
import SvgElement
import SvgImage
import SvgRect
import SvgText
import okio.buffer
import okio.sink
import java.io.File
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

private val rooms = talks.map { it.roomId }.filter { it != "all" }.distinct().sortedBy {
    when (it) {
        "Moebius" -> 0
        "Blin" -> 1
        "2.02" -> 2
        "2.04" -> 3
        else -> 4
    }
}

fun main() {
    val talks = talks

    val dayTalks = talks.filter { it.id != "party" }.groupBy {
        Instant.ofEpochSecond(it.startEpochSecond).atOffset(ZoneOffset.ofHours(2)).dayOfMonth
    }.entries.sortedBy { it.key }.map { it.value }


    var w = 1920.0
    var h = 1080.0

    val background = SvgRect(
        x = 0.0,
        y = 0.0,
        w = w.toDouble(),
        h = h.toDouble(),
        fillColor = "#FFFFFF"
    )

    val padding = 25.0
    val svg = Svg(
        width = w,
        height = h,
        children = listOf(background)
                + drawDay(dayTalks[0], padding, padding, w / 2 - 2 * padding, h - 2 * padding)
                + drawDay(dayTalks[1], w / 2 + padding, padding, w / 2 - 2 * padding, h - 2 * padding)
    )

    File("/Users/mbonnin/Downloads/tv.svg").sink().buffer().use {
        svg.writeTo(it)
    }

}

val timeWidth = 50.0
fun drawDay(dayTalks: List<TVTalk>, x: Double, y: Double, w: Double, h: Double): List<SvgElement> {
    return drawTimes(dayTalks, x, y, timeWidth, h) + drawGrid(dayTalks, x + timeWidth, y, w - timeWidth, h)
}


fun drawTimes(dayTalks: List<TVTalk>, x: Double, y: Double, w: Double, h: Double): List<SvgElement> {
    return dayTalks.map { it.startEpochSecond }.distinct().flatMap { epochSecond ->
        val minuteSinceMidnight = epochSecond.minuteSinceMidnight()

        val a = normalize(compressY(minuteSinceMidnight.toDouble() - dayStartMinutes))
        listOf(
            SvgText(
                x = x,
                y = y + a * h + 10,
                text = String.format("%02d", minuteSinceMidnight / 60)
            ),
            SvgText(
                x = x + timeWidth / 2,
                y = y + a * h + 10,
                text = String.format("%02d", minuteSinceMidnight % 60)
            ),
        )
    }
}

fun drawGrid(dayTalks: List<TVTalk>, x: Double, y: Double, w: Double, h: Double): List<SvgElement> {
    val rect = mutableMapOf<String, Rect>()
    dayTalks.forEach { talk ->
        val x = when (talk.roomId) {
            "all" -> 0.0
            else -> rooms.indexOf(talk.roomId).toDouble() / rooms.size
        }
        val w = when (talk.roomId) {
            "all" -> 1.0
            else -> 1.0 / rooms.size
        }
        val y = talk.yMinute()
        val h = talk.hMinute()
        rect[talk.id + talk.startEpochSecond] = Rect(
            x = x,
            y = y,
            w = w,
            h = h,
        )
    }

    val normalizedRect = rect.mapValues {
        it.value.copy(
            y = compressY(it.value.y),
            h = compressH(it.value.y, it.value.y + it.value.h)
        )
    }.mapValues {
        it.value.copy(
            y = normalize(it.value.y),
            h = normalize(it.value.h),
        )
    }

    val svgRects = mutableListOf<SvgRect>()
    val svgTexts = mutableListOf<SvgText>()
    val svgImages = mutableListOf<SvgElement>()
    dayTalks.map {
        val rect = normalizedRect[it.id + it.startEpochSecond]!!
        svgRects.add(
            SvgRect(
                x = x + rect.x * w,
                y = y + rect.y * h,
                w = rect.w * w,
                h = rect.h * h,
                strokeColor = "#636363"
            )
        )
        svgTexts.add(
            SvgText(
                x = x + rect.x * w,
                y = y + rect.y * h + 10,
                text = it.title
            )
        )

        it.speakers.forEachIndexed { i, speaker ->
            val side = 30.0
            val x = x + rect.x * w + side * i
            val y = y + rect.y * h + side
            svgImages.add(
                SvgClipPath(
                    id = speaker.id,
                    child = SvgCircle(
                        cx = x + side/2,
                        cy = y + side/2,
                        r = side/2
                    )
                )
            )
            svgImages.add(
                SvgImage(
                    x = x,
                    y = y,
                    w = side,
                    h = side,
                    path = "/Users/mbonnin/git/android-makers-2022/${speaker.photoUrl}",
                    extra = """clip-path="url(#${speaker.id})" """
                )
            )
        }
    }

    return svgRects + svgTexts + svgImages
}

private val dayStartMinutes = 8 * 60
private val dayEndMinutes = 18 * 60 + 5
private val openingCompression = 30
private val lunchCompression = 90
private val coffeeCompresion = 15

private fun compressY(minute: Double): Double {
    var ret = minute
    if (minute >= 30) {
        // Gates open
        ret -= openingCompression
    }
    if (minute >= 4 * 60 + 30) {
        // Lunch
        ret -= lunchCompression
    }
    if (minute > 7 * 60 + 45) {
        // Lunch
        ret -= coffeeCompresion
    }
    return ret
}

private fun normalize(minute: Double): Double {
    val compressedDayMinutes =
        dayEndMinutes - dayStartMinutes - openingCompression - lunchCompression - coffeeCompresion
    return minute / compressedDayMinutes
}

private fun compressH(start: Double, end: Double): Double {
    return compressY(end) - compressY(start)
}

fun Long.minuteSinceMidnight(): Int {
    return Instant.ofEpochSecond(this).atOffset(ZoneOffset.ofHours(2)).let {
        it.hour * 60 + it.minute
    }
}

fun TVTalk.yMinute(): Double {
    return startEpochSecond.minuteSinceMidnight().toDouble() - dayStartMinutes
}

fun TVTalk.hMinute(): Double {
    return (endEpochSecond.minuteSinceMidnight() - startEpochSecond.minuteSinceMidnight()).toDouble()
}

data class Rect(val x: Double, val y: Double, val w: Double, val h: Double)
