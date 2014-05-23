package com.terrafolio.gradle.plugins.jenkins.test.dsl

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsJob
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsView
import javaposse.jobdsl.dsl.NameNotProvidedException
import javaposse.jobdsl.dsl.ViewType
import nebula.test.ProjectSpec
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

/**
 * Created by ghale on 4/7/14.
 */
class JenkinsConfigurationTest extends ProjectSpec {
    static final String EMPTY_DSL_JOB_XML = """
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

    static final String EMPTY_DSL_VIEW_XML = """
            <hudson.model.ListView>
              <filterExecutors>false</filterExecutors>
              <filterQueue>false</filterQueue>
              <properties class="hudson.model.View\$PropertyList"/>
              <jobNames class="tree-set">
                <comparator class="hudson.util.CaseInsensitiveComparator"/>
              </jobNames>
              <jobFilters/>
              <columns/>
            </hudson.model.ListView>
    """

    def setup() {
        project.apply plugin: 'jenkins'
    }

    def "configure adds jenkins job" () {
        when:
        project.jenkins {
            jobs {
                testJob
            }
        }

        then:
        project.convention.plugins.jenkins.jenkins.jobs.findByName('testJob') instanceof JenkinsJob
    }

    def "configure adds jenkins view" () {
        when:
        project.jenkins {
            views {
                test
            }
        }

        then:
        project.convention.plugins.jenkins.jenkins.views.findByName('test') instanceof JenkinsView
    }

    def "configure configures jobs using dsl file" (jobName, xml) {
        setup:
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            for (i in 0..2) {
                job {
                    name "Test Job \${i}"
                }
            }
        """)
        XMLUnit.setIgnoreWhitespace(true)
        project.jenkins {
            dsl project.files('test.dsl')
        }

        expect:
        project.jenkins.jobs.findByName(jobName) != null
        new Diff(xml, project.jenkins.jobs.findByName(jobName).definition.xml).similar()

        where:
        jobName      | xml
        "Test Job 0" | EMPTY_DSL_JOB_XML
        "Test Job 1" | EMPTY_DSL_JOB_XML
        "Test Job 2" | EMPTY_DSL_JOB_XML
    }

    def "configure configures jobs using multiple dsl files" (jobName, xml) {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def jenkinsDir = project.file('jenkins')
        jenkinsDir.mkdirs()
        def dslFile1 = new File(jenkinsDir, 'test1.dsl')
        dslFile1.write("""
            for (i in 0..2) {
                job {
                    name "Test Job \${i}"
                }
            }
        """)

        def dslFile2 = new File(jenkinsDir, 'test2.dsl')
        dslFile2.write("""
            for (i in 0..2) {
                job {
                    name "Another Job \${i}"
                }
            }
        """)

        project.jenkins {
            dsl project.fileTree("jenkins").include("*.dsl")
        }

        expect:
        project.jenkins.jobs.findByName(jobName) != null
        new Diff(xml, project.jenkins.jobs.findByName(jobName).definition.xml).similar()

        where:
        jobName         | xml
        "Test Job 0"    | EMPTY_DSL_JOB_XML
        "Test Job 1"    | EMPTY_DSL_JOB_XML
        "Test Job 2"    | EMPTY_DSL_JOB_XML
        "Another Job 0" | EMPTY_DSL_JOB_XML
        "Another Job 1" | EMPTY_DSL_JOB_XML
        "Another Job 2" | EMPTY_DSL_JOB_XML
    }

    def "configure configures views using dsl file" (viewName, xml) {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            for (i in 0..2) {
                view(type: ListView) {
                    name("test view \${i}")
                }
            }
        """)
        project.jenkins {
            dsl project.files('test.dsl')
        }

        expect:
        project.jenkins.views.findByName(viewName) != null
        new Diff(xml, project.jenkins.views.findByName(viewName).xml).similar()

        where:
        viewName      | xml
        "test view 0" | EMPTY_DSL_VIEW_XML
        "test view 1" | EMPTY_DSL_VIEW_XML
        "test view 2" | EMPTY_DSL_VIEW_XML
    }

    def "configure configures views using multiple dsl files" (viewName, xml) {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def jenkinsDir = project.file('jenkins')
        jenkinsDir.mkdirs()
        def dslFile1 = new File(jenkinsDir, 'test1.dsl')
        dslFile1.write("""
            for (i in 0..2) {
                view(type: ListView) {
                    name "test view \${i}"
                }
            }
        """)

        def dslFile2 = new File(jenkinsDir, 'test2.dsl')
        dslFile2.write("""
            for (i in 0..2) {
                view(type: ListView) {
                    name "another view \${i}"
                }
            }
        """)

        project.jenkins {
            dsl project.fileTree("jenkins").include("*.dsl")
        }

        expect:
        project.jenkins.views.findByName(viewName) != null
        new Diff(xml, project.jenkins.views.findByName(viewName).xml).similar()

        where:
        viewName         | xml
        "test view 0"    | EMPTY_DSL_VIEW_XML
        "test view 1"    | EMPTY_DSL_VIEW_XML
        "test view 2"    | EMPTY_DSL_VIEW_XML
        "another view 0" | EMPTY_DSL_VIEW_XML
        "another view 1" | EMPTY_DSL_VIEW_XML
        "another view 2" | EMPTY_DSL_VIEW_XML
    }

    def "configure configures jobs using dsl closure"(jobName, xml) {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        project.jenkins {
            dsl {
                for (i in 0..2) {
                    job {
                        name "Test Job ${i}"
                    }
                }
            }
        }

        expect:
        project.jenkins.jobs.findByName(jobName) != null
        new Diff(xml, project.jenkins.jobs.findByName(jobName).definition.xml).similar()

        where:
        jobName      | xml
        "Test Job 0" | EMPTY_DSL_JOB_XML
        "Test Job 1" | EMPTY_DSL_JOB_XML
        "Test Job 2" | EMPTY_DSL_JOB_XML
    }

    def "configure configures views using dsl closure" (viewName, xml) {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        project.jenkins {
            dsl {
                for (i in 0..2) {
                    view(type: ViewType.ListView) {
                        name "test view ${i}"
                    }
                }
            }
        }

        expect:
        project.jenkins.views.findByName(viewName) != null
        new Diff(xml, project.jenkins.views.findByName(viewName).xml).similar()

        where:
        viewName      | xml
        "test view 0" | EMPTY_DSL_VIEW_XML
        "test view 1" | EMPTY_DSL_VIEW_XML
        "test view 2" | EMPTY_DSL_VIEW_XML
    }

    def "configure throws exeception when no job name provided with dsl closure"() {
        when:
        project.jenkins {
            dsl {
                for (i in 0..2) {
                    job {
                        steps {
                            shell("echo test")
                        }
                    }
                }
            }
        }

        then:
        thrown(NameNotProvidedException)
    }

    def "configure throws exeception when no job name provided with dsl file"() {
        setup:
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            for (i in 0..9) {
                job {
                    steps {
                        shell "echo test"
                    }
                }
            }
        """)

        when:
        project.jenkins {
            dsl project.files('test.dsl')
        }

        then:
        thrown(NameNotProvidedException)
    }

    def "configure configures jobs using dsl closure with basis xml"(jobName, xml) {
        setup:
        project.jenkins {
            jobs {
                "Test Job" {
                    definition {
                        delegate.xml EMPTY_DSL_JOB_XML.replaceFirst('true', 'false')
                    }
                }
            }
            dsl {
                for (i in 0..2) {
                    job {
                        using "Test Job"
                        name "Another Job ${i}"
                    }
                }
            }
        }

        expect:
        project.jenkins.jobs.findByName(jobName) != null
        new Diff(xml, project.jenkins.jobs.findByName(jobName).definition.xml).similar()

        where:
        jobName         | xml
        "Another Job 0" | EMPTY_DSL_JOB_XML.replaceFirst('true', 'false')
        "Another Job 1" | EMPTY_DSL_JOB_XML.replaceFirst('true', 'false')
        "Another Job 2" | EMPTY_DSL_JOB_XML.replaceFirst('true', 'false')
    }

    def "configure configures jobs using dsl file with basis xml" (jobName, xml) {
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            for (i in 0..2) {
                job {
                    using "Test Job"
                    name "Another Job \${i}"
                }
            }
        """)

        project.jenkins {
            jobs {
                "Test Job" {
                    definition {
                        delegate.xml EMPTY_DSL_JOB_XML.replaceFirst('true', 'false')
                    }
                }
            }
            dsl project.files('test.dsl')
        }

        expect:
        project.jenkins.jobs.findByName(jobName) != null
        new Diff(xml, project.jenkins.jobs.findByName(jobName).definition.xml).similar()

        where:
        jobName         | xml
        "Another Job 0" | EMPTY_DSL_JOB_XML.replaceFirst('true', 'false')
        "Another Job 1" | EMPTY_DSL_JOB_XML.replaceFirst('true', 'false')
        "Another Job 2" | EMPTY_DSL_JOB_XML.replaceFirst('true', 'false')
    }
}
