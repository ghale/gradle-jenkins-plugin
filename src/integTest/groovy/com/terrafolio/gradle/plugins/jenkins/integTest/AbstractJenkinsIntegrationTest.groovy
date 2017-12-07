package com.terrafolio.gradle.plugins.jenkins.integTest

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

abstract class AbstractJenkinsIntegrationTest extends Specification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    def serverName = "test"
    def needsCleanup = true
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """
            plugins {
                id 'com.sbelei.jenkins'
            }

            jenkins {
                servers {
                    ${serverName} {
                        url 'http://localhost:${System.getProperty('jenkins.port')}'
                        secure false
                    }
                }
                defaultServer servers.test
            } 
        """
    }

    TaskResults succeeds(String... tasks) {
        BuildResult result = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.root)
                .withArguments(tasks)
                .build()
        println result.output
        return new TaskResults(result)
    }

    TaskResults fails(String... tasks) {
        BuildResult result = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.root)
                .withArguments(tasks)
                .buildAndFail()
        println result.output
        return new TaskResults(result)
    }

    boolean fileExists(String relativePath) {
        return new File(testProjectDir.root, relativePath).exists()
    }

    def cleanup() {
        if (needsCleanup) {
            succeeds("deleteJenkinsItems")
        }
    }
}
