# BHILANI Interoperability by kantini, chanchali

Run SDK

    Android Studio

Usage

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

Screenshot
<img width="1920" height="1080" alt="Screenshot (195)" src="https://github.com/user-attachments/assets/118c3363-0887-4856-ade8-595ca2014581" />

**@AIAmitSuri, Co-creator/Co-founder (🙏 Mata Shabri 🙏)**
