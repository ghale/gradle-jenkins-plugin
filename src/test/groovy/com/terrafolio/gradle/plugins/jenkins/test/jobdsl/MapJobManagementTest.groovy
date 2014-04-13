package com.terrafolio.gradle.plugins.jenkins.test.jobdsl

import com.terrafolio.gradle.plugins.jenkins.jobdsl.MapJobManagement
import javaposse.jobdsl.dsl.ConfigurationMissingException
import javaposse.jobdsl.dsl.NameNotProvidedException
import org.junit.Test

/**
 * Created by ghale on 4/13/14.
 */
class MapJobManagementTest {
    @Test
    def void setsNewJob() {
        Map map = new HashMap<String, String>()
        def jm = new MapJobManagement(map)
        jm.createOrUpdateConfig("test", "<project />", true)
        assert map.get("test") == "<project />"
    }

    @Test
    def void getConfig_returnsJobConfiguration() {
        Map map = new HashMap<String, String>()
        def jm = new MapJobManagement(map)
        jm.createOrUpdateConfig("test", "<project />", true)
        assert jm.getConfig("test") == "<project />"
    }

    @Test
    def void setsNewView() {
        Map map = new HashMap<String, String>()
        def jm = new MapJobManagement(map)
        jm.createOrUpdateView("test", "<view />", true)
        assert map.get("test") == "<view />"
    }

    @Test
    def void getConfig_returnsViewConfiguration() {
        Map map = new HashMap<String, String>()
        def jm = new MapJobManagement(map)
        jm.createOrUpdateView("test", "<view />", true)
        assert jm.getConfig("test") == "<view />"
    }

    @Test (expected = NameNotProvidedException)
    def void throwsExceptionOnNullJobName() {
        Map map = new HashMap<String, String>()
        def jm = new MapJobManagement(map)
        jm.createOrUpdateConfig("", "<project />", true)
    }

    @Test (expected = NameNotProvidedException)
    def void throwsExceptionOnNullViewName() {
        Map map = new HashMap<String, String>()
        def jm = new MapJobManagement(map)
        jm.createOrUpdateView("", "<view />", true)
    }

    @Test (expected = ConfigurationMissingException)
    def void throwsExceptionOnNullJobConfiguration() {
        Map map = new HashMap<String, String>()
        def jm = new MapJobManagement(map)
        jm.createOrUpdateConfig("test", null, true)
    }

    @Test (expected = ConfigurationMissingException)
    def void throwsExceptionOnNullViewConfiguration() {
        Map map = new HashMap<String, String>()
        def jm = new MapJobManagement(map)
        jm.createOrUpdateView("test", "", true)
    }

    @Test
    def void returnsNullOnMissingConfiguration() {
        Map map = new HashMap<String, String>()
        def jm = new MapJobManagement(map)
        assert jm.getConfig("test") == null
    }
}
