package com.terrafolio.gradle.plugins.jenkins.integTest

import nebula.test.functional.ExecutionResult

class JenkinsJobIntegrationTest extends AbstractJenkinsIntegrationTest {
    def setup() {
        buildFile << """
            jenkins {
                jobs {
                    test_job {
                        dsl { displayName 'jenkins test job' }
                    }
                }
            }
        """
    }

    def "can create/dump/validate/delete job in jenkins" () {
        when:
        ExecutionResult result = runTasks('updateJenkinsItems')

        then:
        result.success
        result.standardOutput.contains("Creating new item test_job")

        when:
        result = runTasks('dumpRemoteJenkinsItems')

        then:
        result.success
        fileExists("build/remotes/${serverName}/jobs/test_job.xml")

        when:
        result = runTasks('validateJenkinsItems')

        then:
        result.success

        when:
        result = runTasks('deleteJenkinsItems')

        then:
        result.success
        result.standardOutput.contains("Deleting item test_job")
    }
}
