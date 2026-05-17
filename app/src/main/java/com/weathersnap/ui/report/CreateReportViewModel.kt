package com.weathersnap.ui.report

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.data.repository.ReportRepository
import com.weathersnap.domain.model.WeatherSnapshot
import com.weathersnap.utils.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateReportUiState())
    val uiState = _uiState.asStateFlow()

    // Tracks temp camera file to clean up on discard
    private var tempCameraFile: File? = null

    fun initWithWeather(snapshot: WeatherSnapshot) {
        if (_uiState.value.weather != null) return // already initialized, don't overwrite
        viewModelScope.launch {
            // Restore draft if exists
            val draft = reportRepository.getDraft()
            if (draft != null && draft.cityName == snapshot.cityName) {
                _uiState.value = CreateReportUiState(
                    weather = snapshot, // always use the ORIGINAL passed snapshot
                    notes = draft.notes,
                    imagePath = draft.imagePath,
                    originalSizeBytes = draft.originalSizeBytes,
                    compressedSizeBytes = draft.compressedSizeBytes
                )
            } else {
                _uiState.value = CreateReportUiState(weather = snapshot)
            }
        }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
        persistDraft()
    }

    fun onImageCaptured(rawFile: File) {
        tempCameraFile = rawFile
        viewModelScope.launch {
            try {
                val result = ImageCompressor.compress(context, rawFile)
                _uiState.update {
                    it.copy(
                        imagePath = result.file.absolutePath,
                        originalSizeBytes = result.originalBytes,
                        compressedSizeBytes = result.compressedBytes
                    )
                }
                persistDraft()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Image compression failed: ${e.message}") }
            }
        }
    }

    fun saveReport() {
        val state = _uiState.value
        val weather = state.weather ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                reportRepository.saveReport(
                    weather = weather,
                    notes = state.notes,
                    imagePath = state.imagePath,
                    originalSize = state.originalSizeBytes,
                    compressedSize = state.compressedSizeBytes
                )
                reportRepository.clearDraft()
                cleanupTempFile()
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun discardDraft() {
        viewModelScope.launch {
            reportRepository.clearDraft()
            cleanupTempFile()
            // Also delete compressed image if not saved
            _uiState.value.imagePath?.let { File(it).delete() }
        }
    }

    private fun persistDraft() {
        val state = _uiState.value
        val weather = state.weather ?: return
        viewModelScope.launch {
            reportRepository.saveDraft(
                weather = weather,
                notes = state.notes,
                imagePath = state.imagePath,
                originalSize = state.originalSizeBytes,
                compressedSize = state.compressedSizeBytes
            )
        }
    }

    private fun cleanupTempFile() {
        tempCameraFile?.delete()
        tempCameraFile = null
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
