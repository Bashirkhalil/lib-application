package com.example.testlib

object HelperQuestion {

    fun ini(fileName: String? =null) {
            val newName = fileName ?: "testlib"
            System.loadLibrary(newName)
    }

    external fun stringFromJNI(): String

    external fun askYourQuestion(question: String): String

    external fun enterBornYear(year: String): String


}