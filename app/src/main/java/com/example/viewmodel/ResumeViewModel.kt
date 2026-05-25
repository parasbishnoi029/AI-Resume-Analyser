package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.*
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface AnalysisUiState {
    object Idle : AnalysisUiState
    data class Loading(val message: String) : AnalysisUiState
    data class Success(val response: ResumeAnalysisResponse) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

class ResumeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.getDatabase(application)
    private val repository = AnalysisRepository(database.analysisRecordDao())

    val historyState: StateFlow<List<AnalysisRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)

    val resumeInput = MutableStateFlow("")
    val jobDescInput = MutableStateFlow("")

    fun setAndroidPreset() {
        resumeInput.value = """
            John Doe - Mobile Dev
            johndoe@email.com - Seattle, WA

            Summary:
            Experience designing mobile applications. Looking for projects.

            Experience:
            Mobile Dev - Alpha Corp (2023 - Present)
            - Responsible for fixing some bugs in the Android app.
            - Programmed user screens using Jetpack Compose.
            - Worked on performance issues.
            - Team player and attended scrum meetings.

            Education:
            BS in Computer Science (2022)
        """.trimIndent()
        
        jobDescInput.value = """
            Lead Android Engineer (Kotlin, Jetpack Compose, Coroutines)
            We are seeking a Lead Android Developer to spearhead our next-generation mobile platform.
            Key Requirements:
            - 5+ years of active production experience in Android development.
            - Deep expertise in Kotlin, Coroutines, Flow, and Jetpack Compose.
            - Hands-on experience optimizing network traffic, offline syncing, and DB performance.
            - Experience with CI/CD tools, Git, and writing Roborazzi/Robolectric unit tests.
            - Proactive leadership style to mentor junior developers and drive design patterns.
        """.trimIndent()
    }

    fun setWebPreset() {
        resumeInput.value = """
            Jane Smith - Software Engineer
            jane.smith@email.com - San Francisco, CA

            Overview:
            Web engineer with some React experience. Eager to work with databases.

            Work Experience:
            Engineer at Startup Inc (2021 - Present)
            - Worked on the frontend and backend of SaaS App.
            - Created and designed SQL databases.
            - Helped make the website load slightly faster.
            
            Technical Skills:
            React, Javascript, SQL.
        """.trimIndent()
        
        jobDescInput.value = """
            Senior Full-Stack Engineer (React, Node.js, PostgreSQL)
            Join our fast-growing enterprise SaaS company.
            Required Qualifications:
            - Expert-level React.js & TypeScript experience on high-traffic sites.
            - Strong backend Node.js core expertise with Express/NestJS.
            - Deep skills in tuning PostgreSQL indexes, query optimizations, and scalable schema design.
            - Experience implementing robust OAuth / OpenID Connect authentication pipelines.
        """.trimIndent()
    }

    fun analyzeResume() {
        val resume = resumeInput.value.trim()
        val jobDesc = jobDescInput.value.trim()

        if (resume.isEmpty()) {
            uiState.value = AnalysisUiState.Error("Please enter your resume text first!")
            return
        }

        viewModelScope.launch {
            uiState.value = AnalysisUiState.Loading("Reading styling and layout...")
            delay(800)
            uiState.value = AnalysisUiState.Loading("Evaluating action verbs and bullet point structure...")
            delay(800)
            if (jobDesc.isNotEmpty()) {
                uiState.value = AnalysisUiState.Loading("Correlating technical keyword alignment with job description...")
                delay(800)
            }
            uiState.value = AnalysisUiState.Loading("Synthesizing feedback with Gemini 3.5-Flash...")

            try {
                // Call Gemini REST API
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isEmpty()) {
                    uiState.value = AnalysisUiState.Error(
                        "Gemini API key is not configured. Please add GEMINI_API_KEY to your AI Studio secrets panel!"
                    )
                    return@launch
                }

                val systemInstructionText = """
                    You are an expert Applicant Tracking System (ATS) algorithm and a world-class executive recruiter.
                    Your role is to analyze resumes and guide candidates with highly actionable improvements.
                    Analyze bullet points for passive phrasing and metrics. Check if contact info exists.
                    If a Job Description is provided, calculate the alignment score and track missing/present key terms.
                    Provide exact side-by-side edit suggestions showing text BEFORE and improved rewrite AFTER.
                    
                    You MUST return your response ONLY as a single valid JSON object strictly matching this schema format:
                    {
                      "overallScore": Int, (0 to 100 representing readiness)
                      "summary": "String summarize main suggestions",
                      "strengths": ["String strength 1", "String strength 2"],
                      "weaknesses": ["String weakness 1", "String weakness 2"],
                      "categoryScores": {
                        "impact": Int, (score 0-100)
                        "formatting": Int, (score 0-100)
                        "alignment": Int (score 0-100; if no JD is provided, default to 80 based on standard industry expectations)
                      },
                      "suggestions": [
                        {
                          "category": "String category (e.g., 'Impact & Active Verbs')",
                          "issue": "String of the precise issue found",
                          "bestPractice": "Best practice tip",
                          "before": "Original bullet/sentence from resume that is weak",
                          "after": "High-impact rewritten version with action verbs and quantifiable placeholders"
                        }
                      ],
                      "missingKeywords": ["String keyword 1", "String keyword 2"],
                      "tailoredPitch": "Concise high-impact professional summary pitch tailored to this role"
                    }
                    
                    Start and end your output directly with curly braces. Do NOT wrap it in any ```json code block backticks as that will make JSON parsing fail. Only return clean, raw JSON.
                """.trimIndent()

                val prompt = """
                    Resume Text:
                    ---
                    $resume
                    ---
                    
                    Job Description (optional):
                    ---
                    ${if (jobDesc.isEmpty()) "NONE PROVIDED" else jobDesc}
                    ---
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.2f
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(apiKey, request)
                }

                val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (resultText != null) {
                    val cleanedJson = cleanJsonString(resultText)

                    val parsedResponse = withContext(Dispatchers.Default) {
                        RetrofitClient.resAdapter.fromJson(cleanedJson)
                    }

                    if (parsedResponse != null) {
                        repository.insert(
                            AnalysisRecord(
                                resumeText = resume,
                                jobDescription = jobDesc,
                                overallScore = parsedResponse.overallScore,
                                jsonResult = cleanedJson
                            )
                        )
                        uiState.value = AnalysisUiState.Success(parsedResponse)
                    } else {
                        uiState.value = AnalysisUiState.Error("Failed to parse structural response from Gemini.")
                    }
                } else {
                    uiState.value = AnalysisUiState.Error("Received empty response from Gemini API.")
                }

            } catch (e: Exception) {
                Log.e("ResumeViewModel", "Gemini call error", e)
                uiState.value = AnalysisUiState.Error("API Error: ${e.localizedMessage ?: e.message ?: "Unknown error"}")
            }
        }
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.substringAfter("```json").trim()
        } else if (str.startsWith("```")) {
            str = str.substringAfter("```").trim()
        }
        if (str.endsWith("```")) {
            str = str.substringBeforeLast("```").trim()
        }
        return str
    }

    fun deleteRecord(record: AnalysisRecord) {
        viewModelScope.launch {
            repository.deleteById(record.id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun loadResponseDirectly(record: AnalysisRecord) {
        viewModelScope.launch {
            try {
                val parsedResponse = withContext(Dispatchers.Default) {
                    RetrofitClient.resAdapter.fromJson(record.jsonResult)
                }
                if (parsedResponse != null) {
                    resumeInput.value = record.resumeText
                    jobDescInput.value = record.jobDescription
                    uiState.value = AnalysisUiState.Success(parsedResponse)
                }
            } catch (e: Exception) {
                Log.e("ResumeViewModel", "Failed to reload historical record", e)
            }
        }
    }

    fun resetState() {
        uiState.value = AnalysisUiState.Idle
    }
}
