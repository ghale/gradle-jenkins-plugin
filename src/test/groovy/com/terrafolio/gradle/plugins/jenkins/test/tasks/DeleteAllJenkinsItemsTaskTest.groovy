package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class DeleteAllJenkinsItemsTaskTest {
    def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
    def private final JenkinsPlugin plugin = new JenkinsPlugin()
    def MockFor mockJenkinsRESTService

    @Before
    def void setupProject() {
        plugin.apply(project)

        project.ext.branches = [
                master: [ parents: [ ]] ,
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

        mockJenkinsRESTService = new MockFor(JenkinsRESTServiceImpl.class)
    }

    @Test
    def void execute_deletesJobs() {
        mockJenkinsRESTService.demand.with {
            3.times {
                getConfiguration() { String jobName, Map overrides -> "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>" }
                deleteConfiguration() { String jobName, Map overrides ->
                    if (!(project.jenkins.jobs.collect { it.definition.name } + project.jenkins.views.collect { it.name }).contains(jobName)) {
                        throw new Exception('deleteConfiguration received: ' + jobName + ' but there\'s no item definition with that name!')
                    }
                }
            }
        }

        mockJenkinsRESTService.use {
            project.tasks.deleteJenkinsJobs.execute()
        }
    }

    @Test
    def void execute_deletesJobsOnAllServers() {
        mockJenkinsRESTService.demand.with {
            5.times {
                getConfiguration() { String jobName, Map overrides -> "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>" }
                deleteConfiguration() { String jobName, Map overrides ->
                    if (!(project.jenkins.jobs.collect { it.definition.name } + project.jenkins.views.collect { it.name }).contains(jobName)) {
                        throw new Exception('deleteConfiguration received: ' + jobName + ' but there\'s no item definition with that name!')
                    }
                }
            }
        }

        project.jenkins.jobs.each { job ->
            job.server project.jenkins.servers.test2
        }

        mockJenkinsRESTService.use {
            project.tasks.deleteJenkinsJobs.execute()
        }
    }
}
