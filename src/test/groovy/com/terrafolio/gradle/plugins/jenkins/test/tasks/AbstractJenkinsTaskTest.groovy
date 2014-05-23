package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.tasks.AbstractJenkinsTask
import com.terrafolio.gradle.plugins.jenkins.tasks.DumpJenkinsItemsTask
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurationException

/**
 * Created by ghale on 5/23/14.
 */
class AbstractJenkinsTaskTest extends JenkinsPluginTaskSpec {
    @Override
    AbstractJenkinsTask createTaskUnderTest() {
        return project.task('taskUnderTest', type: DumpJenkinsItemsTask)
    }

    def "getServerDefinitions uses default server when no server specified" () {
        setup:
        project.jenkins.jobs.compile_master.serverDefinitions = []
        project.jenkins.defaultServer project.jenkins.servers.test1

        expect:
        taskUnderTest.getServerDefinitions(project.jenkins.jobs.compile_master) == [ project.jenkins.servers.test1 ]
    }

    def "getServerDefinitions throws exception on missing server" () {
        setup:
        project.jenkins.jobs.compile_master.serverDefinitions = []

        when:
        taskUnderTest.getServerDefinitions(project.jenkins.jobs.compile_master)

        then:
        thrown JenkinsConfigurationException
    }

    def "getServerDefinitions observes jenkinsServerFilter" () {
        setup:
        project.jenkins.jobs.compile_master.server project.jenkins.servers.test2
        project.ext.jenkinsServerFilter = '.*1'

        expect:
        taskUnderTest.getServerDefinitions(project.jenkins.jobs.compile_master) == [ project.jenkins.servers.test1 ]
    }

    def "eachServer only executes on task-configured server" () {
        setup:
        def int count = 0
        project.jenkins.jobs.compile_master.server project.jenkins.servers.test2
        taskUnderTest.servers = [ project.jenkins.servers.test1 ]

        when:
        taskUnderTest.eachServer(project.jenkins.jobs.compile_master) { server, service ->
            count++
        }

        then:
        count == 1
    }

    def "getAllItems gets all configured items" () {
        setup:
        taskUnderTest.dump(project.jenkins.jobs.compile_master)
        taskUnderTest.dump { project.jenkins.jobs.compile_develop }

        expect:
        taskUnderTest.getAllItems() == [ project.jenkins.jobs.compile_master, project.jenkins.jobs.compile_develop ]
    }

    def "getAllItems observes jenkinsJobFilter" () {
        setup:
        taskUnderTest.dump(project.jenkins.jobs.compile_master)
        taskUnderTest.dump(project.jenkins.jobs.compile_develop)
        project.ext.jenkinsJobFilter = '.*master'

        expect:
        taskUnderTest.getAllItems() == [ project.jenkins.jobs.compile_master ]
    }
}
