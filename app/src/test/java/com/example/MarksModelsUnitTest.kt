package com.example

import com.example.data.model.Attempt
import com.example.data.model.Category
import com.example.data.model.Question
import com.example.data.model.QuestionPaletteStatus
import com.example.data.model.Test as MarksTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarksModelsUnitTest {

    @Test
    fun category_defaultValues_areValid() {
        val category = Category(name = "Physics", icon = "physics", order = 1)
        assertEquals("Physics", category.name)
        assertEquals(1, category.order)
        assertTrue(category.id.isEmpty())
    }

    @Test
    fun test_defaultValues_areValid() {
        val test = MarksTest(
            title = "Mechanics Challenge",
            duration = 15,
            totalMarks = 20.0,
            negativeMarking = 1.0,
            totalQuestions = 5
        )
        assertEquals(15, test.duration)
        assertEquals(20.0, test.totalMarks, 0.01)
        assertEquals(1.0, test.negativeMarking, 0.01)
    }

    @Test
    fun question_options_verification() {
        val question = Question(
            text = "What is acceleration due to gravity on Earth?",
            options = listOf("9.8 m/s²", "8.9 m/s²", "10.8 m/s²", "7.8 m/s²"),
            optionImages = listOf("", "https://example.com/opt2.png", "", ""),
            solutionImageUrl = "https://example.com/sol.png",
            correctIndex = 0,
            marks = 4.0
        )
        assertEquals(4, question.options.size)
        assertEquals(0, question.correctIndex)
        assertEquals("9.8 m/s²", question.options[question.correctIndex])
        assertEquals("https://example.com/opt2.png", question.optionImages[1])
        assertEquals("https://example.com/sol.png", question.solutionImageUrl)
    }

    @Test
    fun attempt_scoreCalculation_logic() {
        val totalMarks = 20.0
        val correctCount = 4
        val wrongCount = 1
        val skippedCount = 0
        val marksPerQuestion = 4.0
        val negativeMarks = 1.0

        val computedScore = (correctCount * marksPerQuestion) - (wrongCount * negativeMarks)
        val attempt = Attempt(
            score = computedScore,
            totalMarks = totalMarks,
            correctCount = correctCount,
            wrongCount = wrongCount,
            skippedCount = skippedCount
        )

        assertEquals(15.0, attempt.score, 0.01)
        val percentage = (attempt.score / attempt.totalMarks) * 100
        assertEquals(75.0, percentage, 0.01)
    }

    @Test
    fun mathFormatter_latexAndMathML_formatting() {
        val latexIntegral = """$\int_0^{10 \pi}|\sin x| d x$"""
        val formattedInt = com.example.util.MathFormatter.formatMath(latexIntegral)
        assertTrue(formattedInt.contains("∫"))
        assertTrue(formattedInt.contains("sin x") || formattedInt.contains("sin(x)"))

        val mathML = "<math><mfrac><mn>1</mn><mn>2</mn></mfrac></math>"
        val formattedML = com.example.util.MathFormatter.formatMath(mathML)
        assertEquals("½", formattedML)

        val sqrtExpr = """\sqrt{x^2 + y^2}"""
        val formattedSqrt = com.example.util.MathFormatter.formatMath(sqrtExpr)
        assertTrue(formattedSqrt.contains("√"))
        assertTrue(formattedSqrt.contains("x²") || formattedSqrt.contains("x² + y²"))
    }
}
