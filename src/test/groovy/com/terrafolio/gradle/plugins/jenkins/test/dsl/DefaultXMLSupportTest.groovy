package com.terrafolio.gradle.plugins.jenkins.test.dsl

import com.terrafolio.gradle.plugins.jenkins.dsl.DefaultXMLSupport
import com.terrafolio.gradle.plugins.jenkins.dsl.XMLSupport
import com.terrafolio.gradle.plugins.jenkins.test.TempDirSpec
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

/**
 * Created on 6/27/14.
 */
class DefaultXMLSupportTest extends TempDirSpec {
    XMLSupport support

    def setup() {
        support = new DefaultXMLSupport()
    }

    def "xml from file loads correctly" () {
        setup:
        File file = file("test.xml", JobFixtures.FREEFORM_DSL_JOB_XML)

        when:
        support.xml(file)

        then:
        support.xml == JobFixtures.FREEFORM_DSL_JOB_XML
    }

    def "xml from string loads correctly" () {
        when:
        support.xml(JobFixtures.FREEFORM_DSL_JOB_XML)

        then:
        support.xml == JobFixtures.FREEFORM_DSL_JOB_XML
    }

    def "xml from closure loads correctly" () {
        when:
        support.xml {
            project() {
                displayName('test')
            }
        }

        then:
        support.xml == '<project><displayName>test</displayName></project>'
    }

    def "override correctly overrides xml" () {
        setup:
        support.xml(JobFixtures.FREEFORM_DSL_JOB_XML)
        String expectedXml = JobFixtures.FREEFORM_DSL_JOB_XML.replaceFirst('false', 'true')
        XMLUnit.setIgnoreWhitespace(true)

        expect:
        new Diff(expectedXml, support.override { it.keepDependencies = 'true' }).similar()
    }
}
