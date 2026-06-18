package com.example.overgrown.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.example.overgrown.tracker.AppUsageStore
import com.example.overgrown.tracker.UsageTracker
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayContainer: FrameLayout? = null

    private var serviceJob: Job? = null
    private var growthJob: Job? = null

    private lateinit var usageTracker: UsageTracker

    private data class PlacedFlower(
        val x: Int,
        val y: Int,
        val size: Int,
        val fileName: String
    )

    private data class FlowerState(
        var flowerCount: Int = 0,
        val placedFlowers: MutableList<PlacedFlower> = mutableListOf(),
        val lastBranchFlowers: MutableMap<String, PlacedFlower> = mutableMapOf()
    )

    private val appFlowerStates =
        mutableMapOf<String, FlowerState>()

    private var activeAppPackage = ""

    private val flowers = listOf(
        "clover_og.json",
        "peony_og.json",
        "sunny_og.json",
        "jupiter_og.json",
        "rocky_og.json"
    )

    private val maxFlowers = 1000000

    private val prefsName =
        "overlay_growth_state"

    private val keyDate =
        "growth_date"
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            context.stopService(intent)
        }
    }
    override fun onCreate() {
        super.onCreate()

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        usageTracker =
            UsageTracker(this)

        resetIfNewDay()

        serviceJob =
            CoroutineScope(Dispatchers.Main).launch {
                while (true) {

                    resetIfNewDay()

                    AppUsageStore.resetIfNewDay(
                        this@OverlayService
                    )

                    val currentApp =
                        usageTracker.getCurrentApp()

                    if (
                        currentApp == "Unknown" ||
                        currentApp == packageName ||
                        currentApp.contains("launcher")
                    ) {
                        hideOverlay()
                    } else {
                        AppUsageStore.addSecond(
                            currentApp
                        )

                        if (
                            AppUsageStore.shouldShowOverlay(
                                context = applicationContext,
                                packageName = currentApp
                            )
                        ) {
                            val targetFlowerCount =
                                AppUsageStore.getFlowerCountForApp(
                                    context = applicationContext,
                                    packageName = currentApp
                                )

                            switchToApp(
                                currentApp
                            )

                            showOverlay(
                                targetFlowerCount
                            )
                        } else {
                            hideOverlay()
                        }
                    }

                    delay(1000)
                }
            }
    }

    private fun switchToApp(
        packageName: String
    ) {
        if (activeAppPackage == packageName) return

        hideOverlay()

        activeAppPackage =
            packageName

        appFlowerStates.getOrPut(
            packageName
        ) {
            FlowerState()
        }
    }

    private fun getActiveState(): FlowerState {
        return appFlowerStates.getOrPut(
            activeAppPackage
        ) {
            FlowerState()
        }
    }

    private fun showOverlay(
        targetFlowerCount: Int
    ) {
        if (overlayContainer != null) {
            startFlowerLoop(
                targetFlowerCount
            )
            return
        }

        overlayContainer =
            FrameLayout(this)

        val params =
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            )

        params.gravity =
            Gravity.TOP or Gravity.START

        windowManager?.addView(
            overlayContainer,
            params
        )

        restoreExistingFlowers()

        startFlowerLoop(
            targetFlowerCount
        )
    }

    private fun restoreExistingFlowers() {
        val container =
            overlayContainer ?: return

        val state =
            getActiveState()

        container.removeAllViews()

        state.placedFlowers.forEach { flower ->

            val flowerView =
                LottieAnimationView(this).apply {
                    setAnimation(flower.fileName)
                    repeatCount = 0
                    repeatMode = LottieDrawable.RESTART
                    alpha = 0.95f
                    progress =
                        getFreezeProgress(flower.fileName)
                    pauseAnimation()
                }

            val layoutParams =
                FrameLayout.LayoutParams(
                    flower.size,
                    flower.size
                )

            flowerView.x =
                flower.x.toFloat()

            flowerView.y =
                flower.y.toFloat()

            container.addView(
                flowerView,
                layoutParams
            )
        }
    }

    private fun startFlowerLoop(
        targetFlowerCount: Int
    ) {
        growthJob?.cancel()

        growthJob =
            CoroutineScope(Dispatchers.Main).launch {

                while (overlayContainer != null) {

                    val state =
                        getActiveState()

                    if (
                        state.flowerCount < targetFlowerCount &&
                        state.flowerCount < maxFlowers
                    ) {
                        addNextFlower()
                    }

                    delay(500)
                }
            }
    }

    private fun addNextFlower() {
        val container =
            overlayContainer ?: return

        val state =
            getActiveState()

        if (state.flowerCount >= maxFlowers) return

        val flowerIndex =
            state.flowerCount % flowers.size

        val flowerFile =
            flowers[flowerIndex]

        val flowerSize =
            (300..520).random()

        val screenWidth =
            resources.displayMetrics.widthPixels

        val screenHeight =
            resources.displayMetrics.heightPixels

        val position =
            getFlowerPosition(
                count = state.flowerCount,
                flowerSize = flowerSize,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                flowerFile = flowerFile
            )

        val placedFlower =
            PlacedFlower(
                x = position.first,
                y = position.second,
                size = flowerSize,
                fileName = flowerFile
            )

        state.placedFlowers.add(
            placedFlower
        )

        val flowerView =
            LottieAnimationView(this).apply {
                setAnimation(flowerFile)
                repeatCount = 0
                repeatMode = LottieDrawable.RESTART
                speed = 1.7f
                alpha = 0.95f
                progress = 0f
            }

        val layoutParams =
            FrameLayout.LayoutParams(
                flowerSize,
                flowerSize
            )

        flowerView.x =
            position.first.toFloat()

        flowerView.y =
            position.second.toFloat()

        container.addView(
            flowerView,
            layoutParams
        )

        state.flowerCount++

        flowerView.addAnimatorListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(
                    animation: Animator
                ) {
                    super.onAnimationEnd(animation)

                    flowerView.removeAllAnimatorListeners()

                    flowerView.progress =
                        getFreezeProgress(flowerFile)

                    flowerView.pauseAnimation()


                }
            }
        )

        flowerView.playAnimation()
    }

    private fun getFlowerPosition(
        count: Int,
        flowerSize: Int,
        screenWidth: Int,
        screenHeight: Int,
        flowerFile: String
    ): Pair<Int, Int> {

        val state =
            getActiveState()

        if (count < 4) {
            val corner =
                count

            val start =
                getCornerStartPosition(
                    corner = corner,
                    layer = 0,
                    flowerSize = flowerSize,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight
                )

            val finalX =
                start.first.coerceIn(
                    -flowerSize / 2,
                    screenWidth - flowerSize / 2
                )

            val finalY =
                start.second.coerceIn(
                    -flowerSize / 2,
                    screenHeight - flowerSize / 2
                )

            val placedFlower =
                PlacedFlower(
                    x = finalX,
                    y = finalY,
                    size = flowerSize,
                    fileName = flowerFile
                )

            state.lastBranchFlowers[
                getBranchKey(
                    corner = corner,
                    branch = 0,
                    layer = 0
                )
            ] = placedFlower

            state.lastBranchFlowers[
                getBranchKey(
                    corner = corner,
                    branch = 1,
                    layer = 0
                )
            ] = placedFlower

            return Pair(
                finalX,
                finalY
            )
        }

        val adjustedCount =
            count - 4

        val positionsPerLayer =
            40

        val layer =
            adjustedCount / positionsPerLayer

        val positionInLayer =
            adjustedCount % positionsPerLayer

        val direction =
            positionInLayer % 8

        val corner =
            when (direction) {
                0, 1 -> 0
                2, 3 -> 1
                4, 5 -> 2
                else -> 3
            }

        val branch =
            when (direction) {
                0, 2, 4, 6 -> 0
                else -> 1
            }

        val branchKey =
            getBranchKey(
                corner = corner,
                branch = branch,
                layer = layer
            )

        val previousFlower =
            state.lastBranchFlowers[branchKey]

        val rawPosition =
            if (previousFlower == null) {
                getCornerStartPosition(
                    corner = corner,
                    layer = layer,
                    flowerSize = flowerSize,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight
                )
            } else {
                getOverlappingPosition(
                    corner = corner,
                    branch = branch,
                    previousFlower = previousFlower,
                    flowerSize = flowerSize
                )
            }

        val finalX =
            rawPosition.first.coerceIn(
                -flowerSize / 2,
                screenWidth - flowerSize / 2
            )

        val finalY =
            rawPosition.second.coerceIn(
                -flowerSize / 2,
                screenHeight - flowerSize / 2
            )

        state.lastBranchFlowers[branchKey] =
            PlacedFlower(
                x = finalX,
                y = finalY,
                size = flowerSize,
                fileName = flowerFile
            )

        return Pair(
            finalX,
            finalY
        )
    }

    private fun getCornerStartPosition(
        corner: Int,
        layer: Int,
        flowerSize: Int,
        screenWidth: Int,
        screenHeight: Int
    ): Pair<Int, Int> {

        val bottomSafeInset =
            100

        val inward =
            layer * 120

        val randomX =
            (-35..35).random()

        val randomY =
            (-35..35).random()

        val x =
            when (corner) {
                0 -> -flowerSize / 3 +
                        inward +
                        randomX

                1 -> screenWidth -
                        flowerSize +
                        flowerSize / 3 -
                        inward +
                        randomX

                2 -> -flowerSize / 3 +
                        inward +
                        randomX

                else -> screenWidth -
                        flowerSize +
                        flowerSize / 3 -
                        inward +
                        randomX
            }

        val y =
            when (corner) {
                0 -> -flowerSize / 3 +
                        inward +
                        randomY

                1 -> -flowerSize / 3 +
                        inward +
                        randomY

                2 -> screenHeight -
                        flowerSize -
                        bottomSafeInset -
                        inward +
                        randomY

                else -> screenHeight -
                        flowerSize -
                        bottomSafeInset -
                        inward +
                        randomY
            }

        return Pair(
            x,
            y
        )
    }

    private fun getOverlappingPosition(
        corner: Int,
        branch: Int,
        previousFlower: PlacedFlower,
        flowerSize: Int
    ): Pair<Int, Int> {

        val smallerFlowerSize =
            minOf(
                previousFlower.size,
                flowerSize
            )

        val maxAllowedOverlap =
            (smallerFlowerSize / 2)
                .coerceAtLeast(1)

        val overlapAmount =
            (smallerFlowerSize / 3)
                .coerceIn(
                    1,
                    maxAllowedOverlap
                )

        val horizontalJitter =
            (-25..25).random()

        val verticalJitter =
            (-25..25).random()

        val x: Int
        val y: Int

        if (branch == 0) {
            x =
                when (corner) {
                    0, 2 -> previousFlower.x +
                            previousFlower.size -
                            overlapAmount

                    else -> previousFlower.x -
                            flowerSize +
                            overlapAmount
                }

            y =
                previousFlower.y +
                        verticalJitter

        } else {
            x =
                previousFlower.x +
                        horizontalJitter

            y =
                when (corner) {
                    0, 1 -> previousFlower.y +
                            previousFlower.size -
                            overlapAmount

                    else -> previousFlower.y -
                            flowerSize +
                            overlapAmount
                }
        }

        return Pair(
            x,
            y
        )
    }

    private fun getBranchKey(
        corner: Int,
        branch: Int,
        layer: Int
    ): String {
        return "$corner-$branch-$layer"
    }

    private fun getFreezeProgress(
        fileName: String
    ): Float {
        return when (fileName) {
            "jupiter_og.json" -> 0.75f
            "rocky_og.json" -> 0.85f
            else -> 0.9f
        }
    }

    private fun getDelayAfterFlower(
        flowerIndex: Int
    ): Long {
        return when (flowerIndex) {
            3 -> 250L
            4 -> 250L
            else -> 700L
        }
    }

    private fun hideOverlay() {
        growthJob?.cancel()
        growthJob = null

        if (overlayContainer == null) return

        windowManager?.removeView(
            overlayContainer
        )

        overlayContainer = null
    }

    private fun resetIfNewDay() {
        val prefs =
            getSharedPreferences(
                prefsName,
                Context.MODE_PRIVATE
            )

        val today =
            getTodayString()

        val savedDate =
            prefs.getString(
                keyDate,
                null
            )

        if (savedDate != today) {
            appFlowerStates.clear()
            activeAppPackage = ""

            prefs.edit()
                .putString(
                    keyDate,
                    today
                )
                .apply()
        }
    }

    private fun getTodayString(): String {
        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(
            Date()
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceJob?.cancel()
        growthJob?.cancel()

        hideOverlay()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }
}
