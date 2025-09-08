package com.example.studyflash.domain.model


sealed class FlashcardType(val key: String) {
    data object FrontBack : FlashcardType("FRONT_BACK")
    data object Cloze : FlashcardType("CLOZE")
    data object Typing : FlashcardType("TYPING")
    data object Mcq : FlashcardType("MCQ")
}