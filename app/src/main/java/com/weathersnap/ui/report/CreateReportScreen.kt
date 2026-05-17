package com.weathersnap.ui.report

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.weathersnap.ui.SharedWeatherViewModel
import com.weathersnap.ui.components.WeatherCard
import com.weathersnap.ui.theme.*
import com.weathersnap.utils.toReadableSize
import java.io.File

@Composable
fun CreateReportScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToSavedReports: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateReportViewModel = hiltViewModel(),
    sharedViewModel: SharedWeatherViewModel = hiltViewModel()
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedWeather  by sharedViewModel.selectedWeather.collectAsStateWithLifecycle()
    val capturedImageFile by sharedViewModel.capturedImageFile.collectAsStateWithLifecycle()

    LaunchedEffect(selectedWeather)    { selectedWeather?.let { viewModel.initWithWeather(it) } }
    LaunchedEffect(capturedImageFile)  {
        capturedImageFile?.let { viewModel.onImageCaptured(it); sharedViewModel.consumeCapturedImage() }
    }
    LaunchedEffect(uiState.isSaved)    { if (uiState.isSaved) onNavigateToSavedReports() }
    BackHandler { viewModel.discardDraft(); onBack() }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top bar ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.discardDraft(); onBack() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Surface2)
                        .border(1.dp, Border, RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                        tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(14.dp))
                Text("New Report", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // ── Weather snapshot ─────────────────────────────
                uiState.weather?.let { snapshot ->
                    SectionLabel("Weather")
                    Spacer(Modifier.height(10.dp))
                    WeatherCard(snapshot = snapshot)
                    Spacer(Modifier.height(24.dp))
                }

                // ── Photo ────────────────────────────────────────
                SectionLabel("Photo")
                Spacer(Modifier.height(10.dp))
                AnimatedContent(
                    targetState = uiState.imagePath,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                    label = "image_preview"
                ) { path ->
                    if (path != null) {
                        ImagePreviewCard(
                            imagePath = path,
                            originalSize = uiState.originalSizeBytes,
                            compressedSize = uiState.compressedSizeBytes,
                            onRetake = onNavigateToCamera
                        )
                    } else {
                        CapturePhotoPlaceholder(onClick = onNavigateToCamera)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Notes ────────────────────────────────────────
                SectionLabel("Notes")
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Add notes about the weather...",
                            color = TextTertiary, style = MaterialTheme.typography.bodyLarge)
                    },
                    minLines = 4,
                    maxLines = 7,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = Surface1,
                        unfocusedContainerColor = Surface1,
                        focusedBorderColor      = BorderLight,
                        unfocusedBorderColor    = Border,
                        focusedTextColor        = TextPrimary,
                        unfocusedTextColor      = TextPrimary,
                        cursorColor             = Amber
                    )
                )

                Spacer(Modifier.height(28.dp))

                // ── Save button ──────────────────────────────────
                Button(
                    onClick = viewModel::saveReport,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = uiState.weather != null && !uiState.isSaving,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = Amber,
                        contentColor           = Background,
                        disabledContainerColor = Surface2,
                        disabledContentColor   = TextTertiary
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp, color = Background)
                    } else {
                        Text("Save Report", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(Modifier.height(36.dp))
            }
        }

        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp).navigationBarsPadding(),
                containerColor = Surface2,
                contentColor = TextPrimary,
                action = { TextButton(onClick = viewModel::clearError) {
                    Text("Dismiss", color = Amber) }
                }
            ) { Text(error) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = TextSecondary)
}

@Composable
private fun ImagePreviewCard(
    imagePath: String,
    originalSize: Long,
    compressedSize: Long,
    onRetake: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
    ) {
        AsyncImage(
            model = File(imagePath),
            contentDescription = "Captured photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentScale = ContentScale.Crop
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${originalSize.toReadableSize()} → ${compressedSize.toReadableSize()}",
                    style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text("Compressed", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
            TextButton(onClick = onRetake) {
                Text("Retake", style = MaterialTheme.typography.labelLarge, color = Amber)
            }
        }
    }
}

@Composable
private fun CapturePhotoPlaceholder(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface3),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null,
                    tint = TextSecondary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("Add a photo", style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(3.dp))
            Text("Optional", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        }
    }
}
