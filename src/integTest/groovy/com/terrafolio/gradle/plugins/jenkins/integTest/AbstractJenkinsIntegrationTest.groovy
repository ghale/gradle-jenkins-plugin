package com.terrafolio.gradle.plugins.jenkins.integTest

import nebula.test.IntegrationSpec

abstract class AbstractJenkinsIntegrationTest extends IntegrationSpec {
    def serverName = "test"
    def needsCleanup = true

    def setup() {
        buildFile << """
            buildscript {
                dependencies {
                    classpath files('${System.getProperty('jenkins.plugin')}')
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

    def cleanup() {
        if (needsCleanup) {
            runTasks("deleteJenkinsItems")
        }
    }
}
