package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.tasks.AbstractJenkinsTask
import com.terrafolio.gradle.plugins.jenkins.tasks.JenkinsValidationException
import com.terrafolio.gradle.plugins.jenkins.tasks.ValidateJenkinsItemsTask
import org.gradle.api.tasks.TaskExecutionException

/**
 * Created by ghale on 5/21/14.
 */
class ValidateJenkinsItemsTaskTest extends JenkinsPluginTaskSpec {
    @Override
    AbstractJenkinsTask createTaskUnderTest() {
        return project.task('taskUnderTest', type: ValidateJenkinsItemsTask)
    }

    def "execute succeeds on no difference" () {
        setup:
        def theproject = project
        def jobxml = BASE_JOB_XML

        when:
        taskUnderTest.validate(project.jenkins.jobs.compile_master)
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(theproject.jenkins.jobs.compile_master.definition.name,_) >> { jobxml }
        }
        noExceptionThrown()
    }

    def "execute succeeds on no difference with lazy closure" () {
        setup:
        def theproject = project
        def jobxml = BASE_JOB_XML

        when:
        taskUnderTest.validate { project.jenkins.jobs.compile_master }
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(theproject.jenkins.jobs.compile_master.definition.name,_) >> { jobxml }
        }
        noExceptionThrown()
    }

    def "execute fails on difference" () {
        setup:
        def theproject = project
        def diffxml = BASE_JOB_XML.replaceFirst('true', 'false')

        when:
        taskUnderTest.validate(project.jenkins.jobs.compile_master)
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(theproject.jenkins.jobs.compile_master.definition.name,_) >> { diffxml }
        }
        def e = thrown(TaskExecutionException)
        e.cause.class == JenkinsValidationException
    }

    def "execute succeeds when failOnDifference is false" () {
        setup:
        def theproject = project
        def jobxml = BASE_JOB_XML
        def diffxml = BASE_JOB_XML.replaceFirst('true', 'false')

        when:
        taskUnderTest.failOnDifference = false
        taskUnderTest.validate(project.jenkins.jobs.compile_master)
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(theproject.jenkins.jobs.compile_master.definition.name,_) >> { diffxml }
        }
        noExceptionThrown()
    }

    def "execute succeeds on no difference with multiple items" () {
        setup:
        def theproject = project
        def jobxml = BASE_JOB_XML

        when:
        taskUnderTest.validate(project.jenkins.jobs.compile_master)
        taskUnderTest.validate(project.jenkins.views."test view")
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(theproject.jenkins.jobs.compile_master.definition.name,_) >> { jobxml }
            1 * getConfiguration(theproject.jenkins.views."test view".name,_) >> { theproject.jenkins.views."test view".xml }
        }
        noExceptionThrown()
    }

    def "execute succeeds on no difference with lazy closure and multiple items" () {
        setup:
        def theproject = project
        def jobxml = BASE_JOB_XML

        when:
        taskUnderTest.validate { [ project.jenkins.jobs.compile_master, project.jenkins.views."test view" ] }
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(theproject.jenkins.jobs.compile_master.definition.name,_) >> { jobxml }
            1 * getConfiguration(theproject.jenkins.views."test view".name,_) >> { theproject.jenkins.views."test view".xml }
        }
        noExceptionThrown()
    }
}
