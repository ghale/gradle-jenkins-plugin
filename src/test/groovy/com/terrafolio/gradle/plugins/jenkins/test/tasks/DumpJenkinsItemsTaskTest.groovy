package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.tasks.AbstractJenkinsTask
import com.terrafolio.gradle.plugins.jenkins.tasks.DumpJenkinsItemsTask
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

/**
 * Created by ghale on 5/21/14.
 */
class DumpJenkinsItemsTaskTest extends JenkinsPluginTaskSpec {
    @Override
    AbstractJenkinsTask createTaskUnderTest() {
        return project.task('taskUnderTest', type: DumpJenkinsItemsTask)
    }

    def "execute dumps one job" () {
        setup:
        def jobDir = new File(project.buildDir, "test1/jobs")
        def job1 = new File(jobDir, "compile_master.xml")
        def viewDir = new File(project.buildDir, "test1/views")
        XMLUnit.setIgnoreWhitespace(true)

        when:
        taskUnderTest.dump(project.jenkins.jobs.compile_master)
        taskUnderTest.execute()

        then:
        job1.exists() && new Diff(BASE_JOB_XML, job1.getText()).similar()
        jobDir.listFiles().length == 1
        ! viewDir.exists()
    }

    def "execute dumps one job with lazy closure" () {
        setup:
        def jobDir = new File(project.buildDir, "test1/jobs")
        def job1 = new File(jobDir, "compile_master.xml")
        def viewDir = new File(project.buildDir, "test1/views")
        XMLUnit.setIgnoreWhitespace(true)

        when:
        taskUnderTest.dump { project.jenkins.jobs.compile_master }
        taskUnderTest.execute()

        then:
        job1.exists() && new Diff(BASE_JOB_XML, job1.getText()).similar()
        jobDir.listFiles().length == 1
        ! viewDir.exists()
    }

    def "execute dumps one view" () {
        setup:
        def jobDir = new File(project.buildDir, "test1/jobs")
        def viewDir = new File(project.buildDir, "test1/views")
        def view1 = new File(viewDir, "test view.xml")
        def viewXml = project.jenkins.views."test view".xml
        XMLUnit.setIgnoreWhitespace(true)

        when:
        taskUnderTest.dump(project.jenkins.views."test view")
        taskUnderTest.execute()

        then:
        view1.exists() && new Diff(viewXml, view1.getText()).similar()
        viewDir.listFiles().length == 1
        ! jobDir.exists()
    }

    def "execute dumps multiple items" () {
        setup:
        def jobDir = new File(project.buildDir, "test1/jobs")
        def job1 = new File(jobDir, "compile_master.xml")
        def viewDir = new File(project.buildDir, "test1/views")
        def view1 = new File(viewDir, "test view.xml")
        def viewXml = project.jenkins.views."test view".xml
        XMLUnit.setIgnoreWhitespace(true)

        when:
        taskUnderTest.dump(project.jenkins.jobs.compile_master)
        taskUnderTest.dump(project.jenkins.views."test view")
        taskUnderTest.execute()

        then:
        job1.exists() && new Diff(BASE_JOB_XML, job1.getText()).similar()
        jobDir.listFiles().length == 1
        view1.exists() && new Diff(viewXml, view1.getText()).similar()
        viewDir.listFiles().length == 1
    }

    def "execute dumps multiple items and lazy closure" () {
        setup:
        def jobDir = new File(project.buildDir, "test1/jobs")
        def job1 = new File(jobDir, "compile_master.xml")
        def viewDir = new File(project.buildDir, "test1/views")
        def view1 = new File(viewDir, "test view.xml")
        def viewXml = project.jenkins.views."test view".xml
        XMLUnit.setIgnoreWhitespace(true)

        when:
        taskUnderTest.dump { [ project.jenkins.jobs.compile_master, project.jenkins.views."test view" ] }
        taskUnderTest.execute()

        then:
        job1.exists() && new Diff(BASE_JOB_XML, job1.getText()).similar()
        jobDir.listFiles().length == 1
        view1.exists() && new Diff(viewXml, view1.getText()).similar()
        viewDir.listFiles().length == 1
    }
}
