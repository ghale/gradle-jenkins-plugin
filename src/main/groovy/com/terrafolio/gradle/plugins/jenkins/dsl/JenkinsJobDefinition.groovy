package com.terrafolio.gradle.plugins.jenkins.dsl

class JenkinsJobDefinition implements XMLSupport, XMLConfigurable {
    def name

    JenkinsJobDefinition(String name) {
        this.name = name
    }

    JenkinsJobDefinition(String name, String xml) {
        this(name)
        setXml(xml)
    }

    def name(String name) {
        this.name = name
    }
}
