package com.example.ai

import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import kotlinx.coroutines.tasks.await

/**
 * AI Doubt Solver client.
 *
 * SECURITY MODEL: no AI provider key exists on the device, ever. This calls the
 * `askAiDoubt` Cloud Function, which enforces sign-in, App Check and a
 * per-user daily quota server-side, and only then talks to the AI provider.
 * The provider key lives exclusively in Firebase server-side config.
 */
object AiDoubtSolver {

    data class DoubtResult(val answer: String, val remainingToday: Int)

    /**
     * @param question The doubt text (5-2000 chars)
     * @param options Optional multiple-choice options
     * @param exam e.g. "JEE", "NEET"
     * @param subject e.g. "Physics"
     */
    suspend fun ask(
        question: String,
        options: List<String> = emptyList(),
        exam: String = "",
        subject: String = ""
    ): DoubtResult {
        val payload = mutableMapOf<String, Any>(
            "question" to question.trim()
        )
        if (options.isNotEmpty()) payload["options"] = options
        if (exam.isNotBlank()) payload["exam"] = exam
        if (subject.isNotBlank()) payload["subject"] = subject

        return try {
            val result = Firebase.functions
                .getHttpsCallable("askAiDoubt")
                .call(payload)
                .await()
            @Suppress("UNCHECKED_CAST")
            val data = result.data as? Map<String, Any?>
            DoubtResult(
                answer = data?.get("answer") as? String ?: "No answer received.",
                remainingToday = (data?.get("remaining") as? Long ?: -1L).toInt()
            )
        } catch (e: Exception) {
            throw when {
                e.message?.contains("resource-exhausted") == true ->
                    IllegalStateException("Daily AI doubt limit reached. Try again tomorrow.")
                e.message?.contains("unauthenticated") == true ->
                    IllegalStateException("Please sign in to use the AI doubt solver.")
                else -> IllegalStateException("AI unavailable: ${e.localizedMessage ?: "try again later"}")
            }
        }
    }

    /** Preflight check so UI can hide the feature when AI is not configured yet. */
    val isAvailable: Boolean get() = true // server decides; UI surfaces errors from call
}
