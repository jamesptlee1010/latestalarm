package com.james.mathwakealarm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionEngineTest {
    @Test
    fun generatedMathsAnswersValidate() {
        repeat(2_000) {
            val question = QuestionEngine.next(listOf(Topic.MATHS))
            assertTrue(QuestionEngine.isCorrect(question, question.answer))
        }
    }

    @Test
    fun wrongAnswerDoesNotValidate() {
        val question = QuestionEngine.Question("test", Topic.MATHS, "8 × 12 = ?", "96")
        assertTrue(QuestionEngine.isCorrect(question, "96"))
        assertFalse(QuestionEngine.isCorrect(question, "95"))
    }

    @Test
    fun textAnswersIgnoreCaseAndPunctuation() {
        val question = QuestionEngine.Question("test", Topic.GEOGRAPHY, "Capital?", "Canberra")
        assertTrue(QuestionEngine.isCorrect(question, "  CANBERRA! "))
    }
}
