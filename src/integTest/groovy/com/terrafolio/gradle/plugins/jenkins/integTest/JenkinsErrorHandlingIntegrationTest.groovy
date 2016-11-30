package com.terrafolio.gradle.plugins.jenkins.integTest

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
        TaskResults results = fails('updateJenkinsItems')

        then:
        results.allFailed
        results.output.contains("Jenkins Service Call failed")
        results.output.contains("Connection to http://localhost:${badPort} refused")
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
        TaskResults results = fails('updateJenkinsItems')

        then:
        results.allFailed
        results.output.contains("Jenkins Service Call failed")
        results.output.contains("Not Found")
    }
}
