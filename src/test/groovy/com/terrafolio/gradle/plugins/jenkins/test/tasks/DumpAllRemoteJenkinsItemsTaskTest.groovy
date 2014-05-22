package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.tasks.AbstractJenkinsTask

class DumpAllRemoteJenkinsItemsTaskTest extends JenkinsPluginTaskSpec {
    @Override
    AbstractJenkinsTask createTaskUnderTest() {
        return project.tasks.dumpRemoteJenkinsItems
    }

    def "execute dumps remote items" () {
        setup:
        def remoteDir = new File(project.buildDir, "remotes")
        def xml = BASE_JOB_XML

        when:
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            3 * getConfiguration(_,_) >> { xml }
        }
        remoteDir.exists()
        new File(remoteDir, "/test1/jobs/compile_master.xml").text == BASE_JOB_XML
        new File(remoteDir, "/test1/jobs/compile_develop.xml").text == BASE_JOB_XML
        new File(remoteDir, "/test1/views/test view.xml").text == BASE_JOB_XML
    }
}
