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
						get([ uri: "getTest", params: [ test: "testGetParam"] ])
						create([ uri: "createTest", params: [ test: "testCreateParam"] ])
						update([ uri: "updateTest", params: [ test: "testUpdateParam"] ])
						delete([ uri: "deleteTest", params: [ test: "testDeleteParam"] ])
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

    @Test (expected = JenkinsConfigurationException)
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
	
}
