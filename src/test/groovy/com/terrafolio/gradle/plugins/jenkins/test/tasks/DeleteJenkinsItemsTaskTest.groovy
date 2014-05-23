package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.tasks.AbstractJenkinsTask
import com.terrafolio.gradle.plugins.jenkins.tasks.DeleteJenkinsItemsTask

class DeleteJenkinsItemsTaskTest extends JenkinsPluginTaskSpec {
    @Override
    AbstractJenkinsTask createTaskUnderTest() {
        return project.task('taskUnderTest', type: DeleteJenkinsItemsTask)
    }

    def "execute deletes one job" () {
        setup:
        def jobXml = BASE_JOB_XML
        def jobToDelete = project.jenkins.jobs.compile_master.definition.name

        when:
        taskUnderTest.delete project.jenkins.jobs.compile_master
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(jobToDelete, _) >> { jobXml }
            1 * deleteConfiguration(jobToDelete, _)
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes one job with lazy closure" () {
        setup:
        def jobXml = BASE_JOB_XML
        def jobToDelete = project.jenkins.jobs.compile_master.definition.name

        when:
        taskUnderTest.delete { project.jenkins.jobs.compile_master }
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(jobToDelete, _) >> { jobXml }
            1 * deleteConfiguration(jobToDelete, _)
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes one view" () {
        setup:
        def viewXml = project.jenkins.views."test view".xml
        def viewToDelete = project.jenkins.views."test view".name

        when:
        taskUnderTest.delete project.jenkins.views."test view"
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(viewToDelete, _) >> { viewXml }
            1 * deleteConfiguration(viewToDelete, _)
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes one view with lazy closure" () {
        setup:
        def viewXml = project.jenkins.views."test view".xml
        def viewToDelete = project.jenkins.views."test view".name

        when:
        taskUnderTest.delete { project.jenkins.views."test view" }
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(viewToDelete, _) >> { viewXml }
            1 * deleteConfiguration(viewToDelete, _)
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes one job tuple" () {
        setup:
        def jobXml = BASE_JOB_XML
        def jobToDelete = project.jenkins.jobs.compile_master.definition.name

        when:
        taskUnderTest.delete project.jenkins.servers.test1, jobToDelete
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(jobToDelete, _) >> { jobXml }
            1 * deleteConfiguration(jobToDelete, _)
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes one view tuple" () {
        setup:
        def viewToDelete = project.jenkins.views."test view".name
        def viewXml = project.jenkins.views."test view".xml

        when:
        taskUnderTest.deleteView project.jenkins.servers.test1, viewToDelete
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(viewToDelete, _) >> { viewXml }
            1 * deleteConfiguration(viewToDelete, _)
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes multiple job tuples" () {
        setup:
        def jobXml = BASE_JOB_XML
        def jobToDelete = project.jenkins.jobs.compile_master.definition.name

        when:
        taskUnderTest.delete project.jenkins.servers.test1, jobToDelete
        taskUnderTest.delete project.jenkins.servers.test2, jobToDelete
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            2 * getConfiguration(jobToDelete, _) >> { jobXml }
            2 * deleteConfiguration(jobToDelete, _)
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes multiple jobs" () {
        setup:
        def jobXml = BASE_JOB_XML
        def jobToDelete1 = project.jenkins.jobs.compile_master.definition.name
        def jobToDelete2 = project.jenkins.jobs.compile_develop.definition.name

        when:
        taskUnderTest.delete project.jenkins.jobs.compile_master
        taskUnderTest.delete project.jenkins.jobs.compile_develop
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(jobToDelete1, _) >> { jobXml }
            1 * deleteConfiguration(jobToDelete1, _)
            1 * getConfiguration(jobToDelete2, _) >> { jobXml }
            1 * deleteConfiguration(jobToDelete2, _)
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes multiple jobs with lazy closure" () {
        setup:
        def jobXml = BASE_JOB_XML
        def jobToDelete1 = project.jenkins.jobs.compile_master.definition.name
        def jobToDelete2 = project.jenkins.jobs.compile_develop.definition.name

        when:
        taskUnderTest.delete { [ project.jenkins.jobs.compile_master, project.jenkins.jobs.compile_develop ] }
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(jobToDelete1, _) >> { jobXml }
            1 * deleteConfiguration(jobToDelete1, _)
            1 * getConfiguration(jobToDelete2, _) >> { jobXml }
            1 * deleteConfiguration(jobToDelete2, _)
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes multiple view tuples" () {
        setup:
        def viewXml = project.jenkins.views."test view".xml
        def viewToDelete1 = "test view 1"
        def viewToDelete2 = "test view 2"

        when:
        taskUnderTest.deleteView project.jenkins.servers.test1, viewToDelete1
        taskUnderTest.deleteView project.jenkins.servers.test1, viewToDelete2
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(viewToDelete1, _) >> { viewXml }
            1 * deleteConfiguration(viewToDelete1, _)
            1 * getConfiguration(viewToDelete2, _) >> { viewXml }
            1 * deleteConfiguration(viewToDelete2, _)
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes multiple views" () {
        setup:
        def viewXml = project.jenkins.views."test view".xml
        project.jenkins.views {
            "test view 2" {
                server project.jenkins.servers.test1
                xml viewXml
            }
        }
        def viewToDelete1 = project.jenkins.views."test view".name
        def viewToDelete2 = project.jenkins.views."test view 2".name

        when:
        taskUnderTest.delete project.jenkins.views."test view"
        taskUnderTest.delete project.jenkins.views."test view 2"
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(viewToDelete1, _) >> { viewXml }
            1 * deleteConfiguration(viewToDelete1, _)
            1 * getConfiguration(viewToDelete2, _) >> { viewXml }
            1 * deleteConfiguration(viewToDelete2, _)
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes job with overrides" () {
        setup:
        def jobXml = BASE_JOB_XML
        def jobToDelete = project.jenkins.jobs.compile_master.definition.name
        def testUri = "testuri"
        project.jenkins.jobs.compile_master {
            serviceOverrides { delete = [ uri: testUri ] }
        }

        when:
        taskUnderTest.delete project.jenkins.jobs.compile_master
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(jobToDelete, _) >> { jobXml }
            1 * deleteConfiguration(jobToDelete, { it.uri == testUri })
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes job with default overrides" () {
        setup:
        def jobXml = BASE_JOB_XML
        def jobToDelete = project.jenkins.jobs.compile_master.definition.name

        when:
        taskUnderTest.delete project.jenkins.jobs.compile_master
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(jobToDelete, _) >> { jobXml }
            1 * deleteConfiguration(jobToDelete, { it.uri == "job/${jobToDelete}/doDelete" })
            0 * deleteConfiguration(* _)
        }
    }

    def "execute deletes view with default overrides" () {
        setup:
        def viewXml = project.jenkins.views."test view".xml
        def viewToDelete = project.jenkins.views."test view".name

        when:
        taskUnderTest.delete project.jenkins.views."test view"
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(viewToDelete, _) >> { viewXml }
            1 * deleteConfiguration(viewToDelete, { it.uri == "view/${viewToDelete}/doDelete" })
            0 * deleteConfiguration(* _)
        }
    }
}
