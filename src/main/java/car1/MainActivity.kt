package car1

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rust.interop.data.*
import rust.interop.logic.*

class MainActivity : AppCompatActivity() {

    private lateinit var statusTv: TextView
    private lateinit var dataContainer: LinearLayout
    private lateinit var prevBtn: Button
    private lateinit var nextBtn: Button

    private var currentPage = 1
    private var totalPages = 1 // Dynamically updated from Rust metadata

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root Layout: Black background (OLED friendly/AAOS standard)
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.BLACK)
            weightSum = 10f
        }

        // --- LEFT PANEL: NAVIGATION (Touch optimized for car) ---
        val leftPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -1, 3.5f)
            setPadding(40, 50, 40, 40)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        leftPanel.addView(TextView(this).apply {
            text = "RUST INTEROP"
            setTextColor(Color.parseColor("#BB86FC"))
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        })

        statusTv = TextView(this).apply {
            text = "Initializing..."
            setTextColor(Color.LTGRAY)
            textSize = 18f
            setPadding(0, 20, 0, 40)
            gravity = Gravity.CENTER
        }
        leftPanel.addView(statusTv)

        // Large 64dp+ buttons for "Fat Finger" safety
        prevBtn = createNavButton("PREV")
        nextBtn = createNavButton("NEXT")

        leftPanel.addView(prevBtn)
        leftPanel.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(1, 40) })
        leftPanel.addView(nextBtn)

        rootLayout.addView(leftPanel)

        // --- RIGHT PANEL: SCROLLABLE DATA ---
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, -1, 6.5f)
            setPadding(20, 40, 40, 40)
            isVerticalScrollBarEnabled = false
        }
        dataContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scrollView.addView(dataContainer)
        rootLayout.addView(scrollView)

        setContentView(rootLayout)

        // Click Listeners with Boundary Protection
        prevBtn.setOnClickListener { if (currentPage > 1) { currentPage--; loadData() } }
        nextBtn.setOnClickListener { if (currentPage < totalPages) { currentPage++; loadData() } }

        loadData()
    }

    private fun createNavButton(label: String) = Button(this).apply {
        text = label
        textSize = 20f
        typeface = Typeface.DEFAULT_BOLD
        // Automotive standard: Large height for easy access
        layoutParams = LinearLayout.LayoutParams(-1, 130)
    }

    private fun loadData() {
        // Clear UI and lock buttons while loading
        dataContainer.removeAllViews()
        nextBtn.isEnabled = false
        prevBtn.isEnabled = false
        statusTv.text = "Fetching P$currentPage..."

        lifecycleScope.launch {
            try {
                // RUN ON IO: Ensure Rust async call doesn't freeze the Head Unit UI
                val result = withContext(Dispatchers.IO) {
                    val params = FilterParams(null, null, null, null, currentPage.toString(), null)
                    fetchInteroperability(params)
                }

                // READ METADATA: Update dynamic page limit
                result.pagination?.let { meta ->
                    totalPages = meta.totalPages.toInt()
                }

                // Update Display Text
                statusTv.text = "Page $currentPage of $totalPages"

                // Apply Logic: Enable/Disable based on real data
                prevBtn.isEnabled = currentPage > 1
                nextBtn.isEnabled = currentPage < totalPages

                // Visual feedback (Fading)
                prevBtn.alpha = if (prevBtn.isEnabled) 1.0f else 0.3f
                nextBtn.alpha = if (nextBtn.isEnabled) 1.0f else 0.3f

                // Render List
                result.data.forEach { item ->
                    dataContainer.addView(createDataCard(item))
                }

            } catch (e: Exception) {
                statusTv.text = "Error: Sync Failed"
                android.util.Log.e("JNI_CAR", "Error: ${e.message}")
            }
        }
    }

    private fun createDataCard(item: Interoperability): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(35, 30, 35, 30)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1A1A1A"))
                cornerRadius = 15f
                setStroke(2, Color.parseColor("#333333"))
            }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 0, 0, 25) }

            addView(TextView(context).apply {
                text = item.title
                setTextColor(Color.WHITE)
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
            })
            addView(TextView(context).apply {
                text = "Language: ${item.language}"
                setTextColor(Color.parseColor("#03DAC6"))
                textSize = 16f
                setPadding(0, 8, 0, 0)
            })
        }
    }
}