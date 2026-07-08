package nibm.mad.snapshop.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nibm.mad.snapshop.utils.extractMainObject

class MainViewModel : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _isFlashOn = MutableStateFlow(false)
    val isFlashOn: StateFlow<Boolean> = _isFlashOn

    fun toggleFlash() {
        _isFlashOn.value = !_isFlashOn.value
    }

    fun setProcessing(processing: Boolean) {
        _isProcessing.value = processing
    }

    fun processImage(context: Context, uri: Uri, onProcessed: (Uri) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val croppedUri = extractMainObject(context, uri)
                if (croppedUri != null) {
                    onProcessed(croppedUri)
                } else {
                    onError()
                }
            } catch (e: Exception) {
                onError()
            } finally {
                _isProcessing.value = false
            }
        }
    }
}
