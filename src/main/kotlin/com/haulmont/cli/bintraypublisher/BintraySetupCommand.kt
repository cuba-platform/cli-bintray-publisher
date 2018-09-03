package com.haulmont.cli.bintraypublisher

import com.beust.jcommander.Parameters
import com.haulmont.cuba.cli.PrintHelper
import com.haulmont.cuba.cli.Resources
import com.haulmont.cuba.cli.commands.GeneratorCommand
import com.haulmont.cuba.cli.generation.Properties
import com.haulmont.cuba.cli.generation.Snippets
import com.haulmont.cuba.cli.generation.TemplateProcessor
import com.haulmont.cuba.cli.kodein
import com.haulmont.cuba.cli.prompting.Answers
import com.haulmont.cuba.cli.prompting.QuestionsList
import org.kodein.di.generic.instance
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths

@Parameters(commandDescription = "Prepares build.gradle to publishing to Bintray")
class BintraySetupCommand : GeneratorCommand<BintrayModel>() {

    private val printWriter: PrintWriter by kodein.instance()
    private val printHelper: PrintHelper by kodein.instance()

    private val resources by Resources.fromMyPlugin()

    private val snippets = Snippets(resources, "bintray")

    private val gradlePropertiesPath = Paths.get(System.getProperty("user.home"), ".gradle", "gradle.properties")

    override fun preExecute() {
        checkProjectExistence()
    }

    override fun getModelName(): String = "bintray"

    override fun QuestionsList.prompting() {
        val gradleProperties = Properties(gradlePropertiesPath)

        val (user, pass) = gradleProperties.let {
            it["cubaCliBintrayUser"] to it["cubaCliBintrayApiKey"]
        }

        val askCredentials = user == null || pass == null


        if (askCredentials) {
            question("bintrayUserName", "Bintray user name") {
                validate {
                    checkIsNotBlank()
                }
            }

            question("bintrayApiKey", "Bintray api key") {
                validate {
                    checkIsNotBlank()
                }
            }
        }

        var buildGradleText = projectStructure.buildGradle.let { Files.newInputStream(it) }
                .reader()
                .use {
                    it.readText()
                }

        if (!buildGradleText.contains(snippets["bintrayPlugin"])) {

            question("repositoryName", "Repository name") {
                default("main")

                validate {
                    checkIsNotBlank()
                }
            }

            question("packageName", "Package name") {

                default(projectModel.name)

                validate {
                    checkIsNotBlank()
                }
            }

            confirmation("hasOrganization", "Specify organization?")

            question("organization", "Organization name") {
                askIf("hasOrganization")

                validate {
                    checkIsNotBlank()
                }
            }

            question("websiteUrl", "Web site url")
            question("issueTrackerUrl", "Issue tracker url")
            question("vcsUrl", "Vcs url") {
                validate {
                    checkIsNotBlank("Specify vcs url")
                }
            }

            repeating("licenses", "Add license?") {
                question("license", "License name") {
                    validate {
                        checkIsNotBlank()
                    }
                }
            }

            repeating("labels", "Add label?") {
                question("label", "Label name") {
                    validate {
                        checkIsNotBlank()
                    }
                }
            }
        }
    }

    override fun createModel(answers: Answers): BintrayModel = BintrayModel(answers)

    override fun generate(bindings: Map<String, Any>) {
        model.bintrayUserName?.let {
            val gradleProperties = Properties(gradlePropertiesPath)

            gradleProperties["cubaCliBintrayUser"] = model.bintrayUserName!!
            gradleProperties["cubaCliBintrayApiKey"] = model.bintrayApiKey!!

            gradleProperties.save()
        }

        var buildGradleModified = false

        var buildGradleText = projectStructure.buildGradle.let { Files.newInputStream(it) }
                .reader()
                .use {
                    it.readText()
                }

        if (!buildGradleText.contains(snippets["buildScriptBintrayClasspath"])) {
            buildGradleText = buildGradleText.replace(snippets["buildScriptCubaClasspath"], snippets["buildScriptSnippet"])
            buildGradleModified = true
        }

        if (model.packageName != null) {
            buildGradleModified = true

            val bintrayTemplate = TemplateProcessor(resources.getTemplate("bintray"), bindings)

            val bintrayConfiguration = ByteArrayOutputStream().use {
                bintrayTemplate.transform("bintrayConfiguration", it)
                String(it.toByteArray())
            }

            if (buildGradleText.contains(snippets["subprojects"])) {
                buildGradleText.replace(snippets["subprojects"], snippets["subprojects"] + "\n" + bintrayConfiguration)
            } else {
                buildGradleText += buildString {
                    append("\n")
                    append(snippets["subprojects"])
                    append("\n")
                    append(bintrayConfiguration)
                    append("}")
                }
            }
        }

        if (buildGradleModified) {

            projectStructure.buildGradle.let { Files.newOutputStream(it) }.use {
                it.bufferedWriter().apply {
                    write(buildGradleText)
                    flush()
                }
            }

            printHelper.fileModified(projectStructure.buildGradle)
        }

        if(model.bintrayUserName == null && !buildGradleModified) {
            printWriter.println("Your project already has bintray plugin configured")
        }
    }
}