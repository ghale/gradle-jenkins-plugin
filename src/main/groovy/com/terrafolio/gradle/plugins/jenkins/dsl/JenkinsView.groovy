package com.terrafolio.gradle.plugins.jenkins.dsl

import groovy.xml.StreamingMarkupBuilder
import javaposse.jobdsl.dsl.*
import javaposse.jobdsl.dsl.views.BuildPipelineView
import javaposse.jobdsl.dsl.views.ListView
import org.gradle.util.ConfigureUtil

/**
 * Created by ghale on 4/11/14.
 */
class JenkinsView extends JenkinsConfigurable {
    def String type
    def String xml
    protected JobManagement jm

    def defaultOverrides = {
        create([ uri: "/createView", params: [ name: name ] ])
           get([ uri: "/view/${name}/config.xml" ])
        update([ uri: "/view/${name}/config.xml" ])
        delete([ uri: "/view/${name}/doDelete" ])
    }

    private static final Map<ViewType, Class<? extends View>> VIEW_TYPE_MAPPING = [
            (null)                      : ListView.class,
            (ViewType.ListView)         : ListView.class,
            (ViewType.BuildPipelineView): BuildPipelineView.class,
    ]

    JenkinsView(String name, JobManagement jm) {
        this.name = name
        this.jm = jm
    }

    @Override
    def Closure getDefaultOverrides() {
        return this.defaultOverrides
    }

    @Override
    String getConfigurableName() {
        return name
    }

    @Override
    String getServerSpecificXml(JenkinsServerDefinition server) {
        if (serverSpecificConfiguration.containsKey(server)) {
            def JenkinsView serverSpecificView = new JenkinsView(name, jm)
            serverSpecificView.xml = xml

            serverSpecificConfiguration[server].each { closure ->
                ConfigureUtil.configure(closure, serverSpecificView)
            }

            return serverSpecificView.xml
        } else {
            return xml
        }
    }

    def type(String type) {
        this.type = type
    }

    def xml(String xml) {
        this.xml = xml
    }

    def xml(File xmlFile) {
        this.xml = xmlFile.text
    }

    def String override(Closure closure) {
        def newXml = new XmlSlurper().parseText(xml)
        closure.call(newXml)
        def sbuilder = new StreamingMarkupBuilder()
        return sbuilder.bind { mkp.yield newXml }.toString()
    }

    def dsl(File dslFile) {
        ScriptRequest request = new ScriptRequest(dslFile.name, null, dslFile.parentFile.toURI().toURL(), false)
        GeneratedItems generatedItems = DslScriptLoader.runDslEngine(request, jm)

        if (generatedItems.views.size() != 1) {
            throw new JenkinsConfigurationException("The DSL script ${dslFile.path} did not generate exactly one view (${generatedItems.views.size()})!  Use the general dsl form to generate multiple jobs from dsl.")
        } else {
            GeneratedView generatedView = generatedItems.getViews().iterator().next()
            xml = jm.getConfig(generatedView.name)
        }
    }

    def dsl(Closure closure) {
        Class<? extends View> viewClass = VIEW_TYPE_MAPPING[type as ViewType]
        View view = viewClass.newInstance()
        view.with(closure)
        xml = view.xml
        jm.createOrUpdateView(name, view.xml, true)
    }
}
