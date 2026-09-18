package com.example.klimata

import com.example.klimata.ui.theme.DiurnalPhase
import com.example.klimata.ui.theme.currentDiurnalPhase
import com.example.klimata.ui.theme.dynamicDiurnalColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiurnalProgressionTest {

    @Test
    fun currentDiurnalPhase_classifiesFullDayCorrectly() {
        // Night / Dawn
        assertEquals(DiurnalPhase.NIGHT, currentDiurnalPhase(0))     // 00:00
        assertEquals(DiurnalPhase.NIGHT, currentDiurnalPhase(300))   // 05:00
        assertEquals(DiurnalPhase.NIGHT, currentDiurnalPhase(419))   // 06:59

        // Daylight
        assertEquals(DiurnalPhase.DAY, currentDiurnalPhase(420))     // 07:00
        assertEquals(DiurnalPhase.DAY, currentDiurnalPhase(720))     // 12:00
        assertEquals(DiurnalPhase.DAY, currentDiurnalPhase(989))     // 16:29

        // Sunset / Evening
        assertEquals(DiurnalPhase.EVENING, currentDiurnalPhase(990))  // 16:30
        assertEquals(DiurnalPhase.EVENING, currentDiurnalPhase(1065)) // 17:45
        assertEquals(DiurnalPhase.EVENING, currentDiurnalPhase(1139)) // 18:59

        // Night
        assertEquals(DiurnalPhase.NIGHT, currentDiurnalPhase(1140))   // 19:00
        assertEquals(DiurnalPhase.NIGHT, currentDiurnalPhase(1320))   // 22:00
        assertEquals(DiurnalPhase.NIGHT, currentDiurnalPhase(1439))   // 23:59
    }

    @Test
    fun dynamicDiurnalColors_interpolatesContinuouslyAcrossAll24Hours() {
        // Verify every minute of the day generates valid non-null tokens without throwing
        for (minute in 0 until 1440) {
            val colors = dynamicDiurnalColors(minute)
            assertNotNull(colors)
            assertNotNull(colors.skyGradient)
            assertNotNull(colors.deckSurface)
            assertNotNull(colors.accentColor)
            assertNotNull(colors.textPrimary)
        }
    }

    @Test
    fun dynamicDiurnalColors_respectsNocturnalVisualFlags() {
        val midnight = dynamicDiurnalColors(0)
        assertTrue(midnight.showsStars)

        val noon = dynamicDiurnalColors(720)
        assertFalse(noon.showsStars)
    }

    @Test
    fun dynamicDiurnalColors_smoothlyWrapsAroundMidnight() {
        val at2359 = dynamicDiurnalColors(1439)
        val at0001 = dynamicDiurnalColors(1)

        // Text primary and surfaces should be nearly identical across 00:00 midnight boundary
        assertEquals(at2359.textPrimary.red, at0001.textPrimary.red, 0.05f)
        assertEquals(at2359.deckSurface.red, at0001.deckSurface.red, 0.05f)
    }
}
