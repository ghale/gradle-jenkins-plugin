package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.ConsoleFactory
import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import groovy.mock.interceptor.MockFor
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

class DumpJenkinsJobsTaskTest {
    def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
    def private final JenkinsPlugin plugin = new JenkinsPlugin()

    @Before
    def void setupProject() {
        plugin.apply(project)

        project.jenkins {
            servers {
                test1 {
                    url 'test1'
                    username 'test1'
                    password 'test1'
                }
            }

            templates {
                compile {
                    xml "<?xml version='1.0' encoding='UTF-8'?><project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
                }
                compile2 {
                    xml "<?xml version='1.0' encoding='UTF-8'?><project><actions></actions><description></description><keepDependencies>true</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
                }
            }

            jobs {
                job1 {
                    server servers.test1
                    definition {
                        name "job1"
                        xml templates.compile.xml
                    }
                }

                job2 {
                    server servers.test1
                    definition {
                        name "job2"
                        xml templates.compile.xml
                    }
                }
            }
            views {
                "test view" {
                    server servers.test1
                    dsl {
                        jobs {
                            project.jenkins.jobs.each { job ->
                                name job.definition.name
                            }
                        }
                    }
                }
            }
        }
    }

    @After
    def void cleanUp() {
        def File dumpDir = new File('build/tmp/test/build/test1')
        dumpDir.deleteDir()
    }

    @Test
    def void execute_dumpsAllItemsToFiles() {
        project.tasks.dumpJenkinsJobs.execute()

        def jobDir = new File('build/tmp/test/build/test1/jobs')
        project.jenkins.jobs.each { job ->
            def jobFile = new File(jobDir, "${job.name}.xml")
            assert jobFile.exists()

            XMLUnit.setIgnoreWhitespace(true)
            def xmlDiff = new Diff(job.definition.xml, jobFile.getText())
            assert xmlDiff.similar()
        }

        def viewDir = new File('build/tmp/test/build/test1/views')
        project.jenkins.views.each { view ->
            def viewFile = new File(viewDir, "${view.name}.xml")
            assert viewFile.exists()

            XMLUnit.setIgnoreWhitespace(true)
            def xmlDiff = new Diff(view.xml, viewFile.getText())
            assert xmlDiff.similar()
        }
    }

    @Test
    def void execute_observesJobFilter() {
        project.ext.jenkinsJobFilter = 'job1'
        project.tasks.dumpJenkinsJobs.execute()

        def dumpDir = new File('build/tmp/test/build/test1/jobs')

        assert new File(dumpDir, "job1.xml").exists()
        assert !(new File(dumpDir, "job2.xml").exists())
    }

    @Test
    def void execute_doesNotPromptForCredentials() {
        def mockConsoleFactory = new MockFor(ConsoleFactory.class)
        mockConsoleFactory.demand.with {
            getConsole(0)
        }

        project.jenkins.servers.each { server ->
            server.username = null
            server.password = null
        }

        mockConsoleFactory.use {
            project.tasks.dumpJenkinsJobs.execute()
        }
    }

    @Test
    def void execute_dumpsRawJobsToFile() {
        project.tasks.dumpJenkinsJobs.prettyPrint = false
        project.tasks.dumpJenkinsJobs.execute()

        def jobDir = new File('build/tmp/test/build/test1/jobs')
        project.jenkins.jobs.each { job ->
            def jobFile = new File(jobDir, "${job.name}.xml")
            assert jobFile.exists()
            assert jobFile.getText() == job.definition.xml
        }

        def viewDir = new File('build/tmp/test/build/test1/views')
        project.jenkins.views.each { view ->
            def viewFile = new File(viewDir, "${view.name}.xml")
            assert viewFile.exists()
            assert viewFile.getText() == view.xml
        }
    }

    @Test
    def void execute_dumpsMultipleServerConfigs() {
        project.tasks.dumpJenkinsJobs.prettyPrint = false
        project.jenkins.servers {
            test3 {
                url 'http://test3'
            }
        }
        project.jenkins.jobs.job1.server(
                project.jenkins.servers.test3,
                {
                    xml project.jenkins.templates.compile2.xml
                }
        )

        project.tasks.dumpJenkinsJobs.execute()

        def jobDir = new File('build/tmp/test/build')

        def jobFile = new File(jobDir, "test1/jobs/job1.xml")
        assert jobFile.exists()
        assert jobFile.getText() == project.jenkins.jobs.job1.definition.xml

        jobFile = new File(jobDir, "test3/jobs/job1.xml")
        assert jobFile.exists()
        assert jobFile.getText() == project.jenkins.templates.compile2.xml
    }
}
