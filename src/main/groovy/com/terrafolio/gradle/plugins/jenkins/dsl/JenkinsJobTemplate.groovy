package com.terrafolio.gradle.plugins.jenkins.dsl

import com.terrafolio.gradle.plugins.jenkins.jobdsl.MapJobManagement

/**
 * Created by ghale on 4/8/14.
 */
class JenkinsJobTemplate extends JenkinsJob {
    JenkinsJobTemplate(String name, MapJobManagement jm) {
        super(name, jm)
        this.definition = new JenkinsJobDefinition(name)
    }

    @Override
    void setName(Object name) {
        super.setName(name)
        definition.name = name
    }

    // This junk is for backwards compatibility.
    // No, I'm not proud of it.
    def String getXml() {
        return definition.xml
    }

    def void setXml(String xml) {
        definition.xml = xml
        jm.createOrUpdateConfig(name, xml, true)
    }

    def void xml(String xml) {
        setXml(xml)
    }

    def void xml(File xmlFile) {
        definition.xml(xmlFile)
        jm.createOrUpdateConfig(name, definition.xml, true)
    }

    def xml(Closure closure) {
        definition.xml(closure)
        jm.createOrUpdateConfig(name, definition.xml, true)
    }

    def String override(Closure closure) {
        return definition.override(closure)
    }
}
