package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.tasks.AbstractJenkinsTask

class DeleteAllJenkinsItemsTaskTest extends JenkinsPluginTaskSpec {
    @Override
    AbstractJenkinsTask createTaskUnderTest() {
        return project.tasks.deleteJenkinsItems
    }

    def "execute deletes all items" () {
        setup:
        def job1 = project.jenkins.jobs.compile_master.definition.name
        def job2 = project.jenkins.jobs.compile_develop.definition.name
        def view = project.jenkins.views."test view".name

        when:
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(job1,_) >> { "<project />" }
            1 * getConfiguration(job2,_) >> { "<project />" }
            1 * getConfiguration(view,_) >> { "<project />" }
            1 * deleteConfiguration(job1,_)
            1 * deleteConfiguration(job2,_)
            1 * deleteConfiguration(view,_)
            0 * getConfiguration(* _)
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes items on multiple servers" () {
        setup:
        def job1 = project.jenkins.jobs.compile_master.definition.name
        def job2 = project.jenkins.jobs.compile_develop.definition.name
        def view = project.jenkins.views."test view".name
        project.jenkins.jobs.each { job ->
            job.server project.jenkins.servers.test2
        }

        when:
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            2 * getConfiguration(job1,_) >> { "<project />" }
            2 * getConfiguration(job2,_) >> { "<project />" }
            1 * getConfiguration(view,_) >> { "<project />" }
            2 * deleteConfiguration(job1,_)
            2 * deleteConfiguration(job2,_)
            1 * deleteConfiguration(view,_)
            0 * getConfiguration(* _)
            0 * deleteConfiguration(* _)
        }
    }
}
