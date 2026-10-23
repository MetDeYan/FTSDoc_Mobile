package com.ftsdoc.mobile.data.remote

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsResponseParserTest {
    private val gson = Gson()

    @Test
    fun parse_readsStatisticsText() {
        val raw = """
            {
              "statistics": "ООО \"Митрида\", за Октябрь 2026:\nКоличество документов — 0\nКоличество страниц — 0\nМетелица Денис, за Октябрь 2026:\nКоличество документов — 0\nКоличество страниц — 0"
            }
        """.trimIndent()

        val parsed = parseStatisticsText(raw, gson)

        assertEquals(
            StatisticsTextParse.Text(
                "ООО \"Митрида\", за Октябрь 2026:\n" +
                    "Количество документов — 0\n" +
                    "Количество страниц — 0\n" +
                    "Метелица Денис, за Октябрь 2026:\n" +
                    "Количество документов — 0\n" +
                    "Количество страниц — 0",
            ),
            parsed,
        )
    }

    @Test
    fun parse_normalizesWindowsLineBreaks() {
        val raw = """{"statistics":"Организация\r\nДокументы — 1"}"""

        assertEquals(
            StatisticsTextParse.Text("Организация\nДокументы — 1"),
            parseStatisticsText(raw, gson),
        )
    }

    @Test
    fun parse_blankStatisticsIsEmpty() {
        assertEquals(StatisticsTextParse.Empty, parseStatisticsText("""{"statistics":"  "}""", gson))
        assertEquals(StatisticsTextParse.Empty, parseStatisticsText("", gson))
    }

    @Test
    fun parse_malformedJsonIsInvalid() {
        assertEquals(StatisticsTextParse.Invalid, parseStatisticsText("{", gson))
    }
}
