package car1

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rust.interop.bridge.*

class MainActivity : AppCompatActivity() {

    private lateinit var statusTv: TextView
    private lateinit var dataContainer: LinearLayout
    private lateinit var prevBtn: Button
    private lateinit var nextBtn: Button
    private lateinit var loadingBar: ProgressBar

    private var currentPage = 1
    private var totalPages = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Root Layout: Matches the full screen height
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(-1, -1) // Fill entire screen
            weightSum = 10f
        }

        // --- LEFT PANEL: NAVIGATION (Pinned to top/center) ---
        val leftPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -1, 3f) // Exactly 30% width
            setPadding(30, 40, 30, 40)
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }

        // 1. Heading
        leftPanel.addView(TextView(this).apply {
            text = "Interoperability SDK"
            setTextColor(Color.parseColor("#BB86FC"))
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        })

        // 2. Sub Heading
        leftPanel.addView(TextView(this).apply {
            text = "Rust & Android NDK/AAOS"
            setTextColor(Color.GRAY)
            textSize = 24f
            typeface = Typeface.SANS_SERIF
            setPadding(0, 5, 0, 10)
            gravity = Gravity.CENTER
        })

        // 3. Page Indicator
        statusTv = TextView(this).apply {
            text = "Page $currentPage"
            setTextColor(Color.LTGRAY)
            textSize = 18f
            setPadding(0, 0, 0, 10)
            gravity = Gravity.CENTER
        }
        leftPanel.addView(statusTv)

        loadingBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 15).apply { setMargins(0, 0, 0, 10) }
            visibility = View.INVISIBLE
            isIndeterminate = true
        }
        leftPanel.addView(loadingBar)

        prevBtn = createNavButton("PREV")
        nextBtn = createNavButton("NEXT")

        leftPanel.addView(prevBtn)
        leftPanel.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(1, 20) })
        leftPanel.addView(nextBtn)

        rootLayout.addView(leftPanel)

        // --- RIGHT PANEL: SCROLLABLE DATA (Fills remaining 70%) ---
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, -1, 7f)
            setPadding(10, 40, 40, 40)
            clipToPadding = false // Ensures top items aren't cut off
            isVerticalScrollBarEnabled = false
        }
        dataContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, -2)
        }
        scrollView.addView(dataContainer)
        rootLayout.addView(scrollView)

        setContentView(rootLayout)

        prevBtn.setOnClickListener { if (currentPage > 1) { currentPage--; loadData() } }
        nextBtn.setOnClickListener { if (currentPage < totalPages) { currentPage++; loadData() } }

        loadData()
    }

    private fun createNavButton(label: String) = Button(this).apply {
        text = label
        textSize = 18f
        typeface = Typeface.DEFAULT_BOLD
        // Fixed height for car accessibility (Approx 64dp)
        layoutParams = LinearLayout.LayoutParams(-1, 110)
    }

    private fun loadData() {
        nextBtn.isEnabled = false
        prevBtn.isEnabled = false
        loadingBar.visibility = View.VISIBLE
        statusTv.text = "Loading..."

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val params = FilterParams(null, null, null, null, currentPage.toString(), null)
                    fetchInteroperability(params)
                }

                dataContainer.removeAllViews()

                result.pagination?.let { meta ->
                    totalPages = meta.totalPages.toInt()
                }

                statusTv.text = "Page $currentPage of $totalPages"

                prevBtn.isEnabled = currentPage > 1
                nextBtn.isEnabled = currentPage < totalPages
                prevBtn.alpha = if (prevBtn.isEnabled) 1.0f else 0.3f
                nextBtn.alpha = if (nextBtn.isEnabled) 1.0f else 0.3f

                result.data.forEach { item ->
                    dataContainer.addView(createDataCard(item))
                }

            } catch (e: Exception) {
                statusTv.text = "Sync Error"
                prevBtn.isEnabled = currentPage > 1
                nextBtn.isEnabled = (currentPage < totalPages)
            } finally {
                loadingBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun createDataCard(item: Interoperability): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 25, 30, 25)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1A1A1A"))
                cornerRadius = 12f
                setStroke(2, Color.parseColor("#333333"))
            }
            // Ensure cards take up consistent space
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 0, 0, 20) }

            addView(TextView(context).apply {
                text = item.title
                setTextColor(Color.WHITE)
                textSize = 20f
                typeface = Typeface.DEFAULT_BOLD
            })
            addView(TextView(context).apply {
                text = "Integration: ${item.integration}"
                setTextColor(Color.parseColor("#03DAC6"))
                textSize = 16f
                setPadding(0, 5, 0, 0)
            })
        }
    }
}