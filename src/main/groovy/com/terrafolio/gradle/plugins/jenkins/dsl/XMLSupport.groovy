package com.terrafolio.gradle.plugins.jenkins.dsl

import groovy.util.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder

/**
 * Created by ghale on 6/2/14.
 */
trait XMLSupport implements BasicXMLSupport {
    def String xml

    @Override
    def String override(Closure closure) {
        def newXml = new XmlSlurper().parseText(xml)
        closure.call(newXml)
        return XmlToString.from(newXml)
    }

    @Override
    void setXml(String xml) {
        this.xml = xml
    }

    @Override
    def void xml(String xml) {
        setXml(xml)
    }

    @Override
    def void xml(File xmlFile) {
        setXml(xmlFile.getText())
    }

    @Override
    def void xml(Closure closure) {
        setXml(XmlToString.from(closure))
    }

    static class XmlToString {
        static String from(GPathResult node) {
            return new StreamingMarkupBuilder().bind { mkp.yield node }.toString()
        }

        static String from(Closure closure) {
            return new StreamingMarkupBuilder().bind(closure).toString()
        }
    }
}
