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
import androidx.compose.ui.graphics.Color
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedWeather by sharedViewModel.selectedWeather.collectAsStateWithLifecycle()
    val capturedImageFile by sharedViewModel.capturedImageFile.collectAsStateWithLifecycle()

    LaunchedEffect(selectedWeather) { selectedWeather?.let { viewModel.initWithWeather(it) } }
    LaunchedEffect(capturedImageFile) {
        capturedImageFile?.let {
            viewModel.onImageCaptured(it)
            sharedViewModel.consumeCapturedImage()
        }
    }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onNavigateToSavedReports() }
    BackHandler { viewModel.discardDraft(); onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Top bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.discardDraft(); onBack() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface2)
                        .border(1.dp, Border, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Text("Create Report", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            }

            Spacer(Modifier.height(28.dp))

            uiState.weather?.let { snapshot ->
                SectionLabel("Weather")
                Spacer(Modifier.height(10.dp))
                WeatherCard(snapshot = snapshot)
            }

            Spacer(Modifier.height(24.dp))

            SectionLabel("Photo")
            Spacer(Modifier.height(10.dp))

            AnimatedContent(
                targetState = uiState.imagePath,
                transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(180)) },
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

            SectionLabel("Notes")
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Add notes...", color = TextTertiary, style = MaterialTheme.typography.bodyLarge)
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

            Button(
                onClick = viewModel::saveReport,
                modifier = Modifier.fillMaxWidth().height(50.dp),
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
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Background)
                } else {
                    Text("Save Report", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp).navigationBarsPadding(),
                containerColor = Surface2,
                contentColor = TextPrimary,
                action = { TextButton(onClick = viewModel::clearError) { Text("Dismiss", color = Amber) } }
            ) { Text(error) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
}

@Composable
private fun ImagePreviewCard(
    imagePath: String,
    originalSize: Long,
    compressedSize: Long,
    onRetake: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Surface1)
            .border(1.dp, Border, shape)
    ) {
        AsyncImage(
            model = File(imagePath),
            contentDescription = "Captured photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
            contentScale = ContentScale.Crop
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "${originalSize.toReadableSize()} → ${compressedSize.toReadableSize()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
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
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(shape)
            .background(Surface1)
            .border(1.dp, Border, shape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text("Add a photo", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Text("Optional", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        }
    }
}
