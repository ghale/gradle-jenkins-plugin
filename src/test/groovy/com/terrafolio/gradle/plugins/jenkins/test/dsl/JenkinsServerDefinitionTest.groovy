package com.terrafolio.gradle.plugins.jenkins.test.dsl

import com.terrafolio.gradle.plugins.jenkins.ConsoleFactory
import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurationException
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class JenkinsServerDefinitionTest {
    def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
    def private final JenkinsPlugin plugin = new JenkinsPlugin()
    def MockFor mockJenkinsRESTService

    @Before
    def void setupProject() {
        plugin.apply(project)

        project.ext.branches = [
                master : [ parents: [ ] ],
                develop: [ parents: [ 'master' ] ]
        ]

        project.jenkins {
            servers {
                test1 {
                    url 'test1'
                    username 'test1'
                    password 'test1'
                }
                test2 {
                    url 'test2'
                    username 'test2'
                    password 'test2'
                }
            }
            templates {
                compile {
                    xml "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
                }
            }
            jobs {
                project.branches.eachWithIndex { branchName, map, index ->
                    "compile_${branchName}" {
                        server servers.test1
                        definition {
                            name "${project.name} compile (${branchName})"
                            xml templates.compile.xml
                        }
                    }
                }
            }
        }

        mockJenkinsRESTService = new MockFor(JenkinsRESTServiceImpl.class)
    }

    @Test
    def void execute_usesDefaultServer() {
        mockJenkinsRESTService.demand.with {
            updateConfiguration(0) { String jobName, String configXML -> }

            2.times {
                getConfiguration() { String jobName, Map overrides ->
                    null
                }

                createConfiguration() { String jobName, String configXML, Map overrides ->
                    if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
                        throw new Exception('createConfiguration called with: ' + jobName + ' but no job definition exists with that name!')
                    }
                }

            }
        }

        project.jenkins {
            defaultServer project.jenkins.servers.test1
        }

        project.jenkins.jobs.each { job ->
            job.serverDefinitions = []
        }

        mockJenkinsRESTService.use {
            project.tasks.updateJenkinsJobs.execute()
        }
    }

    @Test(expected = JenkinsConfigurationException)
    def void execute_throwsExceptionOnMissingServer() {
        project.jenkins.jobs.each { job ->
            job.serverDefinitions = []
        }

        mockJenkinsRESTService.use {
            try {
                project.tasks.updateJenkinsJobs.execute()
            } catch (TaskExecutionException e) {
                throw e.cause
            }
        }
    }

    @Test
    def void execute_allowsMissingUsernameForInsecureServer() {
        mockJenkinsRESTService.demand.with {
            updateConfiguration(0) { String jobName, String configXML, Map overrides -> }

            2.times {
                getConfiguration() { String jobName, Map overrides ->
                    null
                }

                createConfiguration() { String jobName, String configXML, Map overrides ->
                    if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
                        throw new Exception('createConfiguration called with: ' + jobName + ' but no job definition exists with that name!')
                    }
                }

            }
        }

        project.jenkins.servers.each { server ->
            server.secure = false
            server.username = null
        }

        mockJenkinsRESTService.use {
            try {
                project.tasks.updateJenkinsJobs.execute()
            } catch (TaskExecutionException e) {
                throw e.cause
            }
        }
    }

    @Test
    def void execute_allowsMissingPasswordForInsecureServer() {
        mockJenkinsRESTService.demand.with {
            updateConfiguration(0) { String jobName, String configXML, Map overrides -> }

            2.times {
                getConfiguration() { String jobName, Map overrides ->
                    null
                }

                createConfiguration() { String jobName, String configXML, Map overrides ->
                    if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
                        throw new Exception('createConfiguration called with: ' + jobName + ' but no job definition exists with that name!')
                    }
                }

            }
        }

        project.jenkins.servers.each { server ->
            server.secure = false
            server.password = null
        }

        mockJenkinsRESTService.use {
            try {
                project.tasks.updateJenkinsJobs.execute()
            } catch (TaskExecutionException e) {
                throw e.cause
            }
        }
    }

    @Test
    void execute_configuresServerSpecificConfiguration() {
        mockJenkinsRESTService.demand.with {
            updateConfiguration(0) { String jobName, String configXML -> }

            4.times {
                getConfiguration() { String jobName, Map overrides ->
                    null
                }

                createConfiguration() { String jobName, String configXML, Map overrides ->
                    if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
                        throw new Exception('createConfiguration called with: ' + jobName + ' but no job definition exists with that name!')
                    }
                }

            }
        }

        project.jenkins.jobs.each { job ->
            job.server project.jenkins.servers.test2, {
                xml override { projectXml ->
                    projectXml.description = 'This is for test2'
                }
            }
        }

        mockJenkinsRESTService.use {
            project.jenkins.jobs.each { job ->
                job.serverDefinitions.each { server ->
                    def xml = job.getServerSpecificXml(server)
                    if (server.name == 'test2') {
                        assert xml == "<project><actions></actions><description>This is for test2</description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
                    } else {
                        assert xml == "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
                    }
                }
            }
            project.tasks.updateJenkinsJobs.execute()
        }
    }
}
