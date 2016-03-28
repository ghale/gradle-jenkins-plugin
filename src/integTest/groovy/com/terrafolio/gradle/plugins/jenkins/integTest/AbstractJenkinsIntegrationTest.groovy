package com.terrafolio.gradle.plugins.jenkins.integTest

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

abstract class AbstractJenkinsIntegrationTest extends IntegrationSpec {
    def serverName = "test"
    def needsCleanup = true

    def setup() {
        fork = true
        buildFile << """
            buildscript {
                dependencies {
                    classpath files('${System.getProperty('jenkins.plugin').replaceAll("\\\\", "/")}')
                }
            }

            apply plugin: 'com.terrafolio.jenkins'

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

    @Override
    protected ExecutionResult runTasks(String... tasks) {
        ExecutionResult result = super.runTasks(tasks)
        println "<========= Standard Output =========>"
        println result.standardOutput
        println "<========= Standard Error  =========>"
        println result.standardError
        return result
    }

    def cleanup() {
        if (needsCleanup) {
            runTasks("deleteJenkinsItems")
        }
    }
}
