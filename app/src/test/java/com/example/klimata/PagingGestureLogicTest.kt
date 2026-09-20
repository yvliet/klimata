package com.example.klimata

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.abs

class PagingGestureLogicTest {

    private fun computeTargetPage(
        currentPage: Int,
        pageCount: Int,
        currentPageOffsetFraction: Float,
        accumulatedDrag: Float,
        swipeThresholdPx: Float
    ): Int {
        val targetPage = if (accumulatedDrag < -swipeThresholdPx && currentPage < pageCount - 1) {
            if (currentPageOffsetFraction >= 0f) currentPage + 1 else currentPage
        } else if (accumulatedDrag > swipeThresholdPx && currentPage > 0) {
            if (currentPageOffsetFraction <= 0f) currentPage - 1 else currentPage
        } else {
            currentPage
        }
        return targetPage.coerceIn(0, pageCount - 1)
    }

    private fun computeTempDynamics(
        isDragging: Boolean,
        offsetFraction: Float
    ): Pair<Float, Float> {
        val dragTransitionProgress = (abs(offsetFraction) / 0.5f).coerceIn(0f, 1f)
        val scale = if (isDragging) {
            (1f - dragTransitionProgress * 0.25f).coerceIn(0.75f, 1f)
        } else {
            1f
        }
        val alpha = if (isDragging) {
            val remaining = (1f - dragTransitionProgress).coerceIn(0f, 1f)
            remaining * remaining
        } else {
            1f
        }
        return Pair(scale, alpha)
    }

    @Test
    fun peekBelowThreshold_snapsBackToCurrentPage() {
        val threshold = 135f
        val page = computeTargetPage(
            currentPage = 0,
            pageCount = 3,
            currentPageOffsetFraction = 0.10f,
            accumulatedDrag = -50f,
            swipeThresholdPx = threshold
        )
        assertEquals(0, page)
    }

    @Test
    fun swipePastThreshold_advancesToNextPage() {
        val threshold = 135f
        val page = computeTargetPage(
            currentPage = 0,
            pageCount = 3,
            currentPageOffsetFraction = 0.25f,
            accumulatedDrag = -180f,
            swipeThresholdPx = threshold
        )
        assertEquals(1, page)
    }

    @Test
    fun swipePastMidpoint_settlesOnNewPage() {
        val threshold = 135f
        val page = computeTargetPage(
            currentPage = 1,
            pageCount = 3,
            currentPageOffsetFraction = -0.40f,
            accumulatedDrag = -400f,
            swipeThresholdPx = threshold
        )
        assertEquals(1, page)
    }

    @Test
    fun swipePreviousRoom_settlesOnPreviousPage() {
        val threshold = 135f
        val page = computeTargetPage(
            currentPage = 1,
            pageCount = 3,
            currentPageOffsetFraction = -0.20f,
            accumulatedDrag = 200f,
            swipeThresholdPx = threshold
        )
        assertEquals(0, page)
    }

    @Test
    fun swipeBeyondFirstRoom_clampsToZero() {
        val threshold = 135f
        val page = computeTargetPage(
            currentPage = 0,
            pageCount = 3,
            currentPageOffsetFraction = 0f,
            accumulatedDrag = 250f,
            swipeThresholdPx = threshold
        )
        assertEquals(0, page)
    }

    @Test
    fun swipeBeyondLastRoom_clampsToLastIndex() {
        val threshold = 135f
        val page = computeTargetPage(
            currentPage = 2,
            pageCount = 3,
            currentPageOffsetFraction = 0f,
            accumulatedDrag = -300f,
            swipeThresholdPx = threshold
        )
        assertEquals(2, page)
    }

    @Test
    fun tempDynamics_atRest_fullScaleAndAlpha() {
        val (scale, alpha) = computeTempDynamics(isDragging = false, offsetFraction = 0f)
        assertEquals(1.0f, scale, 0.001f)
        assertEquals(1.0f, alpha, 0.001f)
    }

    @Test
    fun tempDynamics_atMidpoint_reachesTargetInvisibilityAndScale() {
        val (scale, alpha) = computeTempDynamics(isDragging = true, offsetFraction = 0.5f)
        assertEquals(0.75f, scale, 0.001f)
        assertEquals(0.0f, alpha, 0.001f)
    }

    @Test
    fun tempDynamics_quarterPeek_smoothIntermediateState() {
        val (scale, alpha) = computeTempDynamics(isDragging = true, offsetFraction = 0.25f)
        assertEquals(0.875f, scale, 0.001f)
        assertEquals(0.25f, alpha, 0.001f)
    }
}
