package com.example.ovagrown.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayContainer: FrameLayout? = null

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main
    )

    private var growthJob: Job? = null

    private data class PlacedFlower(
        val x: Int,
        val y: Int,
        val size: Int
    )

    private val lastBranchFlowers = mutableMapOf<String, PlacedFlower>()

    private val flowers = listOf(
        "clover_og.json",
        "peony_og.json",
        "sunny_og.json",
        "jupiter_og.json",
        "rocky_og.json"
    )

    private var flowerCount = 0
    private var targetFlowerCount = 0

    private val maxFlowers = 120

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_START_OVERLAY -> {
                showOverlay()

                val progress = intent.getFloatExtra(
                    EXTRA_PROGRESS,
                    0f
                )

                updateFlowerProgress(progress)
            }

            ACTION_UPDATE_PROGRESS -> {
                showOverlay()

                val progress = intent.getFloatExtra(
                    EXTRA_PROGRESS,
                    0f
                )

                updateFlowerProgress(progress)
            }

            ACTION_STOP_OVERLAY -> {
                hideOverlay()
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun showOverlay() {
        if (overlayContainer != null) return

        flowerCount = 0
        targetFlowerCount = 0
        lastBranchFlowers.clear()

        overlayContainer = FrameLayout(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START

        try {
            windowManager?.addView(
                overlayContainer,
                params
            )
        } catch (e: Exception) {
            Log.e(
                "OverlayService",
                "Failed to add overlay view. Check overlay permission.",
                e
            )

            overlayContainer = null
            stopSelf()
        }
    }

    private fun updateFlowerProgress(
        progress: Float
    ) {
        val safeProgress = progress.coerceIn(0f, 1f)

        targetFlowerCount = (safeProgress * maxFlowers)
            .roundToInt()
            .coerceIn(0, maxFlowers)

        growTowardTarget()
    }

    private fun growTowardTarget() {
        growthJob?.cancel()

        growthJob = serviceScope.launch {
            while (
                overlayContainer != null &&
                flowerCount < targetFlowerCount &&
                flowerCount < maxFlowers
            ) {
                addNextFlower()
                delay(120)
            }
        }
    }

    private fun addNextFlower() {
        val container = overlayContainer ?: return

        if (flowerCount >= maxFlowers) return

        val flowerIndex = flowerCount % flowers.size
        val flowerFile = flowers[flowerIndex]

        if (!assetExists(flowerFile)) {
            flowerCount++
            return
        }

        val flowerSize = (300..520).random()

        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        val position = getFlowerPosition(
            count = flowerCount,
            flowerSize = flowerSize,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )

        val flowerView = LottieAnimationView(this)

        flowerView.setFailureListener { throwable ->
            Log.e(
                "OverlayService",
                "Failed to load Lottie asset: $flowerFile",
                throwable
            )

            try {
                container.removeView(flowerView)
            } catch (_: Exception) {
            }
        }

        flowerView.setAnimation(flowerFile)
        flowerView.repeatCount = 0
        flowerView.repeatMode = LottieDrawable.RESTART
        flowerView.speed = 1.7f
        flowerView.alpha = 0.95f
        flowerView.progress = 0f

        val layoutParams = FrameLayout.LayoutParams(
            flowerSize,
            flowerSize
        )

        flowerView.x = position.first.toFloat()
        flowerView.y = position.second.toFloat()

        container.addView(
            flowerView,
            layoutParams
        )

        flowerCount++

        flowerView.addAnimatorListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(
                    animation: Animator
                ) {
                    super.onAnimationEnd(animation)

                    flowerView.removeAllAnimatorListeners()

                    flowerView.progress = getFreezeProgress(flowerFile)
                    flowerView.pauseAnimation()
                }
            }
        )

        flowerView.playAnimation()
    }

    private fun assetExists(
        fileName: String
    ): Boolean {
        return try {
            assets.open(fileName).use {
                // Asset exists.
            }

            true
        } catch (e: Exception) {
            val availableAssets = assets.list("")?.joinToString(", ") ?: "No assets found"

            Log.e(
                "OverlayService",
                "Missing asset: $fileName. Available assets: $availableAssets",
                e
            )

            false
        }
    }

    private fun getFlowerPosition(
        count: Int,
        flowerSize: Int,
        screenWidth: Int,
        screenHeight: Int
    ): Pair<Int, Int> {
        if (count < 4) {
            val corner = count

            val start = getCornerStartPosition(
                corner = corner,
                layer = 0,
                flowerSize = flowerSize,
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )

            val finalX = start.first.coerceIn(
                -flowerSize / 2,
                screenWidth - flowerSize / 2
            )

            val finalY = start.second.coerceIn(
                -flowerSize / 2,
                screenHeight - flowerSize / 2
            )

            val placedFlower = PlacedFlower(
                x = finalX,
                y = finalY,
                size = flowerSize
            )

            lastBranchFlowers[getBranchKey(corner, 0, 0)] = placedFlower
            lastBranchFlowers[getBranchKey(corner, 1, 0)] = placedFlower

            return Pair(finalX, finalY)
        }

        val adjustedCount = count - 4
        val positionsPerLayer = 40
        val layer = adjustedCount / positionsPerLayer
        val positionInLayer = adjustedCount % positionsPerLayer
        val direction = positionInLayer % 8

        val corner = when (direction) {
            0, 1 -> 0
            2, 3 -> 1
            4, 5 -> 2
            else -> 3
        }

        val branch = when (direction) {
            0, 2, 4, 6 -> 0
            else -> 1
        }

        val branchKey = getBranchKey(
            corner = corner,
            branch = branch,
            layer = layer
        )

        val previousFlower = lastBranchFlowers[branchKey]

        val rawPosition = if (previousFlower == null) {
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

        val finalX = rawPosition.first.coerceIn(
            -flowerSize / 2,
            screenWidth - flowerSize / 2
        )

        val finalY = rawPosition.second.coerceIn(
            -flowerSize / 2,
            screenHeight - flowerSize / 2
        )

        lastBranchFlowers[branchKey] = PlacedFlower(
            x = finalX,
            y = finalY,
            size = flowerSize
        )

        return Pair(finalX, finalY)
    }

    private fun getCornerStartPosition(
        corner: Int,
        layer: Int,
        flowerSize: Int,
        screenWidth: Int,
        screenHeight: Int
    ): Pair<Int, Int> {
        val bottomSafeInset = 100
        val inward = layer * 120
        val randomX = (-35..35).random()
        val randomY = (-35..35).random()

        val x = when (corner) {
            0 -> -flowerSize / 3 + inward + randomX
            1 -> screenWidth - flowerSize + flowerSize / 3 - inward + randomX
            2 -> -flowerSize / 3 + inward + randomX
            else -> screenWidth - flowerSize + flowerSize / 3 - inward + randomX
        }

        val y = when (corner) {
            0 -> -flowerSize / 3 + inward + randomY
            1 -> -flowerSize / 3 + inward + randomY
            2 -> screenHeight - flowerSize - bottomSafeInset - inward + randomY
            else -> screenHeight - flowerSize - bottomSafeInset - inward + randomY
        }

        return Pair(x, y)
    }

    private fun getOverlappingPosition(
        corner: Int,
        branch: Int,
        previousFlower: PlacedFlower,
        flowerSize: Int
    ): Pair<Int, Int> {
        val smallerFlowerSize = minOf(
            previousFlower.size,
            flowerSize
        )

        val maxAllowedOverlap = (smallerFlowerSize / 2)
            .coerceAtLeast(1)

        val overlapAmount = (smallerFlowerSize / 3)
            .coerceIn(1, maxAllowedOverlap)

        val horizontalJitter = (-25..25).random()
        val verticalJitter = (-25..25).random()

        val x: Int
        val y: Int

        if (branch == 0) {
            x = when (corner) {
                0, 2 -> previousFlower.x + previousFlower.size - overlapAmount
                else -> previousFlower.x - flowerSize + overlapAmount
            }

            y = previousFlower.y + verticalJitter
        } else {
            x = previousFlower.x + horizontalJitter

            y = when (corner) {
                0, 1 -> previousFlower.y + previousFlower.size - overlapAmount
                else -> previousFlower.y - flowerSize + overlapAmount
            }
        }

        return Pair(x, y)
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

    private fun hideOverlay() {
        growthJob?.cancel()
        growthJob = null

        val container = overlayContainer ?: return

        try {
            windowManager?.removeView(container)
        } catch (_: Exception) {
        }

        overlayContainer = null
        flowerCount = 0
        targetFlowerCount = 0
        lastBranchFlowers.clear()
    }

    override fun onDestroy() {
        super.onDestroy()

        growthJob?.cancel()
        serviceScope.cancel()

        hideOverlay()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }

    companion object {
        private const val ACTION_START_OVERLAY =
            "com.example.ovagrown.overlay.START_OVERLAY"

        private const val ACTION_UPDATE_PROGRESS =
            "com.example.ovagrown.overlay.UPDATE_PROGRESS"

        private const val ACTION_STOP_OVERLAY =
            "com.example.ovagrown.overlay.STOP_OVERLAY"

        private const val EXTRA_PROGRESS =
            "progress"

        fun start(
            context: Context,
            progress: Float
        ) {
            val intent = Intent(
                context,
                OverlayService::class.java
            ).apply {
                action = ACTION_START_OVERLAY
                putExtra(EXTRA_PROGRESS, progress)
            }

            context.startService(intent)
        }

        fun updateProgress(
            context: Context,
            progress: Float
        ) {
            val intent = Intent(
                context,
                OverlayService::class.java
            ).apply {
                action = ACTION_UPDATE_PROGRESS
                putExtra(EXTRA_PROGRESS, progress)
            }

            context.startService(intent)
        }

        fun stop(
            context: Context
        ) {
            val intent = Intent(
                context,
                OverlayService::class.java
            ).apply {
                action = ACTION_STOP_OVERLAY
            }

            context.startService(intent)
        }
    }
}

