import okio.BufferedSink
import okio.buffer
import okio.source
import java.io.File

interface SvgElement {
    fun writeTo(sink: BufferedSink)
}

class Svg(
    val width: Double,
    val height: Double,
    val children: List<SvgElement>
) : SvgElement {
    override fun writeTo(sink: BufferedSink) {
        sink.writeLine("<svg width=\"$width\" height=\"$height\">")
        children.forEach {
            it.writeTo(sink)
        }
        sink.writeLine("</svg>")
    }
}

class Group(
    val tx: Double = 0.0,
    val ty: Double = 0.0,
    val sx: Double = 1.0,
    val sy: Double = 1.0,
    val children: List<SvgElement>
): SvgElement {
    override fun writeTo(sink: BufferedSink) {
        val transform = buildString {
            var first = true
            if (tx != 0.0 || ty != 0.0) {
                first = false
                append("transform=\"")
                append("translate($tx $ty)")
            }
            if (sx != 1.0 || sy != 1.0) {
                if (first) {
                    first = false
                    append("transform=\"")
                } else {
                    append(" ")
                }
                append("scale($sx, $sy)")
            }
            if (!first) {
                append("\"")
            }
        }
        sink.writeLine("<g $transform>")
        children.forEach {
            it.writeTo(sink)
        }
        sink.writeLine("</g>")

    }
}

class SvgText(val x: Double, val y: Double, val text: String): SvgElement {
    override fun writeTo(sink: BufferedSink) {
        sink.writeLine("""
            <text x="$x" y="$y">$text</text>
        """.trimIndent())
    }
}
class SvgRect(
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
    val strokeColor: String? = null,
    val fillColor: String? = null
    ): SvgElement {
    override fun writeTo(sink: BufferedSink) {
        val stroke = strokeColor ?: "none"
        val fill = fillColor ?: "none"
        sink.writeLine("""
            <rect fill="$fill" stroke="$stroke" stroke-width="1" x="$x" y="$y" width="$w" height="$h"/>
        """.trimIndent())
    }
}

class SvgImage(
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
    val path: String,
    val extra: String,
): SvgElement {
    override fun writeTo(sink: BufferedSink) {
        val base64 = File(path).source().buffer().readByteString().base64()
        sink.writeLine("""
            <image x="$x" y="$y" width="$w" height="$h" $extra xlink:href="data:image/jpeg;base64,$base64"/>
        """.trimIndent())
    }
}

class SvgClipPath(
    val id: String,
    val child: SvgElement
): SvgElement {
    override fun writeTo(sink: BufferedSink) {
        sink.writeLine("""<clipPath id="$id">""")
        child.writeTo(sink)
        sink.writeLine("""</clipPath>""")
    }
}

class SvgCircle(
    val cx: Double,
    val cy: Double,
    val r: Double
): SvgElement {
    override fun writeTo(sink: BufferedSink) {
        sink.writeLine("""<circle cx="$cx" cy="$cy" r="$r"/>""")
    }
}

fun BufferedSink.writeLine(str: String) {
  writeUtf8(str)
  writeUtf8("\n")
}