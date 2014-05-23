package com.terrafolio.gradle.plugins.jenkins.test.dsl

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurationException
import nebula.test.ProjectSpec
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

class JenkinsJobTest extends ProjectSpec {
    static final String FREEFORM_DSL_JOB_XML = """
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

    static final String MAVEN_DSL_JOB_XML = """
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

    static final String MULTIJOB_DSL_JOB_XML = """
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

    static final String BUILDFLOW_DSL_JOB_XML = """
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

    def setup() {
        project.apply plugin: 'jenkins'
    }

    def "configure adds to definition"() {
        setup:
        def testXml = '<test>test</test>'

        when:
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

        then:
        project.jenkins.jobs.test.definition.name == "test name"
        project.jenkins.jobs.test.definition.xml == testXml
    }

    def "configure configures service overrides"() {
        setup:
        def testXml = '<test>test</test>'

        when:
        project.jenkins {
            jobs {
                test {
                    serviceOverrides {
                           get([ uri: "getTest",    params: [ test: "testGetParam" ] ])
                        create([ uri: "createTest", params: [ test: "testCreateParam" ] ])
                        update([ uri: "updateTest", params: [ test: "testUpdateParam" ] ])
                        delete([ uri: "deleteTest", params: [ test: "testDeleteParam" ] ])
                    }
                }
            }
        }

        then:
        project.jenkins.jobs.test.serviceOverrides.get.uri == "getTest"
        project.jenkins.jobs.test.serviceOverrides.get.params.test == "testGetParam"
        project.jenkins.jobs.test.serviceOverrides.create.uri == "createTest"
        project.jenkins.jobs.test.serviceOverrides.create.params.test == "testCreateParam"
        project.jenkins.jobs.test.serviceOverrides.update.uri == "updateTest"
        project.jenkins.jobs.test.serviceOverrides.update.params.test == "testUpdateParam"
        project.jenkins.jobs.test.serviceOverrides.delete.uri == "deleteTest"
        project.jenkins.jobs.test.serviceOverrides.delete.params.test == "testDeleteParam"
    }

    def "configure with dsl file generates correct xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            job {
                name "\${GRADLE_JOB_NAME}"
            }
        """)

        when:
        project.jenkins {
            jobs {
                test {
                    dsl dslFile
                }
            }
        }

        then:
        new Diff(FREEFORM_DSL_JOB_XML, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure with dsl closure generates correct xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)

        when:
        project.jenkins {
            jobs {
                test {
                    dsl {
                        name "Test Job"
                    }
                }
            }
        }

        then:
        new Diff(FREEFORM_DSL_JOB_XML, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure with dsl closure generates correct freeform xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)

        when:
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

        then:
        new Diff(FREEFORM_DSL_JOB_XML, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure with dsl closure generates correct maven xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)

        when:
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

        then:
        new Diff(MAVEN_DSL_JOB_XML, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure with dsl closure generates correct multijob xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)

        when:
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

        then:
        new Diff(MULTIJOB_DSL_JOB_XML, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure with dsl closure generates correct buildflow xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)

        when:
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

        then:
        new Diff(BUILDFLOW_DSL_JOB_XML, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure with dsl closure throws exception on bad type" () {
        when:
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

        then:
        thrown(JenkinsConfigurationException)
    }

    def "configure with dsl closure uses job name when not specified" () {
        when:
        project.jenkins {
            jobs {
                test {
                    dsl {
                        displayName "Test Job"
                    }
                }
            }
        }

        then:
        project.jenkins.jobs.findByName('test').definition.name == "test"
    }

    def "configure with dsl file and definition overrides xml"() {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def newXml = FREEFORM_DSL_JOB_XML.replaceFirst('true', 'false')
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            job {
                name "\${GRADLE_JOB_NAME}"
            }
        """)

        when:
        project.jenkins {
            jobs {
                test {
                    dsl dslFile
                    definition {
                        xml override { projectXml ->
                            projectXml.canRoam = "false"
                        }
                    }
                }
            }
        }

        then:
        new Diff(newXml, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure with dsl closure and definition generates correct xml"() {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def newXml = FREEFORM_DSL_JOB_XML.replaceFirst('false', 'true')

        when:
        project.jenkins {
            jobs {
                test {
                    definition {
                        xml FREEFORM_DSL_JOB_XML
                    }
                    dsl {
                        keepDependencies true
                    }
                }
            }
        }

        then:
        new Diff(newXml, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure with dsl closure using template definition generates correct xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def newXml = FREEFORM_DSL_JOB_XML.replaceFirst('false', 'true')

        when:
        project.jenkins {
            templates {
                testTemplate {
                    xml newXml
                }
            }

            jobs {
                test {
                    dsl {
                        using 'testTemplate'
                    }
                }
            }
        }

        then:
        new Diff(newXml, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure with dsl closure using template dsl generates correct xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def newXml = FREEFORM_DSL_JOB_XML.replaceFirst('false', 'true')

        when:
        project.jenkins {
            templates {
                testTemplate {
                    dsl { keepDependencies true }
                }
            }

            jobs {
                test {
                    dsl {
                        using 'testTemplate'
                    }
                }
            }
        }

        then:
        new Diff(newXml, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure with definition overrides xml from dsl template" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def newXml = FREEFORM_DSL_JOB_XML.replaceFirst('false', 'true')

        when:
        project.jenkins {
            templates {
                testTemplate {
                    dsl { }
                }
            }

            jobs {
                test {
                    definition {
                        xml templates.testTemplate.override { projectXml ->
                            projectXml.keepDependencies = 'true'
                        }
                    }
                }
            }
        }

        then:
        new Diff(newXml, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure allows incremental dsl changes with closure" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def newXml = FREEFORM_DSL_JOB_XML.replaceFirst('false', 'true').replaceFirst('n><', 'n>test<')

        when:
        project.jenkins {
            jobs {
                test {
                    dsl {
                        description "test"
                    }
                }
            }

            jobs {
                test {
                    dsl {
                        keepDependencies true
                    }
                }
            }
        }

        then:
        new Diff(newXml, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure allows incremental dsl changes with file" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def newXml = FREEFORM_DSL_JOB_XML.replaceFirst('false', 'true').replaceFirst('n><', 'n>test<')
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            job {
                name "\${GRADLE_JOB_NAME}"
                using "\${GRADLE_JOB_NAME}"
                keepDependencies true
            }
        """)

        when:
        project.jenkins {
            jobs {
                test {
                    dsl {
                        description "test"
                    }
                }
            }

            jobs {
                test {
                    dsl dslFile
                }
            }
        }

        then:
        new Diff(newXml, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure with dsl file and definition generates correct xml"() {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def newXml = FREEFORM_DSL_JOB_XML.replaceFirst('false', 'true').replaceFirst('n><', 'n>test<')
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            job {
                name "\${GRADLE_JOB_NAME}"
                using "\${GRADLE_JOB_NAME}"
                keepDependencies true
            }
        """)

        when:
        project.jenkins {
            jobs {
                test {
                    definition {
                        xml FREEFORM_DSL_JOB_XML.replaceFirst('n><', 'n>test<')
                    }
                    dsl dslFile
                }
            }
        }

        then:
        new Diff(newXml, project.jenkins.jobs.findByName('test').definition.xml).similar()
    }

    def "configure with dsl file throws exception on multiple jobs" () {
        setup:
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            for (i in 0..1) {
                job {
                    name "Test Job \${i}"
                }
            }
        """)

        when:
        project.jenkins {
            jobs {
                test {
                    dsl dslFile
                }
            }
        }

        then:
        thrown(JenkinsConfigurationException)
    }

    def "configure doesn't call dsl.configure multiple times" () {
        setup:
        def int count = 0

        when:
        project.jenkins {
            jobs {
                test {
                    dsl {
                        name "Test Job"
                        wrappers {
                            configure { root ->
                                count++
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

        then:
        count == 1
    }

    def "getServerSpecificXml uses server-specific configuration" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def newXml = FREEFORM_DSL_JOB_XML.replaceFirst('false', 'true')

        when:
        project.jenkins {
            servers {
                test_server { }
            }
            jobs {
                test {
                    server servers.test_server, {
                        xml override { projectXml ->
                            projectXml.keepDependencies = 'true'
                        }
                    }
                    definition {
                        xml FREEFORM_DSL_JOB_XML
                    }

                }
            }
        }

        then:
        new Diff(newXml, project.jenkins.jobs.findByName('test').getServerSpecificXml(project.jenkins.servers.test_server)).similar()
    }
}
