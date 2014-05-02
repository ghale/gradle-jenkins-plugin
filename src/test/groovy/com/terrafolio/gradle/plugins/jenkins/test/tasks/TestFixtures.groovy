package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class TestFixtures implements TestRule {

    public static final String BASE_JOB_XML = "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"

    final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
    final JenkinsPlugin plugin = new JenkinsPlugin()
    MockFor mockJenkinsRESTService

    @Override
    Statement apply(Statement base, Description description) {

        return [
                evaluate: {
                    setupProject()

                    mockJenkinsRESTService = new MockFor(JenkinsRESTServiceImpl.class)

                    base.evaluate()
                }
        ] as Statement
    }



    private void setupProject() {
        plugin.apply(project)

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
    }
}
