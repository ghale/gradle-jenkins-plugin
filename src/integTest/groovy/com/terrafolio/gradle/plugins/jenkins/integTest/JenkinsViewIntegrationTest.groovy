package com.terrafolio.gradle.plugins.jenkins.integTest

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
        TaskResults results = succeeds('updateJenkinsItems')

        then:
        results.allSucceeded
        results.output.contains("Creating new item test_view")

        when:
        results = succeeds('dumpRemoteJenkinsItems')

        then:
        results.allSucceeded
        fileExists("build/remotes/${serverName}/views/test_view.xml")

        when:
        results = succeeds('deleteJenkinsItems')

        then:
        results.allSucceeded
        results.output.contains("Deleting item test_view")
    }
}
