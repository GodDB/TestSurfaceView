package com.example.testsurfaceview

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class RunningManSurfaceView(context : Context) : SurfaceView(context), Runnable{

    private lateinit var gameThread : Thread
    @Volatile
    private var isPlaying : Boolean = false
    private var isMoving = false
    private val runSpeedPerSecond : Float = 250f
    private var manXPos : Float = 10f
    private var manYPos : Float = 10f
    private val frameWidth = 115
    private val frameHeight = 137
    private val frameCount = 8
    private var currentFrame = 0
    private var fps : Long = 0
    private var timeThisFrame : Long = 0
    private var lastFrameChangeTime : Long = 0
    private val frameLengthInMillisecond = 100

    private val bitmapRunningMan : Bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.running_man).run {
            Bitmap.createScaledBitmap(this, frameWidth * frameCount, frameHeight, false)
        }
    }

    private val frameToDraw : Rect = Rect(0, 0, frameWidth, frameHeight)
    private val whereToDraw : RectF = RectF(manXPos, manYPos, (manXPos + frameWidth), frameHeight.toFloat())



    override fun run() {
        while (isPlaying) {
            val startFrameTime = System.currentTimeMillis()
            update()
            draw()

            timeThisFrame = System.currentTimeMillis() - startFrameTime

            if(timeThisFrame >= 1) {
                fps = 1000/ timeThisFrame
            }
        }
    }

    private fun update() {
        if(isMoving) {
            manXPos += runSpeedPerSecond / fps

            if(manXPos > width) {
                manYPos += frameHeight
                manXPos = 10f
            }

            if(manYPos + frameHeight > height) {
                manYPos = 10f
            }
        }
    }

    private fun manageCurrentFrame() {
        val time = System.currentTimeMillis()

        if(isMoving) {
            if(time > lastFrameChangeTime + frameLengthInMillisecond) {
                lastFrameChangeTime = time
                currentFrame++

                if(currentFrame >= frameCount) {
                    currentFrame = 0
                }

            }
        }
        frameToDraw.left = currentFrame * frameWidth
        frameToDraw.right = frameToDraw.left + frameWidth
    }

    private fun draw() {
        if(holder.surface.isValid) {
            val canvas = holder.lockCanvas().apply {
                drawColor(Color.WHITE)
            }
            whereToDraw.set(manXPos, manYPos, manXPos + frameWidth, manYPos + frameHeight)
            manageCurrentFrame()
            // bitmap을 2번째 Rect 사이즈로 잘라서, 3번째 Rect의 위치에 위치 시킨다.
            canvas.drawBitmap(bitmapRunningMan, frameToDraw, whereToDraw, null)
            holder.unlockCanvasAndPost(canvas)

        }
    }

    fun pause() {
        isPlaying = false

        try {
            gameThread.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }
    }

    fun resume() {
        isPlaying = true
        gameThread = Thread(this)
        gameThread.start()
    }

    // event.action vs event.actionMasked
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isMoving = !isMoving
                return true
            }
        }
        return true
    }
}