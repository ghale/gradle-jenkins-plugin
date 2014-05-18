package com.terrafolio.gradle.plugins.jenkins.test.tasks

import org.gradle.api.Incubating
import org.junit.Rule
import org.junit.Test

@Incubating
class DumpRemoteJenkinsItemsTaskTest {

    @Rule
    public TestFixtures fixtures = new TestFixtures()

    @Test
    def void execute_dumpItems() {
        fixtures.mockJenkinsRESTService.demand.with {
            3.times {
                getConfiguration() { String jobName, Map overrides -> TestFixtures.BASE_JOB_XML }
            }
        }

        fixtures.mockJenkinsRESTService.use {
            fixtures.project.tasks.dumpRemoteJenkinsItemsTest1.execute()
        }

        def remoteDir = new File(fixtures.project.buildDir, "remotes")
        assert remoteDir.exists()
        assert new File(remoteDir, "/test1/jobs/compile_master.xml").text == TestFixtures.BASE_JOB_XML
        assert new File(remoteDir, "/test1/jobs/compile_develop.xml").text == TestFixtures.BASE_JOB_XML
        assert new File(remoteDir, "/test1/views/test view.xml").text == TestFixtures.BASE_JOB_XML
    }
}
