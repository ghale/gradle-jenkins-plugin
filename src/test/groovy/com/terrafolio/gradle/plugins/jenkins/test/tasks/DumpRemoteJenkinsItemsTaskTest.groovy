package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.tasks.AbstractJenkinsItemsTask
import com.terrafolio.gradle.plugins.jenkins.tasks.DumpRemoteJenkinsItemsTask
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

/**
 * Created by ghale on 5/21/14.
 */
class DumpRemoteJenkinsItemsTaskTest extends JenkinsPluginTaskSpec {
    @Override
    AbstractJenkinsItemsTask createTaskUnderTest() {
        return project.task('taskUnderTest', type: DumpRemoteJenkinsItemsTask)
    }

    def "execute dumps one job" () {
        setup:
        def theproject = project
        def jobDir = new File(project.buildDir, "remotes/test1/jobs")
        def job1 = new File(jobDir, "compile_master.xml")
        def viewDir = new File(project.buildDir, "remotes/test1/views")
        XMLUnit.setIgnoreWhitespace(true)

        when:
        taskUnderTest.dump(project.jenkins.jobs.compile_master)
        taskUnderTest.execute()

        then:
        1 * mockJenkinsRESTService.getConfiguration(theproject.jenkins.jobs.compile_master.definition.name, _) >> { BASE_JOB_XML }
        job1.exists() && new Diff(BASE_JOB_XML, job1.getText()).similar()
        jobDir.listFiles().length == 1
        ! viewDir.exists()
    }

    def "execute dumps one job with lazy closure" () {
        setup:
        def theproject = project
        def jobDir = new File(project.buildDir, "remotes/test1/jobs")
        def job1 = new File(jobDir, "compile_master.xml")
        def viewDir = new File(project.buildDir, "remotes/test1/views")
        XMLUnit.setIgnoreWhitespace(true)

        when:
        taskUnderTest.dump { project.jenkins.jobs.compile_master }
        taskUnderTest.execute()

        then:
        1 * mockJenkinsRESTService.getConfiguration(theproject.jenkins.jobs.compile_master.definition.name, _) >> { BASE_JOB_XML }
        job1.exists() && new Diff(BASE_JOB_XML, job1.getText()).similar()
        jobDir.listFiles().length == 1
        ! viewDir.exists()
    }

    def "execute dumps one view" () {
        setup:
        def theproject = project
        def jobDir = new File(project.buildDir, "remotes/test1/jobs")
        def viewDir = new File(project.buildDir, "remotes/test1/views")
        def view1 = new File(viewDir, "test view.xml")
        def viewXml = project.jenkins.views."test view".xml
        XMLUnit.setIgnoreWhitespace(true)

        when:
        taskUnderTest.dump(project.jenkins.views."test view")
        taskUnderTest.execute()

        then:
        1 * mockJenkinsRESTService.getConfiguration(theproject.jenkins.views."test view".name, _) >> { viewXml }
        view1.exists() && new Diff(viewXml, view1.getText()).similar()
        viewDir.listFiles().length == 1
        ! jobDir.exists()
    }

    def "execute dumps multiple items" () {
        setup:
        def theproject = project
        def jobDir = new File(project.buildDir, "remotes/test1/jobs")
        def job1 = new File(jobDir, "compile_master.xml")
        def jobXml = BASE_JOB_XML
        def viewDir = new File(project.buildDir, "remotes/test1/views")
        def view1 = new File(viewDir, "test view.xml")
        def viewXml = project.jenkins.views."test view".xml
        XMLUnit.setIgnoreWhitespace(true)

        when:
        taskUnderTest.dump(project.jenkins.jobs.compile_master)
        taskUnderTest.dump(project.jenkins.views."test view")
        taskUnderTest.execute()

        then:
        with (mockJenkinsRESTService) {
            1 * getConfiguration(theproject.jenkins.jobs.compile_master.definition.name, _) >> { jobXml }
            1 * getConfiguration(theproject.jenkins.views."test view".name, _) >> { viewXml }
        }
        job1.exists() && new Diff(BASE_JOB_XML, job1.getText()).similar()
        jobDir.listFiles().length == 1
        view1.exists() && new Diff(viewXml, view1.getText()).similar()
        viewDir.listFiles().length == 1
    }

    def "execute dumps multiple items and lazy closure" () {
        setup:
        def theproject = project
        def jobDir = new File(project.buildDir, "remotes/test1/jobs")
        def job1 = new File(jobDir, "compile_master.xml")
        def jobXml = BASE_JOB_XML
        def viewDir = new File(project.buildDir, "remotes/test1/views")
        def view1 = new File(viewDir, "test view.xml")
        def viewXml = project.jenkins.views."test view".xml
        XMLUnit.setIgnoreWhitespace(true)

        when:
        taskUnderTest.dump { [ project.jenkins.jobs.compile_master, project.jenkins.views."test view" ] }
        taskUnderTest.execute()

        then:
        with (mockJenkinsRESTService) {
            1 * getConfiguration(theproject.jenkins.jobs.compile_master.definition.name, _) >> { jobXml }
            1 * getConfiguration(theproject.jenkins.views."test view".name, _) >> { viewXml }
        }
        job1.exists() && new Diff(BASE_JOB_XML, job1.getText()).similar()
        jobDir.listFiles().length == 1
        view1.exists() && new Diff(viewXml, view1.getText()).similar()
        viewDir.listFiles().length == 1
    }
}
