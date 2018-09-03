package com.haulmont.cli.bintraypublisher

import com.haulmont.cuba.cli.prompting.Answer
import com.haulmont.cuba.cli.prompting.Answers

class BintrayModel(answers: Answers) {
    private val clearedAnswers: Map<String, Answer?> = answers
            .mapValues {
                it.value.let {
                    if ((it is String) && (it.isBlank()))
                        return@let null
                    return@let it
                }
            }.withDefault { null }

    val bintrayUserName: String? by clearedAnswers

    val bintrayApiKey: String? by clearedAnswers

    val repositoryName: String? by clearedAnswers

    val packageName: String? by clearedAnswers
    val organization: String? by clearedAnswers

    val websiteUrl: String? by clearedAnswers

    val issueTrackerUrl: String? by clearedAnswers
    val vcsUrl: String? by clearedAnswers

    val licenses: String? = kotlin.run {
        return@run if ("licenses" in answers) {
            (answers["licenses"] as List<Answers>).map {
                it["license"] as String
            }.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        } else null
    }

    val labels: String? = kotlin.run {
        return@run if ("labels" in answers) {
            (answers["labels"] as List<Answers>).map {
                it["label"] as String
            }.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        } else null
    }
}