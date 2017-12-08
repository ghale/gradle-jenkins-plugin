[![Build Status](https://travis-ci.org/crc83/gradle-jenkins-plugin.svg?branch=master)](https://travis-ci.org/crc83/gradle-jenkins-plugin.svg?branch=master)


[![Coverage Status](https://coveralls.io/repos/github/crc83/gradle-jenkins-plugin/badge.svg?branch=master)](https://coveralls.io/github/crc83/gradle-jenkins-plugin?branch=master)

gradle-jenkins-plugin
=====================

Forked from here : https://github.com/ghale/gradle-jenkins-plugin

Gradle version updated to 4.3.1
Jenkins DSL updated to 1.66

Gradle plugin to programmatically configure Jenkins jobs.  This plugin allows you to maintain jenkins job configurations in source control and apply them to the server via gradle.  Jobs can be stored as straight xml files, xml strings, markup builder closures, or jenkins job dsl.  Job templates can be defined that can then be manipulated such that multiple jobs can be generated off of a single template definition.

See https://github.com/ghale/gradle-jenkins-plugin/wiki for details on usage.
Since this plugin uses latest Jenkins DSL. Please refer documentation here for more details https://github.com/jenkinsci/job-dsl-plugin/wiki

To release plugin
----------------------
Run locally 'gradle release'.
Tag will be created and plugin will be published to gradle repository.
Please refer [https://plugins.gradle.org/docs/publish-plugin] to configure local publishing

Improvements
----------------------
1)[1.4.3] "Invalid crumb" issue fixed
2)[1.4.4] Add possibility to import job into folder. If your folder on Jenkins is "Folder1/SubFolred1", please specify it as
(no slash at the beginning and slash at the end) 
(it's temporary solution and maybe I'll improve it in a future)

[source, groovy]
----
jobs{
	my_job{
		...
		folder = "job/Folder1/job/SubFolder1/"
		...
	}
}
-----