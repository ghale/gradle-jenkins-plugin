package com.terrafolio.gradle.plugins.jenkins.test.dsl

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurationException
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsServerDefinition
import spock.lang.Specification

class JenkinsServerDefinitionTest extends Specification {
    def JenkinsServerDefinition definitionUnderTest

    def setup() {
        definitionUnderTest = new JenkinsServerDefinition("test")
    }

    def "configure converts all server urls to end with slash" (url, expectedUrl) {
        setup:
        definitionUnderTest.url = url

        expect:
        definitionUnderTest.url == expectedUrl

        where:
        url                    | expectedUrl
        'http://test1'         | 'http://test1/'
        'http://test2/jenkins' | 'http://test2/jenkins/'
        'http://test3/'        | 'http://test3/'
    }

    def "checkDefinitionValues allows missing username for insecure server" () {
        setup:
        definitionUnderTest.url 'http://test1'
        definitionUnderTest.secure false

        when:
        definitionUnderTest.checkDefinitionValues()

        then:
        noExceptionThrown()
    }

    def "checkDefinitionValues throws exception on missing username for secure server" () {
        setup:
        definitionUnderTest.url 'http://test1'
        definitionUnderTest.secure true
        definitionUnderTest.password = 'testpass'

        when:
        definitionUnderTest.checkDefinitionValues()

        then:
        thrown(JenkinsConfigurationException)
    }

    def "checkDefinitionValues throws exception on missing password for secure server" () {
        setup:
        definitionUnderTest.url 'http://test1'
        definitionUnderTest.secure true
        definitionUnderTest.username = 'testuser'

        when:
        definitionUnderTest.checkDefinitionValues()

        then:
        thrown(JenkinsConfigurationException)
    }
}
