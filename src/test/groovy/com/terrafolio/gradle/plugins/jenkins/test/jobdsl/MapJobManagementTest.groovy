package com.terrafolio.gradle.plugins.jenkins.test.jobdsl

import com.terrafolio.gradle.plugins.jenkins.jobdsl.MapJobManagement
import javaposse.jobdsl.dsl.ConfigurationMissingException
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.NameNotProvidedException
import spock.lang.Specification

/**
 * Created by ghale on 4/13/14.
 */
class MapJobManagementTest extends Specification {
    def HashMap<String, String> map
    def JobManagement jm

    def setup() {
        map = new HashMap<String, String>()
        jm = new MapJobManagement(map)
    }

    def "createOrUpdateConfig sets new config" () {
        when:
        jm.createOrUpdateConfig("test", "<project />", true)

        then:
        map.get("test") == "<project />"
    }

    def "getConfig returns set config" () {
        setup:
        jm.createOrUpdateConfig("test", "<project />", true)

        expect:
        jm.getConfig("test") == "<project />"
    }

    def "createOrUpdateView sets new view config" () {
        when:
        jm.createOrUpdateView("test", "<view />", true)

        then:
        map.get("test") == "<view />"
    }

    def "getConfig returns set view config" () {
        setup:
        jm.createOrUpdateView("test", "<view />", true)

        expect:
        jm.getConfig("test") == "<view />"
    }

    def "createOrUpdateConfig throws exception on empty name" () {
        when:
        jm.createOrUpdateConfig("", "<project />", true)

        then:
        thrown(NameNotProvidedException)
    }

    def "createOrUpdateView throws exception on empty view name" () {
        when:
        jm.createOrUpdateView("", "<view />", true)

        then:
        thrown(NameNotProvidedException)
    }

    def "createOrUpdateConfig throws exception on empty config" () {
        when:
        jm.createOrUpdateConfig("test", null, true)

        then:
        thrown(ConfigurationMissingException)
    }

    def "createOrUpdateView throws exception on empty view config" () {
        when:
        jm.createOrUpdateView("test", "", true)

        then:
        thrown(ConfigurationMissingException)
    }

    def "getConfig returns null for missing configuration" () {
        expect:
        jm.getConfig("test") == null
    }
}
