package org.gradle.plugins.jenkins

import groovy.xml.StreamingMarkupBuilder

class JenkinsJobDefinition {
	def name
	def xml
	
	JenkinsJobDefinition() { }
	
	JenkinsJobDefinition(String name) {
		this.name = name
	}
	
	JenkinsJobDefinition(String name, String xml) {
		this.name = name
		this.xml = xml
	}
	
	def String override(Closure closure) { 
		def newXml = new XmlSlurper().parseText(xml)
		closure.call(newXml)
		def sbuilder = new StreamingMarkupBuilder()
		return sbuilder.bind { mkp.yield newXml }.toString()
	}
	
	def name(String name) {
		this.name = name
	}
	
	def xml(String xml) {
		this.xml = xml
	}
	
	def xml(File xmlFile) {
		this.xml = xmlFile.getText()
	}
	
	def xml(Closure closure) {
		def sbuilder = new StreamingMarkupBuilder()
		this.xml = sbuilder.bind(closure).toString()
	}
}
