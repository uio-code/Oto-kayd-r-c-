package com.opensource.autoswiper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import kotlinx.coroutines.*

/**
 * Bu sınıf mimarinin kalbidir. 
 * Kayan widget'ın yönetimini ve Bezier eğrisi kullanılarak sisteme gönderilen
 * yüksek hızlı "falsolu" (kavisli) kaydırma jestlerini (gestures) kontrol eder.
 */
class AutoSwipeService : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    
    @Volatile
    private var isSwiping = false
    
    // Coroutine kapsamı, bellek sızıntılarını önlemek için yaşam döngüsüne bağlıdır.
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    // --- HIZ VE FALSO KONFİGÜRASYONLARI ---
    // swipeDurationMs: Kaydırma işleminin ne kadar süreceği (Düşük = Çok daha hızlı)
    private val swipeDurationMs = 30L 
    // swipeIntervalMs: İki kaydırma arasında bekleme süresi
    private val swipeIntervalMs = 5L 

    override fun onServiceConnected() {
        super.onServiceConnected()
        createFloatingWidget()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingWidget() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#99000000")) // Yarı saydam arka plan
            setPadding(15, 15, 15, 15)
        }

        val btnStartStop = Button(this).apply {
            text = "▶ BAŞLAT"
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                if (isSwiping) {
                    stopSwiping()
                    text = "▶ BAŞLAT"
                    setBackgroundColor(Color.parseColor("#4CAF50"))
                } else {
                    startSwiping()
                    text = "⏸ DURDUR"
                    setBackgroundColor(Color.parseColor("#F44336"))
                }
            }
        }
        layout.addView(btnStartStop)

        // Widget'ın ekranda sürüklenebilmesi (Drag & Drop) için Touch Listener
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        layout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, layoutParams)
                    true
                }
                else -> false
            }
        }

        floatingView = layout
        windowManager.addView(floatingView, layoutParams)
    }

    private fun startSwiping() {
        isSwiping = true
        
        serviceScope.launch {
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels.toFloat()
            val screenHeight = displayMetrics.heightPixels.toFloat()

            // DİNAMİK EKRAN KOORDİNATLARI (Aşağıdan Yukarıya Doğru)
            val startX = screenWidth * 0.5f  // Ekranın tam ortası
            val startY = screenHeight * 0.8f // Ekranın alt kısmı (%80)
            
            val endX = screenWidth * 0.5f    // Ekranın tam ortası
            val endY = screenHeight * 0.2f   // Ekranın üst kısmı (%20)

            // FALSO EFEKTİ (Bezier Control Point)
            // Bu nokta, düz çizgiyi ekranın sağına doğru çeken çekim kuvvetini oluşturur.
            val controlX = screenWidth * 0.9f 
            val controlY = screenHeight * 0.5f 

            // Yüksek Hızlı Asenkron Döngü
            while (isSwiping && isActive) {
                performCurvedSwipe(startX, startY, endX, endY, controlX, controlY, swipeDurationMs)
                // Android'in jesti işleyebilmesi için jest süresi + bekleme süresi kadar duraksama
                delay(swipeIntervalMs + swipeDurationMs)
            }
        }
    }

    /**
     * Quadratic Bezier Eğrisi ile falsolu bir rota (Path) çizer ve sisteme enjekte eder.
     */
    private fun performCurvedSwipe(startX: Float, startY: Float, endX: Float, endY: Float, controlX: Float, controlY: Float, durationMs: Long) {
        val path = Path()
        path.moveTo(startX, startY)
        
        // Falsoyu (kavisi) veren matematiksel algoritma
        path.quadTo(controlX, controlY, endX, endY)

        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, null, null)
    }

    private fun stopSwiping() {
        isSwiping = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
        stopSwiping()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSwiping()
        serviceScope.cancel() 
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView) 
        }
    }
}
