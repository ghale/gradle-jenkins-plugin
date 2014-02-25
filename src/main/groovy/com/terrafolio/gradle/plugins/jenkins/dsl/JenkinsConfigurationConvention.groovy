package com.terrafolio.gradle.plugins.jenkins.dsl

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.util.ConfigureUtil

class JenkinsConfigurationConvention {
	JenkinsConfiguration jenkins
	
	public JenkinsConfigurationConvention(JenkinsConfiguration jenkins) {
		this.jenkins = jenkins
	}
	
	def jenkins(closure) {
		ConfigureUtil.configure(closure, jenkins)
	}
}
