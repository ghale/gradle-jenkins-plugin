package com.terrafolio.gradle.plugins.jenkins.test.dsl

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurationException
import nebula.test.ProjectSpec
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

/**
 * Created by ghale on 4/11/14.
 */
class JenkinsViewTest extends ProjectSpec {
    static final String LIST_DSL_VIEW_XML = """
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

    def "configure with dsl closure generates correct xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)

        when:
        project.jenkins.views {
            test {
                dsl {
                    name "test"
                }
            }
        }

        then:
        new Diff(LIST_DSL_VIEW_XML, project.jenkins.views.findByName('test').xml).similar()
    }

    def "configure with dsl file generates correct xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            view(type: ListView) {
                name "test"
            }
        """)

        when:
        project.jenkins.views {
            test {
                dsl dslFile
            }
        }

        then:
        new Diff(LIST_DSL_VIEW_XML, project.jenkins.views.findByName('test').xml).similar()
    }

    def "configure throws exception when multiple views are generated" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def dslFile = project.file('test.dsl')
        dslFile.write("""
                view {
                    name "Test View 1"
                }
                view {
                    name "Test View 2"
                }
        """)

        when:
        project.jenkins.views {
            test {
                dsl dslFile
            }
        }

        then:
        thrown(JenkinsConfigurationException)
    }

    def "configure loads xml from file" () {
        setup:
        def File xmlFile = project.file('test.xml')
        xmlFile.write(LIST_DSL_VIEW_XML)

        when:
        project.jenkins.views {
            test {
                xml(xmlFile)
            }
        }

        then:
        project.jenkins.views.test.xml == LIST_DSL_VIEW_XML
    }

    def "configure loads xml from string" () {
        when:
        project.jenkins.views {
            test {
                xml(LIST_DSL_VIEW_XML)
            }
        }

        then:
        project.jenkins.views.test.xml == LIST_DSL_VIEW_XML
    }

    def "getServerSpecificXml uses server-specific overrides" () {
        setup:
        def newXml = LIST_DSL_VIEW_XML.replaceFirst('false', 'true')
        project.jenkins {
            servers {
                test1 {
                    url "http://localhost:8080"
                    secure false
                }
                test2 {
                    url "http://localhost:8081"
                    secure false
                }
            }
            views {
                test {
                    server(project.jenkins.servers.test1) {
                        xml(newXml)
                    }
                    server project.jenkins.servers.test2
                    xml(LIST_DSL_VIEW_XML)
                }
            }
        }

        expect:
        project.jenkins.views.test.getServerSpecificXml(project.jenkins.servers.test1) == newXml
        project.jenkins.views.test.getServerSpecificXml(project.jenkins.servers.test2) == LIST_DSL_VIEW_XML
    }

    def "configure with xml override generates correct xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def newXml = LIST_DSL_VIEW_XML.replaceFirst('false', 'true')
        File xmlFile = project.file('test.xml')
        xmlFile.write(LIST_DSL_VIEW_XML)

        when:
        project.jenkins.views {
            test {
                xml(xmlFile)
                xml override { viewXml ->
                    viewXml.filterExecutors = 'true'
                }
            }
        }

        then:
        new Diff(newXml, project.jenkins.views.findByName('test').xml).similar()
    }

    def "configure with xml override from dsl generates correct xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def newXml = LIST_DSL_VIEW_XML.replaceFirst('false', 'true')

        when:
        project.jenkins.views {
            test {
                dsl {
                    name "test"
                }
                xml override { viewXml ->
                    viewXml.filterExecutors = 'true'
                }
            }
        }

        then:
        new Diff(newXml, project.jenkins.views.findByName('test').xml).similar()
    }

    def "configure with xml override from dsl file generates correct xml" () {
        setup:
        XMLUnit.setIgnoreWhitespace(true)
        def newXml = LIST_DSL_VIEW_XML.replaceFirst('false', 'true')
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            view(type: ListView) {
                name "test"
            }
        """)

        when:
        project.jenkins.views {
            test {
                dsl dslFile
                xml override { viewXml ->
                    viewXml.filterExecutors = 'true'
                }
            }
        }

        then:
        new Diff(newXml, project.jenkins.views.findByName('test').xml).similar()
    }
}
