package com.opensource.autoswiper

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Bu sınıf yalnızca uygulamanın ihtiyaç duyduğu kritik izinleri kullanıcıdan almak
 * için tasarlanmıştır. Hiçbir harici kütüphane kullanılmamış, arayüz tamamen
 * programatik olarak oluşturulmuştur (Zero-cost architecture).
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createView())
    }

    private fun createView(): android.view.View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 64, 64, 64)
        }

        val title = TextView(this).apply {
            text = "Falsolu Multi-Speed Auto Swiper"
            textSize = 22f
            setPadding(0, 0, 0, 64)
        }
        layout.addView(title)

        // 1. Adım: Kayan Pencere İzni
        val btnOverlay = Button(this).apply {
            text = "1. Kayan Ekran İznini Ver"
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this@MainActivity)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "İzin zaten verilmiş, 2. adıma geçin.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        layout.addView(btnOverlay)

        // 2. Adım: Erişilebilirlik Servisi İzni
        val btnAccessibility = Button(this).apply {
            text = "2. Erişilebilirlik Servisini Başlat"
            setPadding(0, 32, 0, 0)
            setOnClickListener {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                Toast.makeText(this@MainActivity, "Listeden 'Falsolu Swiper'ı bulup aktif edin.", Toast.LENGTH_LONG).show()
            }
        }
        layout.addView(btnAccessibility)

        return layout
    }
}
