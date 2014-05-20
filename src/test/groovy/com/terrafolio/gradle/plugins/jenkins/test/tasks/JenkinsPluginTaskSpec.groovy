package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsService
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsServiceFactory
import com.terrafolio.gradle.plugins.jenkins.tasks.AbstractJenkinsTask
import nebula.test.ProjectSpec
import org.junit.Ignore

/**
 * Created by ghale on 5/19/14.
 */
abstract class JenkinsPluginTaskSpec extends ProjectSpec {
    public JenkinsService mockJenkinsRESTService
    public AbstractJenkinsTask taskUnderTest

    public static final String BASE_JOB_XML = """
        <project>
            <actions></actions>
            <description></description>
            <keepDependencies>false</keepDependencies>
            <properties></properties>
            <scm class='hudson.scm.NullSCM'></scm>
            <canRoam>true</canRoam>
            <disabled>false</disabled>
            <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
            <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
            <triggers class='vector'></triggers>
            <concurrentBuild>false</concurrentBuild>
            <builders></builders>
            <publishers></publishers>
            <buildWrappers></buildWrappers>
        </project>
    """

    def setup() {
        project.apply plugin: 'jenkins'

        project.ext.branches = [
                master: [parents: []],
                develop: [parents: ['master']]
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
                    xml BASE_JOB_XML
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

        mockJenkinsRESTService = Mock(JenkinsRESTServiceImpl)
        taskUnderTest = createTaskUnderTest()
        injectFactory(taskUnderTest)
    }

    abstract AbstractJenkinsTask createTaskUnderTest()

    @Ignore
    def injectFactory(AbstractJenkinsTask task) {
        task.serviceFactory = new JenkinsServiceFactory() {
            @Override
            JenkinsService getService(String url) {
                return mockJenkinsRESTService
            }

            @Override
            JenkinsService getService(String url, String username, String password) {
                return mockJenkinsRESTService
            }
        }
    }
}
