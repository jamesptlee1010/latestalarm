package com.james.mathwakealarm

import kotlin.math.abs
import kotlin.random.Random

object QuestionEngine {
    data class Question(
        val id: String,
        val topic: Topic,
        val prompt: String,
        val answer: String,
        val acceptedAnswers: List<String> = emptyList()
    )

    private val fixedQuestions = listOf(
        Question("ww2_01", Topic.WORLD_WAR_II, "In what year did World War II begin in Europe?", "1939"),
        Question("ww2_02", Topic.WORLD_WAR_II, "Which country did Germany invade in September 1939?", "Poland"),
        Question("ww2_03", Topic.WORLD_WAR_II, "What was the code name for the Allied invasion of Normandy?", "Operation Overlord", listOf("Overlord")),
        Question("ww2_04", Topic.WORLD_WAR_II, "On what date did D-Day occur?", "6 June 1944", listOf("June 6 1944", "6/6/1944")),
        Question("ww2_05", Topic.WORLD_WAR_II, "Which battle is widely considered the turning point on the Eastern Front?", "Stalingrad", listOf("Battle of Stalingrad")),
        Question("ww2_06", Topic.WORLD_WAR_II, "Who was British prime minister for most of World War II?", "Winston Churchill", listOf("Churchill")),
        Question("ww2_07", Topic.WORLD_WAR_II, "What was the name of Germany's air force?", "Luftwaffe"),
        Question("ww2_08", Topic.WORLD_WAR_II, "Which 1941 attack brought the United States into World War II?", "Pearl Harbor", listOf("Attack on Pearl Harbor")),
        Question("ww2_09", Topic.WORLD_WAR_II, "Which city was the first target of an atomic bomb in war?", "Hiroshima"),
        Question("ww2_10", Topic.WORLD_WAR_II, "In what year did World War II end?", "1945"),

        Question("jung_01", Topic.CARL_JUNG, "Carl Jung founded which school of psychology?", "Analytical psychology"),
        Question("jung_02", Topic.CARL_JUNG, "What term did Jung use for universal inherited patterns?", "Archetypes", listOf("Archetype")),
        Question("jung_03", Topic.CARL_JUNG, "What is Jung's term for the hidden, rejected side of personality?", "Shadow", listOf("The shadow")),
        Question("jung_04", Topic.CARL_JUNG, "What did Jung call the process of becoming a whole self?", "Individuation"),
        Question("jung_05", Topic.CARL_JUNG, "Which two broad attitudes did Jung describe?", "Introversion and extraversion", listOf("Introvert and extrovert", "Introversion and extroversion")),
        Question("jung_06", Topic.CARL_JUNG, "What is Jung's term for humanity's shared inherited unconscious?", "Collective unconscious", listOf("The collective unconscious")),
        Question("jung_07", Topic.CARL_JUNG, "Which Swiss city is closely associated with Jung's career?", "Zurich", listOf("Zürich")),
        Question("jung_08", Topic.CARL_JUNG, "What was Carl Jung's profession?", "Psychiatrist", listOf("Psychologist", "Psychiatrist and psychoanalyst")),

        Question("c20_01", Topic.TWENTIETH_CENTURY, "In what year did the Berlin Wall fall?", "1989"),
        Question("c20_02", Topic.TWENTIETH_CENTURY, "Who was the first person to walk on the Moon?", "Neil Armstrong", listOf("Armstrong")),
        Question("c20_03", Topic.TWENTIETH_CENTURY, "In what year did the Soviet Union dissolve?", "1991"),
        Question("c20_04", Topic.TWENTIETH_CENTURY, "Which crisis in 1962 brought the US and USSR close to nuclear war?", "Cuban Missile Crisis"),
        Question("c20_05", Topic.TWENTIETH_CENTURY, "Who delivered the 'I Have a Dream' speech?", "Martin Luther King Jr", listOf("Martin Luther King", "MLK")),
        Question("c20_06", Topic.TWENTIETH_CENTURY, "Which ship sank on its maiden voyage in 1912?", "Titanic", listOf("RMS Titanic")),
        Question("c20_07", Topic.TWENTIETH_CENTURY, "What year did humans first land on the Moon?", "1969"),
        Question("c20_08", Topic.TWENTIETH_CENTURY, "Which South African leader became president in 1994?", "Nelson Mandela", listOf("Mandela")),

        Question("geo_01", Topic.GEOGRAPHY, "What is the capital of Australia?", "Canberra"),
        Question("geo_02", Topic.GEOGRAPHY, "What is the largest ocean on Earth?", "Pacific Ocean", listOf("Pacific")),
        Question("geo_03", Topic.GEOGRAPHY, "Which river flows through London?", "Thames", listOf("River Thames")),
        Question("geo_04", Topic.GEOGRAPHY, "Mount Kilimanjaro is in which country?", "Tanzania"),
        Question("geo_05", Topic.GEOGRAPHY, "What is the capital of Japan?", "Tokyo"),
        Question("geo_06", Topic.GEOGRAPHY, "Which continent contains the Sahara Desert?", "Africa"),
        Question("geo_07", Topic.GEOGRAPHY, "What is the smallest Australian state by area?", "Tasmania"),
        Question("geo_08", Topic.GEOGRAPHY, "Which country has the city of Barcelona?", "Spain"),

        Question("sci_01", Topic.SCIENCE, "What planet is known as the Red Planet?", "Mars"),
        Question("sci_02", Topic.SCIENCE, "What gas do plants absorb from the atmosphere?", "Carbon dioxide", listOf("CO2")),
        Question("sci_03", Topic.SCIENCE, "How many bones are in a typical adult human body?", "206"),
        Question("sci_04", Topic.SCIENCE, "What is the chemical symbol for gold?", "Au"),
        Question("sci_05", Topic.SCIENCE, "What force keeps planets in orbit?", "Gravity"),
        Question("sci_06", Topic.SCIENCE, "What is H2O commonly called?", "Water"),
        Question("sci_07", Topic.SCIENCE, "Which organ pumps blood around the body?", "Heart", listOf("The heart")),
        Question("sci_08", Topic.SCIENCE, "What is the nearest star to Earth?", "Sun", listOf("The Sun")),

        Question("sport_01", Topic.SPORT, "How many players does a football team have on the pitch at kick-off?", "11", listOf("Eleven")),
        Question("sport_02", Topic.SPORT, "How long is an Olympic swimming pool in metres?", "50", listOf("50 metres", "50m")),
        Question("sport_03", Topic.SPORT, "In tennis, what word means a score of zero?", "Love"),
        Question("sport_04", Topic.SPORT, "How many points is a try worth in rugby union?", "5", listOf("Five")),
        Question("sport_05", Topic.SPORT, "Which country hosted the 2000 Summer Olympics?", "Australia", listOf("Sydney", "Australia Sydney")),
        Question("sport_06", Topic.SPORT, "What colour jersey is worn by the Tour de France leader?", "Yellow"),

        Question("gk_01", Topic.GENERAL_KNOWLEDGE, "How many days are in a leap year?", "366"),
        Question("gk_02", Topic.GENERAL_KNOWLEDGE, "Which instrument has 88 keys?", "Piano"),
        Question("gk_03", Topic.GENERAL_KNOWLEDGE, "What is the currency of Japan?", "Yen"),
        Question("gk_04", Topic.GENERAL_KNOWLEDGE, "Who wrote 1984?", "George Orwell", listOf("Orwell")),
        Question("gk_05", Topic.GENERAL_KNOWLEDGE, "How many sides does a hexagon have?", "6", listOf("Six")),
        Question("gk_06", Topic.GENERAL_KNOWLEDGE, "What colour do blue and yellow make?", "Green"),

        Question("logic_01", Topic.LOGIC, "Complete the sequence: 2, 4, 8, 16, ?", "32"),
        Question("logic_02", Topic.LOGIC, "Complete the sequence: 1, 1, 2, 3, 5, ?", "8"),
        Question("logic_03", Topic.LOGIC, "If all cats are animals and Taz is a cat, is Taz an animal?", "Yes", listOf("Y")),
        Question("logic_04", Topic.LOGIC, "What comes next: A, C, E, G, ?", "I"),
        Question("logic_05", Topic.LOGIC, "If yesterday was Monday, what day is tomorrow?", "Wednesday")
    )

    fun next(topics: List<Topic>, recentIds: Collection<String> = emptyList()): Question {
        val chosenTopics = topics.ifEmpty { Topic.entries.toList() }
        if (Topic.MATHS in chosenTopics && (chosenTopics.size == 1 || Random.nextInt(100) < 45)) {
            return mathsQuestion()
        }
        val pool = fixedQuestions.filter { it.topic in chosenTopics && it.id !in recentIds }
            .ifEmpty { fixedQuestions.filter { it.topic in chosenTopics } }
            .ifEmpty { fixedQuestions }
        return pool.random()
    }

    fun isCorrect(question: Question, input: String): Boolean {
        val actual = normalise(input)
        val expected = listOf(question.answer) + question.acceptedAnswers
        if (expected.any { normalise(it) == actual }) return true

        val actualNumber = actual.replace(',', '.').toDoubleOrNull()
        val expectedNumber = question.answer.replace(',', '.').toDoubleOrNull()
        return actualNumber != null && expectedNumber != null && abs(actualNumber - expectedNumber) < 0.011
    }

    private fun normalise(value: String): String = value.trim().lowercase()
        .replace(Regex("[^a-z0-9.]+"), " ")
        .trim()

    private fun mathsQuestion(): Question {
        return when (Random.nextInt(4)) {
            0 -> {
                val a = Random.nextInt(12, 100)
                val b = Random.nextInt(7, 80)
                Question("math_${System.nanoTime()}", Topic.MATHS, "$a + $b = ?", (a + b).toString())
            }
            1 -> {
                val a = Random.nextInt(30, 120)
                val b = Random.nextInt(5, a)
                Question("math_${System.nanoTime()}", Topic.MATHS, "$a − $b = ?", (a - b).toString())
            }
            2 -> {
                val a = Random.nextInt(4, 13)
                val b = Random.nextInt(4, 13)
                Question("math_${System.nanoTime()}", Topic.MATHS, "$a × $b = ?", (a * b).toString())
            }
            else -> {
                val divisor = Random.nextInt(3, 13)
                val quotient = Random.nextInt(4, 16)
                val dividend = divisor * quotient
                Question("math_${System.nanoTime()}", Topic.MATHS, "$dividend ÷ $divisor = ?", quotient.toString())
            }
        }
    }
}
