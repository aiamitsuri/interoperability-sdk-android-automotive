# BHILANI Interoperability by kantini, chanchali

Run SDK

    Android Studio

Usage

Usage

    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.withContext
    import rust.interop.bridge.*
    
    // Pure logic to fetch data from the Rust JNI bridge
    suspend fun fetchDataFromRust(pageNumber: Int): FilterResponse {
    
        // 1. Create the params object
        val params = FilterParams(
            language = null,
            integration = null,
            crates = null,
            developmentkit = null,
            page = pageNumber.toString(),
            ids = null
        )
    
        // 2. Execute the call on the IO thread
        return withContext(Dispatchers.IO) {
            fetchInteroperability(params)
        }        
    }

Screenshot
<img width="1920" height="1080" alt="Screenshot (195)" src="https://github.com/user-attachments/assets/118c3363-0887-4856-ade8-595ca2014581" />

**@AIAmitSuri, Co-creator/Co-founder (🙏 Mata Shabri 🙏)**
