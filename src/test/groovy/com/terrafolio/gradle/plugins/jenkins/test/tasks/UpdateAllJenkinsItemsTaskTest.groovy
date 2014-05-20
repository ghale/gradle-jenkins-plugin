package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.tasks.AbstractJenkinsTask

class UpdateAllJenkinsItemsTaskTest extends JenkinsPluginTaskSpec {
    @Override
    AbstractJenkinsTask createTaskUnderTest() {
        return project.tasks.updateJenkinsItems
    }

    def "execute updates existing items" () {
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
            1 * getConfiguration(view,_) >> { "<hudson.model.ListView />" }
            1 * updateConfiguration(job1,_,_)
            1 * updateConfiguration(job2,_,_)
            1 * updateConfiguration(view,_,_)
            0 * getConfiguration(* _)
            0 * updateConfiguration(* _)
        }
    }

    def "execute creates new items" () {
        setup:
        def job1 = project.jenkins.jobs.compile_master.definition.name
        def job2 = project.jenkins.jobs.compile_develop.definition.name
        def view = project.jenkins.views."test view".name

        when:
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(job1,_)
            1 * getConfiguration(job2,_)
            1 * getConfiguration(view,_)
            1 * createConfiguration(job1,_,_)
            1 * createConfiguration(job2,_,_)
            1 * createConfiguration(view,_,_)
            0 * getConfiguration(* _)
            0 * createConfiguration(* _)
        }
    }

    def "execute creates new items on all servers" () {
        setup:
        def job1 = project.jenkins.jobs.compile_master.definition.name
        def job2 = project.jenkins.jobs.compile_develop.definition.name
        def view = project.jenkins.views."test view".name
        project.jenkins.jobs.each { job ->
            job.server project.jenkins.servers.test2
        }
        project.jenkins.views.each { _view ->
            _view.server project.jenkins.servers.test2
        }

        when:
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            2 * getConfiguration(job1,_)
            2 * getConfiguration(job2,_)
            2 * getConfiguration(view,_)
            2 * createConfiguration(job1,_,_)
            2 * createConfiguration(job2,_,_)
            2 * createConfiguration(view,_,_)
            0 * getConfiguration(* _)
            0 * createConfiguration(* _)
        }
    }
}
