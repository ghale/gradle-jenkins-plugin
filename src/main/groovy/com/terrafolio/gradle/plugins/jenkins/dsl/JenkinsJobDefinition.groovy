package com.terrafolio.gradle.plugins.jenkins.dsl

class JenkinsJobDefinition implements XMLConfigurable {
    def name

    @Delegate
    def XMLSupport xmlSupport

    JenkinsJobDefinition() {
        xmlSupport = new DefaultXMLSupport()
    }

    JenkinsJobDefinition(String name) {
        this()
        this.name = name
    }

    JenkinsJobDefinition(String name, String xml) {
        this(name)
        xmlSupport.setXml(xml)
    }

    def name(String name) {
        this.name = name
    }

    @Override
    XMLSupport getXMLSupport() {
        return xmlSupport
    }

    @Override
    void setXMLSupport(XMLSupport support) {
        this.xmlSupport = support
    }
}
