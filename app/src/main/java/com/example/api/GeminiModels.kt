package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

// --- Custom Domain Model For Resume Analysis ---
@JsonClass(generateAdapter = true)
data class ResumeAnalysisResponse(
    @Json(name = "overallScore") val overallScore: Int,
    @Json(name = "summary") val summary: String,
    @Json(name = "strengths") val strengths: List<String>,
    @Json(name = "weaknesses") val weaknesses: List<String>,
    @Json(name = "categoryScores") val categoryScores: CategoryScores,
    @Json(name = "suggestions") val suggestions: List<RewriteSuggestion>,
    @Json(name = "missingKeywords") val missingKeywords: List<String>,
    @Json(name = "tailoredPitch") val tailoredPitch: String
)

@JsonClass(generateAdapter = true)
data class CategoryScores(
    @Json(name = "impact") val impact: Int,         // Score out of 100 for bullet point impact
    @Json(name = "formatting") val formatting: Int, // Score out of 100 for section layout and completeness
    @Json(name = "alignment") val alignment: Int    // Score out of 100 for alignment with the job description
)

@JsonClass(generateAdapter = true)
data class RewriteSuggestion(
    @Json(name = "category") val category: String,  // e.g. "Impact & Action Verbs", "Keywords", "Metrics"
    @Json(name = "issue") val issue: String,
    @Json(name = "bestPractice") val bestPractice: String,
    @Json(name = "before") val before: String,
    @Json(name = "after") val after: String
)
