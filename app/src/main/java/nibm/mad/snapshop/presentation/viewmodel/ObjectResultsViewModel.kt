package nibm.mad.snapshop.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nibm.mad.snapshop.domain.model.HistoryEntry
import nibm.mad.snapshop.domain.model.ProductMatch
import nibm.mad.snapshop.domain.repository.HistoryRepository
import nibm.mad.snapshop.domain.repository.ProductRepository
import nibm.mad.snapshop.presentation.components.SearchStep

class ObjectResultsViewModel(
    private val productRepository: ProductRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val TAG = "ObjectResultsVM"

    private val _currentStep = MutableStateFlow(SearchStep.IDLE)
    val currentStep: StateFlow<SearchStep> = _currentStep

    private val _headerText = MutableStateFlow("Snapped")
    val headerText: StateFlow<String> = _headerText

    private val _productMatches = MutableStateFlow<List<ProductMatch>>(emptyList())
    val productMatches: StateFlow<List<ProductMatch>> = _productMatches

    private val _foundProductCount = MutableStateFlow(0)
    val foundProductCount: StateFlow<Int> = _foundProductCount

    fun initFromHistory(headerText: String, results: List<ProductMatch>) {
        _headerText.value = headerText
        _productMatches.value = results
        _foundProductCount.value = results.size
        _currentStep.value = SearchStep.SUCCESS
    }

    fun startSearch(context: Context, croppedImageUriString: String) {
        if (_currentStep.value != SearchStep.IDLE && _currentStep.value != SearchStep.ERROR) return

        viewModelScope.launch {
            _currentStep.value = SearchStep.UPLOADING
            _foundProductCount.value = 0

            val directImageLink = productRepository.uploadImage(
                context,
                croppedImageUriString.toUri()
            )

            if (directImageLink != null) {
                Log.d(TAG, "Image uploaded successfully: $directImageLink")
                _currentStep.value = SearchStep.SEARCHING

                val productResults = productRepository.searchImage(
                    imageUrl = directImageLink
                )

                if (productResults.isNotEmpty()) {
                    Log.d(TAG, "Found ${productResults.size} products")
                    _foundProductCount.value = productResults.size
                    _currentStep.value = SearchStep.DISTILLING

                    val allTitles = productResults.map { it.title }
                    val top5Titles = allTitles.take(5)

                    val finalDistilledQuery = productRepository.distillQuery(top5Titles)

                    if (finalDistilledQuery != null) {
                        Log.d(TAG, "Query distilled: $finalDistilledQuery")
                        _headerText.value = finalDistilledQuery
                        _productMatches.value = productResults
                        _currentStep.value = SearchStep.SUCCESS

                        // Save to History
                        val historyEntry = HistoryEntry(
                            productName = finalDistilledQuery,
                            imageUrl = croppedImageUriString,
                            timestamp = System.currentTimeMillis(),
                            resultsJson = Json.encodeToString(productResults)
                        )
                        historyRepository.insertHistory(historyEntry)

                    } else {
                        Log.e(TAG, "Query distillation failed (returned null)")
                        _currentStep.value = SearchStep.ERROR
                    }
                } else {
                    Log.e(TAG, "No products found by SerpApi")
                    _currentStep.value = SearchStep.ERROR
                }
            } else {
                Log.e(TAG, "Image upload to ImgBB failed")
                _currentStep.value = SearchStep.ERROR
            }
        }
    }
}
