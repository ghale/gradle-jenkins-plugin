package com.terrafolio.gradle.plugins.jenkins.test.dsl

import nebula.test.ProjectSpec
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

/**
 * Created on 6/30/14.
 */
class JenkinsJobTemplateTest extends ProjectSpec {

    def setup() {
        project.apply plugin: 'com.terrafolio.jenkins'
    }

    def "xml from file creates correct xml" () {
        setup:
        def xmlFile = project.file('test.xml')
        xmlFile.write(JobFixtures.FREEFORM_DSL_JOB_XML)

        when:
        project.jenkins {
            templates {
                test {
                    xml xmlFile
                }
            }
        }

        then:
        project.jenkins.templates.findByName('test').xml == JobFixtures.FREEFORM_DSL_JOB_XML
    }

    def "xml from string creates correct xml" () {
        when:
        project.jenkins {
            templates {
                test {
                    xml JobFixtures.FREEFORM_DSL_JOB_XML
                }
            }
        }

        then:
        project.jenkins.templates.findByName('test').xml == JobFixtures.FREEFORM_DSL_JOB_XML
    }

    def "xml from closure creates correct xml" () {
        when:
        project.jenkins {
            templates {
                test {
                    xml {
                        test1() {
                            test2('value')
                        }
                    }
                }
            }
        }

        then:
        project.jenkins.templates.findByName('test').xml == '<test1><test2>value</test2></test1>'
    }

    def "override correctly overrides xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)

        when:
        project.jenkins {
            templates {
                test {
                    xml JobFixtures.FREEFORM_DSL_JOB_XML
                }
                test2 {
                    xml test.override {
                        it.keepDependencies = 'true'
                    }
                }
            }
        }

        then:
        new Diff(JobFixtures.FREEFORM_DSL_JOB_XML.replaceFirst('false', 'true'), project.jenkins.templates.findByName('test2').xml).similar()
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
            templates {
                test {
                    dsl dslFile
                }
            }
        }

        then:
        new Diff(JobFixtures.FREEFORM_DSL_JOB_XML, project.jenkins.templates.findByName('test').xml).similar()
    }

    def "configure with dsl closure generates correct xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)

        when:
        project.jenkins {
            templates {
                test {
                    dsl {
                        name "Test Job"
                    }
                }
            }
        }

        then:
        new Diff(JobFixtures.FREEFORM_DSL_JOB_XML, project.jenkins.templates.findByName('test').xml).similar()
    }
}
