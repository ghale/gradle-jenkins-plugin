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

/**
 * Created by ghale on 4/11/14.
 */
class JenkinsViewTest {
    def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
    def private final JenkinsPlugin plugin = new JenkinsPlugin()

    @Before
    def void setupProject() {
        plugin.apply(project)
    }

    @Test
    def void configure_generatesViewFromDslClosure() {
        project.jenkins.views {
            test {
                dsl {
                    name "test"
                }
            }
        }

        String expectedXml = """
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

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.views.findByName('test').xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_generatesViewFromDslFile() {
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            view(type: ListView) {
                name "test"
            }
        """)
        project.jenkins.views {
            test {
                dsl dslFile
            }
        }

        String expectedXml = """
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

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.views.findByName('test').xml))
        assert xmlDiff.similar()
    }

    @Test (expected = JenkinsConfigurationException)
    def void configure_dslThrowsExceptionOnMultipleViewsInDsl() {
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            for (i in 0..1) {
                view {
                    name "Test Job \${i}"
                }
            }
        """)

        project.jenkins.views {
            test {
                dsl dslFile
            }
        }
    }

    @Test
    def void configure_loadsXmlFromFile() {
        def String expectedXml = """
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
        File xmlFile = project.file('test.xml')
        xmlFile.write(expectedXml)

        project.jenkins.views {
            test {
                xml(xmlFile)
            }
        }

        assert project.jenkins.views.test.xml == expectedXml
    }

    @Test
    def void configure_loadsXmlFromString() {
        def String expectedXml = """
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

        project.jenkins.views {
            test {
                xml(expectedXml)
            }
        }

        assert project.jenkins.views.test.xml == expectedXml
    }

    @Test
    def void getServerSpecificXml_createsCorrectXml() {
        def String expectedXml = """
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

        def String ssxml = """
            <hudson.model.ListView>
              <filterExecutors>true</filterExecutors>
              <filterQueue>false</filterQueue>
              <properties class="hudson.model.View\$PropertyList"/>
              <jobNames class="tree-set">
                <comparator class="hudson.util.CaseInsensitiveComparator"/>
              </jobNames>
              <jobFilters/>
              <columns/>
            </hudson.model.ListView>
        """

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
                        xml(ssxml)
                    }
                    xml(expectedXml)
                }
            }
        }

        assert project.jenkins.views.test.getServerSpecificXml(project.jenkins.servers.test1) == ssxml
    }

    @Test
    def void configure_overridesXml() {
        def String expectedXml = """
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
        File xmlFile = project.file('test.xml')
        xmlFile.write(expectedXml)

        project.jenkins.views {
            test {
                xml(xmlFile)
                xml override { viewXml ->
                    viewXml.filterExecutors = 'true'
                }
            }
        }

        expectedXml = """
            <hudson.model.ListView>
              <filterExecutors>true</filterExecutors>
              <filterQueue>false</filterQueue>
              <properties class="hudson.model.View\$PropertyList"/>
              <jobNames class="tree-set">
                <comparator class="hudson.util.CaseInsensitiveComparator"/>
              </jobNames>
              <jobFilters/>
              <columns/>
            </hudson.model.ListView>
        """

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.views.findByName('test').xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_overridesXmlFromDsl() {
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

        def String expectedXml = """
            <hudson.model.ListView>
              <filterExecutors>true</filterExecutors>
              <filterQueue>false</filterQueue>
              <properties class="hudson.model.View\$PropertyList"/>
              <jobNames class="tree-set">
                <comparator class="hudson.util.CaseInsensitiveComparator"/>
              </jobNames>
              <jobFilters/>
              <columns/>
            </hudson.model.ListView>
        """

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.views.findByName('test').xml))
        assert xmlDiff.similar()
    }

    @Test
    def void configure_overridesXmlFromDslFile() {
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            view(type: ListView) {
                name "test"
            }
        """)
        project.jenkins.views {
            test {
                dsl dslFile
                xml override { viewXml ->
                    viewXml.filterExecutors = 'true'
                }
            }
        }

        String expectedXml = """
            <hudson.model.ListView>
              <filterExecutors>true</filterExecutors>
              <filterQueue>false</filterQueue>
              <properties class="hudson.model.View\$PropertyList"/>
              <jobNames class="tree-set">
                <comparator class="hudson.util.CaseInsensitiveComparator"/>
              </jobNames>
              <jobFilters/>
              <columns/>
            </hudson.model.ListView>
        """

        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new DetailedDiff(new Diff(expectedXml, project.jenkins.views.findByName('test').xml))
        assert xmlDiff.similar()
    }
}
