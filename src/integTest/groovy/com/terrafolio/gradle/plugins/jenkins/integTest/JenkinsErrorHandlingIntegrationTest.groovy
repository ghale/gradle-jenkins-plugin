package com.terrafolio.gradle.plugins.jenkins.integTest

import nebula.test.functional.ExecutionResult

class JenkinsErrorHandlingIntegrationTest extends AbstractJenkinsIntegrationTest {
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

    def "provides sensible error when server is unreachable" () {
        ServerSocket socket = new ServerSocket(0)
        def badPort = socket.getLocalPort()
        socket.close()

        needsCleanup = false
        buildFile << """
            jenkins.servers.${serverName}.url = "http://localhost:${badPort}"
        """

        when:
        ExecutionResult result = runTasks('updateJenkinsItems')

        then:
        result.failure
        result.standardError.contains("Jenkins Service Call failed")
        result.standardError.contains("Connection to http://localhost:${badPort} refused")
    }

    def "provides sensible error when url path is wrong" () {
        needsCleanup = false
        buildFile << """
            jenkins.jobs.test_job.serviceOverrides {
                update = [ uri: "testUri" ]
                create = [ uri: "testUri" ]
            }
        """

        when:
        ExecutionResult result = runTasks('updateJenkinsItems')

        then:
        result.failure
        result.standardError.contains("Jenkins Service Call failed")
        result.standardError.contains("Not Found")
    }
}
