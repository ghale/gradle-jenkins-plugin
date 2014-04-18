package com.terrafolio.gradle.plugins.jenkins.test.dsl

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurationException
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class JenkinsJobTest {
    def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
    def private final JenkinsPlugin plugin = new JenkinsPlugin()

    @Before
    def void setupProject() {
        plugin.apply(project)
    }

    @Test
    def void configure_closureAddsToDefinition() {
        def testXml = '<test>test</test>'
        project.jenkins {
            jobs {
                test {
                    definition {
                        xml testXml
                    }
                }
            }
        }
        project.jenkins.jobs.test.definition {
            name "test name"
        }
        assert project.jenkins.jobs.test.definition.name == "test name"
        assert project.jenkins.jobs.test.definition.xml == testXml
    }

    @Test
    def void configure_configuresServiceOverrides() {
        def testXml = '<test>test</test>'
        project.jenkins {
            jobs {
                test {
                    definition {
                        xml testXml
                    }
                    serviceOverrides {
                           get([ uri: "getTest",    params: [ test: "testGetParam" ] ])
                        create([ uri: "createTest", params: [ test: "testCreateParam" ] ])
                        update([ uri: "updateTest", params: [ test: "testUpdateParam" ] ])
                        delete([ uri: "deleteTest", params: [ test: "testDeleteParam" ] ])
                    }
                }
            }
        }

        assert project.jenkins.jobs.test.serviceOverrides.get.uri == "getTest"
        assert project.jenkins.jobs.test.serviceOverrides.get.params.test == "testGetParam"
        assert project.jenkins.jobs.test.serviceOverrides.create.uri == "createTest"
        assert project.jenkins.jobs.test.serviceOverrides.create.params.test == "testCreateParam"
        assert project.jenkins.jobs.test.serviceOverrides.update.uri == "updateTest"
        assert project.jenkins.jobs.test.serviceOverrides.update.params.test == "testUpdateParam"
        assert project.jenkins.jobs.test.serviceOverrides.delete.uri == "deleteTest"
        assert project.jenkins.jobs.test.serviceOverrides.delete.params.test == "testDeleteParam"
    }

    @Test
    def void configure_dslFileGeneratesXml() {
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            job {
                name "\${GRADLE_JOB_NAME}"
            }
        """)
        project.jenkins {
            jobs {
                test {
                    dsl dslFile
                }
            }
        }

        def expectedXml = """
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

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.jobs.findByName('test').definition.xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_dslClosureGeneratesXml() {
        project.jenkins {
            jobs {
                test {
                    dsl {
                        name "Test Job"
                    }
                }
            }
        }

        def expectedXml = """
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

        XMLUnit.setIgnoreWhitespace(true)
        def job = project.jenkins.jobs.findByName('test')
        assert job.definition.name == "Test Job"
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, job.definition.xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_dslClosureGeneratesFreeformXml() {
        project.jenkins {
            jobs {
                test {
                    type 'Freeform'
                    dsl {
                        name "Test Job"
                    }
                }
            }
        }

        def expectedXml = """
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

        XMLUnit.setIgnoreWhitespace(true)
        def job = project.jenkins.jobs.findByName('test')
        assert job.definition.name == "Test Job"
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, job.definition.xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_dslClosureGeneratesMavenXml() {
        project.jenkins {
            jobs {
                test {
                    type 'Maven'
                    dsl {
                        name "Test Job"
                    }
                }
            }
        }

        def expectedXml = """
            <maven2-moduleset>
              <actions/>
              <description></description>
              <keepDependencies>false</keepDependencies>
              <properties/>
              <scm class="hudson.scm.NullSCM"/>
              <canRoam>true</canRoam>
              <disabled>false</disabled>
              <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
              <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
              <triggers class="vector"/>
              <concurrentBuild>false</concurrentBuild>
              <aggregatorStyleBuild>true</aggregatorStyleBuild>
              <incrementalBuild>false</incrementalBuild>
              <perModuleEmail>false</perModuleEmail>
              <ignoreUpstremChanges>true</ignoreUpstremChanges>
              <archivingDisabled>false</archivingDisabled>
              <resolveDependencies>false</resolveDependencies>
              <processPlugins>false</processPlugins>
              <mavenValidationLevel>-1</mavenValidationLevel>
              <runHeadless>false</runHeadless>
              <publishers/>
              <buildWrappers/>
            </maven2-moduleset>
        """

        XMLUnit.setIgnoreWhitespace(true)
        def job = project.jenkins.jobs.findByName('test')
        assert job.definition.name == "Test Job"
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, job.definition.xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_dslClosureGeneratesMultijobXml() {
        project.jenkins {
            jobs {
                test {
                    type 'Multijob'
                    dsl {
                        name "Test Job"
                    }
                }
            }
        }

        def expectedXml = """
            <com.tikal.jenkins.plugins.multijob.MultiJobProject plugin="jenkins-multijob-plugin@1.8">
              <actions/>
              <description/>
              <keepDependencies>false</keepDependencies>
              <properties/>
              <scm class="hudson.scm.NullSCM"/>
              <canRoam>true</canRoam>
              <disabled>false</disabled>
              <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
              <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
              <triggers class="vector"/>
              <concurrentBuild>false</concurrentBuild>
              <builders/>
              <publishers/>
              <buildWrappers/>
            </com.tikal.jenkins.plugins.multijob.MultiJobProject>
        """

        XMLUnit.setIgnoreWhitespace(true)
        def job = project.jenkins.jobs.findByName('test')
        assert job.definition.name == "Test Job"
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, job.definition.xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_dslClosureGeneratesBuildFlowXml() {
        project.jenkins {
            jobs {
                test {
                    type 'BuildFlow'
                    dsl {
                        name "Test Job"
                    }
                }
            }
        }

        def expectedXml = """
            <com.cloudbees.plugins.flow.BuildFlow>
              <actions/>
              <description></description>
              <keepDependencies>false</keepDependencies>
              <properties/>
              <scm class="hudson.scm.NullSCM"/>
              <canRoam>true</canRoam>
              <disabled>false</disabled>
              <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
              <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
              <triggers class="vector"/>
              <concurrentBuild>false</concurrentBuild>
              <builders/>
              <publishers/>
              <buildWrappers/>
              <icon/>
              <dsl></dsl>
            </com.cloudbees.plugins.flow.BuildFlow>
        """

        XMLUnit.setIgnoreWhitespace(true)
        def job = project.jenkins.jobs.findByName('test')
        assert job.definition.name == "Test Job"
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, job.definition.xml))
        assert xmlDiff.similar()
    }

    @Test(expected = JenkinsConfigurationException)
    def void configure_dslClosureThrowsExceptionOnBadType() {
        project.jenkins {
            jobs {
                test {
                    type 'Junk'
                    dsl {
                        name "Test Job"
                    }
                }
            }
        }
    }

    @Test
    def void configure_dslClosureUsesJobNameWhenNotSpecified() {
        project.jenkins {
            jobs {
                test {
                    dsl {
                        displayName "Test Job"
                    }
                }
            }
        }

        def job = project.jenkins.jobs.findByName('test')
        assert job.definition.name == "test"
    }

    @Test
    def void configure_dslAndDefinitionOverridesGeneratedXml() {
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            job {
                name "\${GRADLE_JOB_NAME}"
            }
        """)
        project.jenkins {
            jobs {
                test {
                    dsl dslFile
                    definition {
                        xml override { projectXml ->
                            projectXml.description = "This is a description"
                        }
                    }
                }
            }
        }

        def expectedXml = """
            <project>
                <actions></actions>
                <description>This is a description</description>
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

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.jobs.findByName('test').definition.xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_dslClosureAndDefinitionUsingBasisXml() {
        project.jenkins {
            jobs {
                test {
                    definition {
                        xml """
                            <project>
                                <actions></actions>
                                <description>This is a description</description>
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
                    }
                    dsl {
                        displayName "Some Display Name"
                    }
                }
            }
        }

        def expectedXml = """
            <project>
                <actions></actions>
                <displayName>Some Display Name</displayName>
                <description>This is a description</description>
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

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.jobs.findByName('test').definition.xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_dslClosureAndDefinitionUsingTemplateXml() {
        project.jenkins {
            templates {
                testTemplate {
                    xml """
                        <project>
                            <actions></actions>
                            <description>This is a description</description>
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
                }
            }

            jobs {
                test {
                    dsl {
                        using 'testTemplate'
                        displayName "Some Display Name"
                    }
                }
            }
        }

        def expectedXml = """
            <project>
                <actions></actions>
                <displayName>Some Display Name</displayName>
                <description>This is a description</description>
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

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.jobs.findByName('test').definition.xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_dslClosureUsingTemplateDsl() {
        project.jenkins {
            templates {
                testTemplate {
                    dsl {
                        description "This is a description"
                    }
                }
            }

            jobs {
                test {
                    dsl {
                        using "testTemplate"
                        displayName "Some Display Name"
                    }
                }
            }
        }

        def expectedXml = """
            <project>
                <actions></actions>
                <displayName>Some Display Name</displayName>
                <description>This is a description</description>
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

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.jobs.findByName('test').definition.xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_overridesXmlFromDslTemplate() {
        project.jenkins {
            templates {
                testTemplate {
                    dsl {
                        description "This is a description"
                    }
                }
            }

            jobs {
                test {
                    definition {
                        xml templates.testTemplate.override { projectXml ->
                            projectXml.appendNode {
                                displayName("Some Display Name")
                            }
                        }
                    }
                }
            }
        }

        def expectedXml = """
            <project>
                <actions></actions>
                <displayName>Some Display Name</displayName>
                <description>This is a description</description>
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

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.jobs.findByName('test').definition.xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_allowsIncrementalDslChangesWithClosure() {
        project.jenkins {
            jobs {
                test {
                    dsl {
                        description "This is a description"
                    }
                }
            }

            jobs {
                test {
                    dsl {
                        displayName "Some Display Name"
                    }
                }
            }
        }

        def expectedXml = """
            <project>
                <actions></actions>
                <displayName>Some Display Name</displayName>
                <description>This is a description</description>
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

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.jobs.findByName('test').definition.xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_allowsIncrementalDslChangesWithFile() {
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            job {
                name "\${GRADLE_JOB_NAME}"
                using "\${GRADLE_JOB_NAME}"
                displayName "Some Display Name"
            }
        """)
        project.jenkins {
            jobs {
                test {
                    dsl {
                        description "This is a description"
                    }
                }
            }

            jobs {
                test {
                    dsl dslFile
                }
            }
        }

        def expectedXml = """
            <project>
                <actions></actions>
                <displayName>Some Display Name</displayName>
                <description>This is a description</description>
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

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.jobs.findByName('test').definition.xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_dslFileAndDefinitionUsingBasisXml() {
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            job {
                name "\${GRADLE_JOB_NAME}"
                using "\${GRADLE_JOB_NAME}"
                displayName "Some Display Name"
            }
        """)
        project.jenkins {
            jobs {
                test {
                    definition {
                        xml """
                            <project>
                                <actions></actions>
                                <description>This is a description</description>
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
                    }
                    dsl dslFile
                }
            }
        }

        def expectedXml = """
            <project>
                <actions></actions>
                <displayName>Some Display Name</displayName>
                <description>This is a description</description>
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

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.jobs.findByName('test').definition.xml))
        assert xmlDiff.similar()
    }

    @Test(expected = JenkinsConfigurationException)
    def void configure_dslThrowsExceptionOnMultipleJobsInDsl() {
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            for (i in 0..1) {
                job {
                    name "Test Job \${i}"
                }
            }
        """)
        project.jenkins {
            jobs {
                test {
                    dsl dslFile
                }
            }
        }
    }

    @Test
    def void configure_doesntCallConfigureMultipleTimes() {
        def int count = 0
        project.jenkins {
            jobs {
                test {
                    dsl {
                        name "Test Job"
                        wrappers {
                            configure { root ->
                                assert count++ == 0
                                (root / "buildWrappers")
                                        .appendNode("newNode")
                                        .appendNode("testNode")
                                        .setValue("test")
                            }
                        }
                    }
                }
            }
        }

        def expectedXml = """
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
                <buildWrappers>
                    <newNode>
                        <testNode>test</testNode>
                    </newNode>
                </buildWrappers>
            </project>
        """

        XMLUnit.setIgnoreWhitespace(true)
        def job = project.jenkins.jobs.findByName('test')
        assert job.definition.name == "Test Job"
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, job.definition.xml))
        assert xmlDiff.similar()
    }

}
