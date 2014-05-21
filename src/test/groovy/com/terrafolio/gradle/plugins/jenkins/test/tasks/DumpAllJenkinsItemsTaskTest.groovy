package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.ConsoleFactory
import com.terrafolio.gradle.plugins.jenkins.tasks.AbstractJenkinsTask
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

class DumpAllJenkinsItemsTaskTest extends JenkinsPluginTaskSpec {
    @Override
    AbstractJenkinsTask createTaskUnderTest() {
        return project.tasks.dumpJenkinsItems
    }

    def "execute dumps all items to files" () {
        setup:
        def jobDir = new File(project.buildDir, "test1/jobs")
        def job1 = new File(jobDir, "compile_master.xml")
        def job2 = new File(jobDir, "compile_develop.xml")
        def viewDir = new File(project.buildDir, "test1/views")
        def view1 = new File(viewDir, "test view.xml")
        def viewXml = project.jenkins.views."test view".xml
        XMLUnit.setIgnoreWhitespace(true)

        when:
        taskUnderTest.execute()

        then:
        job1.exists() && new Diff(BASE_JOB_XML, job1.getText()).similar()
        job2.exists() && new Diff(BASE_JOB_XML, job2.getText()).similar()
        view1.exists() && new Diff(viewXml, view1.getText()).similar()
    }

    def "execute observes job filter" () {
        setup:
        def jobDir = new File(project.buildDir, "test1/jobs")
        def job1 = new File(jobDir, "compile_master.xml")
        def job2 = new File(jobDir, "compile_develop.xml")
        def viewDir = new File(project.buildDir, "test1/views")
        def view1 = new File(viewDir, "test view.xml")
        project.ext.jenkinsJobFilter = 'compile_master'
        XMLUnit.setIgnoreWhitespace(true)

        when:
        taskUnderTest.execute()

        then:
        job1.exists()
        ! job2.exists()
        ! view1.exists()
    }

    def "execute does not prompt for credentials" () {
        setup:
        def ConsoleFactory mockConsoleFactory = Mock(ConsoleFactory)
        project.jenkins.servers.each { server ->
            server.username = null
            server.password = null
            server.consoleFactory = mockConsoleFactory
        }

        when:
        taskUnderTest.execute()

        then:
        0 * mockConsoleFactory.getConsole()
    }

    def "execute dumps raw jobs when prettyPrint equals false" () {
        setup:
        def jobDir = new File(project.buildDir, "test1/jobs")
        def job1 = new File(jobDir, "compile_master.xml")
        def job2 = new File(jobDir, "compile_develop.xml")
        def viewDir = new File(project.buildDir, "test1/views")
        def view1 = new File(viewDir, "test view.xml")
        def viewXml = project.jenkins.views."test view".xml

        when:
        taskUnderTest.prettyPrint = false
        taskUnderTest.execute()

        then:
        job1.exists() && job1.text == BASE_JOB_XML
        job2.exists() && job2.text == BASE_JOB_XML
        view1.exists() && view1.text == viewXml
    }

    def "execute dumps multiple server configs" () {
        setup:
        def job1Dir = new File(project.buildDir, "test1/jobs")
        def job1 = new File(job1Dir, "compile_master.xml")
        def job2 = new File(job1Dir, "compile_develop.xml")
        def job3Dir = new File(project.buildDir, "test3/jobs")
        def job3 = new File(job3Dir, "compile_master.xml")
        def viewDir = new File(project.buildDir, "test1/views")
        def view1 = new File(viewDir, "test view.xml")
        def viewXml = project.jenkins.views."test view".xml
        XMLUnit.setIgnoreWhitespace(true)
        project.jenkins.servers {
            test3 {
                url 'http://test3'
            }
        }
        project.jenkins.jobs.compile_master.server project.jenkins.servers.test3

        when:
        taskUnderTest.execute()

        then:
        job1.exists() && new Diff(BASE_JOB_XML, job1.getText()).similar()
        job2.exists() && new Diff(BASE_JOB_XML, job2.getText()).similar()
        job3.exists() && new Diff(BASE_JOB_XML, job3.getText()).similar()
        view1.exists() && new Diff(viewXml, view1.getText()).similar()
    }
}
