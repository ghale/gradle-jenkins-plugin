package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import com.terrafolio.gradle.plugins.jenkins.tasks.AbstractJenkinsTask
import com.terrafolio.gradle.plugins.jenkins.tasks.JenkinsValidationException
import com.terrafolio.gradle.plugins.jenkins.tasks.ValidateJenkinsItemsTask
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class ValidateAllJenkinsItemsTaskTest extends JenkinsPluginTaskSpec {
    @Override
    AbstractJenkinsTask createTaskUnderTest() {
        return project.tasks.validateJenkinsItems
    }

    def "execute succeeds on no difference" () {
        setup:
        def theproject = project
        def jobxml = BASE_JOB_XML

        when:
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(theproject.jenkins.jobs.compile_master.definition.name,_) >> { jobxml }
            1 * getConfiguration(theproject.jenkins.jobs.compile_develop.definition.name,_) >> { jobxml }
            1 * getConfiguration(theproject.jenkins.views."test view".name,_) >> { theproject.jenkins.views."test view".xml }
        }
        noExceptionThrown()
    }

    def "execute fails on difference" () {
        setup:
        def theproject = project
        def jobxml = BASE_JOB_XML
        def diffxml = BASE_JOB_XML.replaceFirst('true', 'false')

        when:
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(theproject.jenkins.jobs.compile_master.definition.name,_) >> { jobxml }
            1 * getConfiguration(theproject.jenkins.jobs.compile_develop.definition.name,_) >> { diffxml }
            1 * getConfiguration(theproject.jenkins.views."test view".name,_) >> { theproject.jenkins.views."test view".xml }
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
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            1 * getConfiguration(theproject.jenkins.jobs.compile_master.definition.name,_) >> { jobxml }
            1 * getConfiguration(theproject.jenkins.jobs.compile_develop.definition.name,_) >> { diffxml }
            1 * getConfiguration(theproject.jenkins.views."test view".name,_) >> { theproject.jenkins.views."test view".xml }
        }
        noExceptionThrown()
    }
}
