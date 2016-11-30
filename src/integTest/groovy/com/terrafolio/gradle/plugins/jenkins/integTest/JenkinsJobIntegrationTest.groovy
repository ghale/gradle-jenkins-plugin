package com.terrafolio.gradle.plugins.jenkins.integTest

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
        TaskResults results = succeeds('updateJenkinsItems')

        then:
        results.allSucceeded
        results.output.contains("Creating new item test_job")

        when:
        results = succeeds('updateJenkinsItems')

        then:
        results.allSucceeded
        results.output.contains("Jenkins item test_job has no changes to the existing item")

        when:
        results = succeeds('dumpRemoteJenkinsItems')

        then:
        results.allSucceeded
        fileExists("build/remotes/${serverName}/jobs/test_job.xml")

        when:
        results = succeeds('validateJenkinsItems')

        then:
        results.allSucceeded

        when:
        results = succeeds('deleteJenkinsItems')

        then:
        results.allSucceeded
        results.output.contains("Deleting item test_job")
    }

    def "can force update a job" () {
        when:
        TaskResults results = succeeds('updateJenkinsItems')

        then:
        results.allSucceeded
        results.output.contains("Creating new item test_job")

        when:
        buildFile << """
            project.ext.forceJenkinsJobsUpdate = 'true'
        """
        results = succeeds('updateJenkinsItems')

        then:
        results.allSucceeded
        results.output.contains("Updating item test_job")
    }
}
