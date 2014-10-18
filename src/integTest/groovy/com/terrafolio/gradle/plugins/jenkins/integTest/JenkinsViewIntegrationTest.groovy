package com.terrafolio.gradle.plugins.jenkins.integTest

import nebula.test.functional.ExecutionResult

class JenkinsViewIntegrationTest extends AbstractJenkinsIntegrationTest {
    def setup() {
        buildFile << """
            jenkins {
                jobs {
                    test_job {
                        dsl { displayName 'jenkins test job' }
                    }
                }
                views {
                    test_view {
                        dsl {
                            jobs {
                                name('test_job')
                            }
                            columns {
                                name()
                                buildButton()
                            }
                        }
                    }
                }
            }
        """
    }

    def "can create/dump/delete view in jenkins" () {
        when:
        ExecutionResult result = runTasks('updateJenkinsItems')

        then:
        result.success
        result.standardOutput.contains("Creating new item test_view")

        when:
        result = runTasks('dumpRemoteJenkinsItems')

        then:
        result.success
        fileExists("build/remotes/${serverName}/views/test_view.xml")

        when:
        result = runTasks('deleteJenkinsItems')

        then:
        result.success
        result.standardOutput.contains("Deleting item test_view")
    }
}
