package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.Candidate
import com.example.api.ResumeAnalysisResponse
import com.example.api.RewriteSuggestion
import com.example.data.AnalysisRecord
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AnalysisUiState
import com.example.viewmodel.ResumeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import java.text.SimpleDateFormat
import java.util.*

// --- Cohesive Dynamic Custom Color Struct ---
data class AppThemeColors(
    val bg: Color,
    val surface: Color,
    val white: Color,
    val border: Color,
    val textDark: Color,
    val textMuted: Color,
    val primaryBlue: Color,
    val accentPurple: Color,
    val scoreBg: Color,
    val scoreTxt: Color,
    val cardBg: Color,
    val lightRedBg: Color,
    val darkRedTxt: Color,
    val lightGreenBg: Color,
    val darkGreenTxt: Color,
    val lightBlueBg: Color,
    val darkBlueTxt: Color,
    val emeraldAccent: Color,
    val amberAccent: Color,
    val roseAccent: Color
)

class MainActivity : ComponentActivity() {
    private val viewModel: ResumeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            // Dynamic color declarations to fulfill both Beautiful Light & Dark premium Minimalist vibes
            val appColors = if (isDarkTheme) {
                AppThemeColors(
                    bg = Color(0xFF090D1A), // Astro Deep Space Obsidian Dark Background
                    surface = Color(0xFF131B2E), // Rich Dark Indigo panel container
                    white = Color(0xFF1B243B), // Elevated Glassmorphic Card face
                    border = Color(0xFF283556), // Sophisticated neon-border guidelines
                    textDark = Color(0xFFFAF9FB), // High-fidelity Crystal White display titles
                    textMuted = Color(0xFF94A3B8), // Sleek silver slate supporting text description
                    primaryBlue = Color(0xFF38BDF8), // Radiant Sky Cyan theme primary
                    accentPurple = Color(0xFF818CF8), // Electric Purple accents
                    scoreBg = Color(0xFF151D34), // Rich deep-sapphire container bg for key metrics
                    scoreTxt = Color(0xFFE0E7FF), // Celestial indigo-white highlights
                    cardBg = Color(0xFF161F35),
                    lightRedBg = Color(0xFF4C1D24), // Vibrant crimson accents
                    darkRedTxt = Color(0xFFFDA4AF),
                    lightGreenBg = Color(0xFF064E3B), // Premium dark forest emerald
                    darkGreenTxt = Color(0xFF34D399),
                    lightBlueBg = Color(0xFF1E3A8A), // Regal sapphire background
                    darkBlueTxt = Color(0xFF93C5FD),
                    emeraldAccent = Color(0xFF10B981),
                    amberAccent = Color(0xFFFBBF24),
                    roseAccent = Color(0xFFF43F5E)
                )
            } else {
                AppThemeColors(
                    bg = Color(0xFFF8FAFC), // Elegant Soft Pearl Snow Light Canvas
                    surface = Color(0xFFF1F5F9), // Subtle Cloud Lavender Slate structural panels
                    white = Color(0xFFFFFFFF), // Core card container bodies
                    border = Color(0xFFE2E8F0), // Ultra clean hair-thin slate borders
                    textDark = Color(0xFF0F172A), // Luxury obsidian primary ink
                    textMuted = Color(0xFF475569), // Calm slate supporting descriptions
                    primaryBlue = Color(0xFF1D4ED8), // Deep cobalt high-fidelity active accents
                    accentPurple = Color(0xFF6D28D9), // Luxurious Royal amethyst accents
                    scoreBg = Color(0xFFEEF2FF), // Soft lavender ice backdrop for scoring details
                    scoreTxt = Color(0xFF1E1B4B), // Heavy indigo-ink contrast highlight
                    cardBg = Color(0xFFFFFFFF),
                    lightRedBg = Color(0xFFFEE2E2),
                    darkRedTxt = Color(0xFF991B1B),
                    lightGreenBg = Color(0xFFD1FAE5),
                    darkGreenTxt = Color(0xFF065F46),
                    lightBlueBg = Color(0xFFEFF6FF),
                    darkBlueTxt = Color(0xFF1E40AF),
                    emeraldAccent = Color(0xFF059669),
                    amberAccent = Color(0xFFD97706),
                    roseAccent = Color(0xFFE11D48)
                )
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    containerColor = appColors.bg
                ) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme },
                        appColors = appColors,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: ResumeViewModel,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    appColors: AppThemeColors = AppThemeColors(
        bg = Color(0xFFF8FAFC), // Elegant Soft Pearl Snow Light Canvas
        surface = Color(0xFFF1F5F9), // Subtle Cloud Lavender Slate structural panels
        white = Color(0xFFFFFFFF), // Core card container bodies
        border = Color(0xFFE2E8F0), // Ultra clean hair-thin slate borders
        textDark = Color(0xFF0F172A), // Luxury obsidian primary ink
        textMuted = Color(0xFF475569), // Calm slate supporting descriptions
        primaryBlue = Color(0xFF1D4ED8), // Deep cobalt high-fidelity active accents
        accentPurple = Color(0xFF6D28D9), // Luxurious Royal amethyst accents
        scoreBg = Color(0xFFEEF2FF), // Soft lavender ice backdrop for scoring details
        scoreTxt = Color(0xFF1E1B4B), // Heavy indigo-ink contrast highlight
        cardBg = Color(0xFFFFFFFF),
        lightRedBg = Color(0xFFFEE2E2),
        darkRedTxt = Color(0xFF991B1B),
        lightGreenBg = Color(0xFFD1FAE5),
        darkGreenTxt = Color(0xFF065F46),
        lightBlueBg = Color(0xFFEFF6FF),
        darkBlueTxt = Color(0xFF1E40AF),
        emeraldAccent = Color(0xFF059669),
        amberAccent = Color(0xFFD97706),
        roseAccent = Color(0xFFE11D48)
    ),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.historyState.collectAsState()

    val resumeText by viewModel.resumeInput.collectAsState()
    val jobDescText by viewModel.jobDescInput.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()

    var showKeyDialog by remember { mutableStateOf(false) }

    if (showKeyDialog) {
        GeminiKeyDialog(
            currentKey = customApiKey,
            onSave = { viewModel.saveApiKey(it) },
            onDismiss = { showKeyDialog = false },
            appColors = appColors
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(appColors.bg, appColors.surface.copy(alpha = 0.35f))
                )
            )
    ) {
        // --- Elegant Header App Bar with Logo & Theme Toggle ---
        HeaderSection(
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onConfigKeyClick = { showKeyDialog = true },
            appColors = appColors
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "state_transition"
            ) { state ->
                when (state) {
                    is AnalysisUiState.Idle -> {
                        InputForm(
                            resumeText = resumeText,
                            jobDescText = jobDescText,
                            history = history,
                            onResumeChange = { viewModel.resumeInput.value = it },
                            onJobDescChange = { viewModel.jobDescInput.value = it },
                            onLoadAndroidPreset = { viewModel.setAndroidPreset() },
                            onLoadWebPreset = { viewModel.setWebPreset() },
                            onAnalyze = { viewModel.analyzeResume() },
                            onLoadHistory = { viewModel.loadResponseDirectly(it) },
                            onDeleteHistory = { viewModel.deleteRecord(it) },
                            onClearHistory = { viewModel.clearAllHistory() },
                            appColors = appColors
                        )
                    }
                    is AnalysisUiState.Loading -> {
                        LoadingScreen(message = state.message, appColors = appColors)
                    }
                    is AnalysisUiState.Success -> {
                        ResultsScreen(
                            response = state.response,
                            onAnalyzeAnother = { viewModel.resetState() },
                            appColors = appColors
                        )
                    }
                    is AnalysisUiState.Error -> {
                        ErrorScreen(
                            errorMessage = state.message,
                            onDismiss = { viewModel.resetState() },
                            appColors = appColors
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BeautifulLogo(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier.size(40.dp)) {
        val w = size.width
        val h = size.height
        
        // Draw elegant decorative orbital arcs representing system analysis
        drawArc(
            color = color.copy(alpha = 0.2f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawArc(
            color = color.copy(alpha = 0.55f),
            startAngle = -20f,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
            color = color.copy(alpha = 0.55f),
            startAngle = 160f,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Gorgeous center analytical star vector
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.22f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.78f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, h * 0.78f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.22f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, h * 0.22f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
fun SunMoonIcon(isDark: Boolean, modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        if (isDark) {
            // High-contrast glowing crescent moon
            val moonPath = Path().apply {
                moveTo(w * 0.35f, h * 0.2f)
                quadraticTo(w * 0.85f, h * 0.5f, w * 0.35f, h * 0.8f)
                quadraticTo(w * 0.65f, h * 0.5f, w * 0.35f, h * 0.2f)
            }
            drawPath(path = moonPath, color = color)
        } else {
            // Bright sun with custom radiant beams
            drawCircle(color = color, radius = w * 0.25f)
            for (i in 0 until 8) {
                val angle = i * Math.PI / 4
                val startX = (w * 0.5f + Math.cos(angle) * w * 0.3f).toFloat()
                val startY = (h * 0.5f + Math.sin(angle) * w * 0.3f).toFloat()
                val endX = (w * 0.5f + Math.cos(angle) * w * 0.44f).toFloat()
                val endY = (h * 0.5f + Math.sin(angle) * w * 0.44f).toFloat()
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(startX, startY),
                    end = androidx.compose.ui.geometry.Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun GeminiKeyDialog(
    currentKey: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    appColors: AppThemeColors
) {
    var keyText by remember { mutableStateOf(currentKey) }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = appColors.primaryBlue
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Configure Gemini API Key",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = appColors.textDark
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter your custom Gemini API Key below. If left blank, the app will fall back to the built-in AI Studio developer key.",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textMuted,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { showPassword = !showPassword }) {
                            Text(
                                text = if (showPassword) "Hide" else "Show",
                                color = appColors.primaryBlue,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.primaryBlue,
                        unfocusedBorderColor = appColors.border,
                        focusedLabelColor = appColors.primaryBlue,
                        unfocusedLabelColor = appColors.textMuted,
                        focusedTextColor = appColors.textDark,
                        unfocusedTextColor = appColors.textDark
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Your API Key is stored safely on this device via native SharedPreferences. It is only sent directly to Google's official Gemini API servers.",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = appColors.textMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(keyText.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Key", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = appColors.textMuted)
            }
        },
        containerColor = appColors.cardBg,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun HeaderSection(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onConfigKeyClick: () -> Unit,
    appColors: AppThemeColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(appColors.primaryBlue.copy(alpha = 0.15f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                BeautifulLogo(color = appColors.primaryBlue)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "AI Resume Analyser",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = appColors.textDark
                )
                Text(
                    text = "Tailor & Scan using Gemini 3.5 Flash",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textMuted
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Elegant Key Config Button
            IconButton(
                onClick = onConfigKeyClick,
                modifier = Modifier
                    .size(42.dp)
                    .background(appColors.surface.copy(alpha = 0.5f), CircleShape)
                    .border(1.dp, appColors.border.copy(alpha = 0.25f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Configure Key",
                    tint = appColors.primaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Beautiful Theme Switch Button
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .size(42.dp)
                    .background(appColors.surface.copy(alpha = 0.5f), CircleShape)
                    .border(1.dp, appColors.border.copy(alpha = 0.25f), CircleShape)
            ) {
                SunMoonIcon(isDark = isDarkTheme, color = appColors.primaryBlue)
            }
        }
    }
}

@Composable
fun InputForm(
    resumeText: String,
    jobDescText: String,
    history: List<AnalysisRecord>,
    onResumeChange: (String) -> Unit,
    onJobDescChange: (String) -> Unit,
    onLoadAndroidPreset: () -> Unit,
    onLoadWebPreset: () -> Unit,
    onAnalyze: () -> Unit,
    onLoadHistory: (AnalysisRecord) -> Unit,
    onDeleteHistory: (AnalysisRecord) -> Unit,
    onClearHistory: () -> Unit,
    appColors: AppThemeColors
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val coroutineScope = rememberCoroutineScope()
    
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var isFileLoaded by remember { mutableStateOf(false) }

    // Multi-format Text Picker
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val name = withContext(Dispatchers.IO) {
                        getFileName(context, it) ?: "resume.txt"
                    }
                    selectedFileName = name
                    val content = withContext(Dispatchers.IO) {
                        contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                            reader.readText()
                        }
                    }
                    if (!content.isNullOrBlank()) {
                        onResumeChange(content)
                        isFileLoaded = true
                        Toast.makeText(context, "Loaded Profile: $name", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Loaded file has unreadable/empty text format.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error reading text contents: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        // --- Try Presets Quick Launcher ---
        Text(
            text = "PRESET SYSTEM INTUITION",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            ),
            color = appColors.primaryBlue,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Initiate parsing benchmarks. Tap a simulated preset profile below to immediately explore ATS reports without uploading files.",
            style = MaterialTheme.typography.bodySmall,
            color = appColors.textMuted,
            modifier = Modifier.padding(bottom = 14.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 26.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                onClick = onLoadAndroidPreset,
                colors = CardDefaults.cardColors(containerColor = appColors.white),
                border = BorderStroke(1.dp, appColors.border.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .testTag("preset_android_btn")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(appColors.lightBlueBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = appColors.primaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Android Dev",
                        color = appColors.textDark,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Card(
                onClick = onLoadWebPreset,
                colors = CardDefaults.cardColors(containerColor = appColors.white),
                border = BorderStroke(1.dp, appColors.border.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .testTag("preset_web_btn")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(appColors.lightGreenBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = appColors.emeraldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Fullstack Web",
                        color = appColors.textDark,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // --- EXPORTED DOCUMENT FILE PICKER ---
        Text(
            text = "SECURE DOCUMENT PARSER",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp
            ),
            color = appColors.primaryBlue,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Select any plain text resume document (.txt, .md, .docx structural exports) to automatically ingest information securely.",
            style = MaterialTheme.typography.bodySmall,
            color = appColors.textMuted,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // Drag/Click selection area with glowing visual transition
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 26.dp)
                .background(
                    brush = if (isFileLoaded) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                appColors.lightGreenBg.copy(alpha = 0.35f),
                                appColors.bg.copy(alpha = 0.1f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                appColors.white.copy(alpha = 0.9f),
                                appColors.surface.copy(alpha = 0.5f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = if (isFileLoaded) appColors.darkGreenTxt.copy(alpha = 0.8f) else appColors.border.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable {
                    try {
                        fileLauncher.launch("*/*")
                    } catch (e: android.content.ActivityNotFoundException) {
                        Toast.makeText(context, "File selector app is not installed/enabled. Try pasting the resume content or loading a design preset config.", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error launching file picker: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
                .padding(vertical = 28.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Vector drawn document status icon
                Canvas(modifier = Modifier.size(50.dp)) {
                    val w = size.width
                    val h = size.height
                    val docColor = if (isFileLoaded) appColors.darkGreenTxt else appColors.primaryBlue
                    
                    val pathDoc = Path().apply {
                        moveTo(w * 0.25f, h * 0.15f)
                        lineTo(w * 0.6f, h * 0.15f)
                        lineTo(w * 0.75f, h * 0.3f)
                        lineTo(w * 0.75f, h * 0.85f)
                        lineTo(w * 0.25f, h * 0.85f)
                        close()
                    }
                    drawPath(path = pathDoc, color = docColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                    
                    // Folder corner fold
                    drawLine(color = docColor, start = androidx.compose.ui.geometry.Offset(w * 0.6f, h * 0.15f), end = androidx.compose.ui.geometry.Offset(w * 0.6f, h * 0.3f), strokeWidth = 2.dp.toPx())
                    drawLine(color = docColor, start = androidx.compose.ui.geometry.Offset(w * 0.6f, h * 0.3f), end = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.3f), strokeWidth = 2.dp.toPx())
                    
                    // Up arrow design
                    val arrowYOffset = h * 0.04f
                    drawLine(color = docColor, start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.75f - arrowYOffset), end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.45f - arrowYOffset), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(color = docColor, start = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.57f - arrowYOffset), end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.45f - arrowYOffset), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(color = docColor, start = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.57f - arrowYOffset), end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.45f - arrowYOffset), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedFileName != null) {
                    Text(
                        text = selectedFileName!!,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isFileLoaded) appColors.darkGreenTxt else appColors.textDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Document Ingested Successfully • Tap to swap",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = if (isFileLoaded) appColors.darkGreenTxt else appColors.textMuted,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Import Local Resume File",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = appColors.textDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap to browse records • Gemini AI will extract structural data",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // --- Resume Text Area ---
        Text(
            text = "CANDIDATE RESUME TEXT",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = appColors.primaryBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = resumeText,
            onValueChange = {
                onResumeChange(it)
                if (it.isBlank()) {
                    selectedFileName = null
                    isFileLoaded = false
                }
            },
            placeholder = { Text("Paste candidate's resume content here (e.g. experience, education, skills)...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("resume_input_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = appColors.textDark,
                unfocusedTextColor = appColors.textDark,
                focusedContainerColor = appColors.white,
                unfocusedContainerColor = appColors.surface.copy(alpha = 0.4f),
                focusedBorderColor = appColors.primaryBlue,
                unfocusedBorderColor = appColors.border.copy(alpha = 0.5f),
                focusedPlaceholderColor = appColors.textMuted.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = appColors.textMuted.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Target JD Input ---
        Text(
            text = "TARGET JOB DESCRIPTION (OPTIONAL)",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = appColors.primaryBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = jobDescText,
            onValueChange = onJobDescChange,
            placeholder = { Text("Paste target job listing description to evaluate candidate tailoring matching, alignment, and missing keyword metrics...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .testTag("jd_input_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = appColors.textDark,
                unfocusedTextColor = appColors.textDark,
                focusedContainerColor = appColors.white,
                unfocusedContainerColor = appColors.surface.copy(alpha = 0.4f),
                focusedBorderColor = appColors.primaryBlue,
                unfocusedBorderColor = appColors.border.copy(alpha = 0.5f),
                focusedPlaceholderColor = appColors.textMuted.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = appColors.textMuted.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // --- Run Analysis Button ---
        Button(
            onClick = onAnalyze,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(appColors.primaryBlue, appColors.accentPurple)
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .testTag("analyze_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(28.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Analyze & Optimize Resume",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Historical Logs List ---
        if (history.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PREVIOUS SCAN HISTORY",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = appColors.primaryBlue
                )
                TextButton(
                    onClick = onClearHistory,
                    colors = ButtonDefaults.textButtonColors(contentColor = appColors.darkRedTxt)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear all logo",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Clear All",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            history.forEach { record ->
                HistoryItemRow(
                    record = record,
                    onLoad = { onLoadHistory(record) },
                    onDelete = { onDeleteHistory(record) },
                    appColors = appColors
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Branding ---
        Text(
            text = "Powered by Gemini Flash 3.5",
            style = MaterialTheme.typography.bodySmall,
            color = appColors.textMuted.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun HistoryItemRow(
    record: AnalysisRecord,
    onLoad: () -> Unit,
    onDelete: () -> Unit,
    appColors: AppThemeColors
) {
    val dateString = remember(record.timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy - HH:mm", Locale.getDefault())
        sdf.format(Date(record.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(appColors.cardBg, RoundedCornerShape(16.dp))
            .border(1.dp, appColors.border.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onLoad() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Score Badge
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = when {
                        record.overallScore >= 80 -> appColors.lightGreenBg
                        record.overallScore >= 60 -> Color(0xFFFFF3CD).copy(alpha = 0.25f)
                        else -> appColors.lightRedBg
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = record.overallScore.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = when {
                    record.overallScore >= 80 -> appColors.darkGreenTxt
                    record.overallScore >= 60 -> Color(0xFF856404)
                    else -> appColors.darkRedTxt
                }
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (record.jobDescription.isNotEmpty()) "Job-Tailored Analysis" else "General Formatting Check",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = appColors.textDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = dateString,
                style = MaterialTheme.typography.bodySmall,
                color = appColors.textMuted
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove scanner log",
                tint = appColors.textMuted.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun LoadingScreen(message: String, appColors: AppThemeColors) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = appColors.primaryBlue,
                strokeWidth = 4.dp,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(28.dp))
            AnimatedContent(
                targetState = message,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "loading_text"
            ) { targetMsg ->
                Text(
                    text = targetMsg,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    ),
                    color = appColors.textDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Gemini AI models are parsing sections, grading word choice & identifying optimizations...",
                style = MaterialTheme.typography.bodySmall,
                color = appColors.textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ResultsScreen(
    response: ResumeAnalysisResponse,
    onAnalyzeAnother: () -> Unit,
    appColors: AppThemeColors
) {
    var activeTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview Summary", "Rewrite Suggestions", "Keyword Checklist")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        // --- Circular Gauge & Score Summary Row ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.scoreBg),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreGauge(score = response.overallScore, appColors = appColors)

                Spacer(modifier = Modifier.width(24.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            response.overallScore >= 80 -> "Extremely Strong Match"
                            response.overallScore >= 60 -> "Awaiting Key Polish"
                            else -> "Action Required"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = appColors.scoreTxt
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Categories Scores Cards
                    CategoryScoreRow("Impact & Verbs", response.categoryScores.impact, appColors = appColors)
                    Spacer(modifier = Modifier.height(4.dp))
                    CategoryScoreRow("Formatting", response.categoryScores.formatting, appColors = appColors)
                    Spacer(modifier = Modifier.height(4.dp))
                    CategoryScoreRow("Job Match", response.categoryScores.alignment, appColors = appColors)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Custom Flat Tab Component ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.surface, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = index == activeTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (selected) appColors.primaryBlue else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { activeTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (selected) Color.White else appColors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Tab content area ---
        AnimatedContent(
            targetState = activeTab,
            label = "tab_content_switch"
        ) { tabIndex ->
            when (tabIndex) {
                0 -> SummaryTab(response = response, appColors = appColors)
                1 -> SuggestionsTab(suggestions = response.suggestions, appColors = appColors)
                2 -> KeywordsTab(response = response, appColors = appColors)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // --- Return Button ---
        Button(
            onClick = onAnalyzeAnother,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("another_analysis_button"),
            colors = ButtonDefaults.buttonColors(containerColor = appColors.surface),
            border = BorderStroke(1.dp, appColors.border.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = appColors.primaryBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Scan/Analyse Another Resume",
                color = appColors.primaryBlue,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Branding ---
        Text(
            text = "Powered by Gemini Flash 3.5",
            style = MaterialTheme.typography.bodySmall,
            color = appColors.textMuted.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(44.dp))
    }
}

@Composable
fun SummaryTab(response: ResumeAnalysisResponse, appColors: AppThemeColors) {
    Column {
        // Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = BorderStroke(1.dp, appColors.border.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "EXECUTIVE ANALYSIS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = appColors.primaryBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = response.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.textDark,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Strengths Card
        Card(
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = BorderStroke(1.dp, appColors.border.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "CORE STRENGTHS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = appColors.darkGreenTxt
                )
                Spacer(modifier = Modifier.height(10.dp))
                response.strengths.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "check logo",
                            tint = appColors.emeraldAccent,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.textDark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Elevator Pitch Card
        Card(
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = BorderStroke(1.dp, appColors.border.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "TAILORED MINI PITCH / PROFILE SUMMARY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = appColors.primaryBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${response.tailoredPitch}\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Serif
                    ),
                    color = appColors.textDark,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun SuggestionsTab(suggestions: List<RewriteSuggestion>, appColors: AppThemeColors) {
    if (suggestions.isEmpty()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = BorderStroke(1.dp, appColors.border.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Awesome work! Gemini found zero issues or weak bullet points on this document.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.textMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        suggestions.forEachIndexed { index, suggestion ->
            Card(
                colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
                border = BorderStroke(1.dp, appColors.border.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = suggestion.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = appColors.primaryBlue
                        )
                        Box(
                            modifier = Modifier
                                .background(appColors.primaryBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = appColors.primaryBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = suggestion.issue,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = appColors.textDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tip: ${suggestion.bestPractice}",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Side-by-Side rewritten cards
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Before Weak Sentence
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    appColors.lightRedBg.copy(alpha = 0.25f),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, appColors.lightRedBg.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "ORIGINAL RESUME DRAFT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.darkRedTxt,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = suggestion.before,
                                style = MaterialTheme.typography.bodySmall,
                                color = appColors.darkRedTxt
                            )
                        }

                        // Arrow down
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Suggested change indicator",
                            tint = appColors.primaryBlue,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        // After Action-Metric Sentence
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    appColors.lightGreenBg.copy(alpha = 0.25f),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, appColors.lightGreenBg.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "SUGGESTED HIGH-IMPACT REWRITE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.darkGreenTxt,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = suggestion.after,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = appColors.darkGreenTxt
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeywordsTab(response: ResumeAnalysisResponse, appColors: AppThemeColors) {
    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = BorderStroke(1.dp, appColors.border.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "TARGET ALIGNMENT REVEAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = appColors.primaryBlue
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "To maximize success with ATS parsers, inject the following keywords and specific technologies into your skill definitions or bullet details.",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textMuted
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "MISSING KEY PHRASES & TECHS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = appColors.darkRedTxt
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (response.missingKeywords.isEmpty()) {
                    Text(
                        text = "Nice! No critical keywords are missing from this resume matching the role.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textDark,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        response.missingKeywords.forEach { keyword ->
                            Box(
                                modifier = Modifier
                                    .background(appColors.lightRedBg.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .border(1.dp, appColors.lightRedBg.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = keyword,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.darkRedTxt
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Core Weaknesses list
                Text(
                    text = "AREAS OF CONCERN",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = appColors.amberAccent
                )
                Spacer(modifier = Modifier.height(10.dp))
                response.weaknesses.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning marker",
                            tint = appColors.amberAccent,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.textDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryScoreRow(label: String, score: Int, appColors: AppThemeColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = appColors.scoreTxt.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .width(60.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = appColors.accentPurple,
                trackColor = appColors.white.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$score%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.scoreTxt
            )
        }
    }
}

@Composable
fun ScoreGauge(score: Int, appColors: AppThemeColors, modifier: Modifier = Modifier) {
    val animatedScore = animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "score"
    )

    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 9.dp.toPx()
            drawCircle(
                color = appColors.white.copy(alpha = 0.4f),
                style = Stroke(width = strokeWidth),
                radius = size.minDimension / 2 - strokeWidth / 2
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(appColors.primaryBlue, appColors.accentPurple, appColors.primaryBlue)
                ),
                startAngle = -90f,
                sweepAngle = (animatedScore.value / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = appColors.scoreTxt
            )
            Text(
                text = "Score",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = appColors.scoreTxt.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ErrorScreen(
    errorMessage: String,
    onDismiss: () -> Unit,
    appColors: AppThemeColors
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = appColors.white),
            border = BorderStroke(1.dp, appColors.border.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(appColors.lightRedBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "warning logo",
                        tint = appColors.darkRedTxt,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Analysis Encountered an Error",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = appColors.textDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.textMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(26.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primaryBlue)
                ) {
                    Text(text = "Go Back & Refine", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun getFileName(context: android.content.Context, uri: android.net.Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            result = cursor.getString(index)
                        }
                    }
                } finally {
                    cursor.close()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error querying file name from ContentResolver: ${e.localizedMessage}", e)
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}
