package com.terrafolio.gradle.plugins.jenkins.dsl

import javaposse.jobdsl.dsl.*
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.FileCollection

class JenkinsConfiguration {
    private final NamedDomainObjectContainer<JenkinsJob> jobs
    private final NamedDomainObjectContainer<JenkinsServerDefinition> servers
    private final NamedDomainObjectContainer<JenkinsJobTemplate> templates
    private final NamedDomainObjectContainer<JenkinsView> views
    private final JobManagement jm

    def defaultServer

    public JenkinsConfiguration(NamedDomainObjectContainer<JenkinsJob> jobs,
                                NamedDomainObjectContainer<JenkinsJobTemplate> templates,
                                NamedDomainObjectContainer<JenkinsServerDefinition> servers,
                                NamedDomainObjectContainer<JenkinsView> views,
                                JobManagement jm) {
        this.jobs = jobs
        this.servers = servers
        this.templates = templates
        this.views = views
        this.jm = jm
    }

    def jobs(Closure closure) {
        jobs.configure(closure)
    }

    def templates(Closure closure) {
        templates.configure(closure)
    }

    def servers(Closure closure) {
        servers.configure(closure)
    }

    def views(Closure closure) {
        views.configure(closure)
    }

    def defaultServer(JenkinsServerDefinition server) {
        this.defaultServer = server
    }

    def dsl(FileCollection files) {
        jobs.each { job ->
            if (job.definition != null && job.definition.xml != null) {
                jm.createOrUpdateConfig(job.definition.name, job.definition.xml, true)
            }
        }

        files.each { dslFile ->
            ScriptRequest request = new ScriptRequest(dslFile.name, null, dslFile.parentFile.toURI().toURL(), false)
            GeneratedItems generatedItems = DslScriptLoader.runDslEngine(request, jm)

            generatedItems.getJobs().each { generatedJob ->
                def JenkinsJob job = jobs.findByName(generatedJob.jobName)
                if (job == null) {
                    job = new JenkinsJob(generatedJob.jobName, jm)
                }
                job.definition = new JenkinsJobDefinition(generatedJob.jobName)
                job.definition.xml jm.getConfig(generatedJob.jobName)
                jobs.add(job)
            }

            generatedItems.getViews().each { GeneratedView generatedView ->
                def JenkinsView view = jobs.findByName(generatedView.name)
                if (view == null) {
                    view = new JenkinsView(generatedView.name, jm)
                }
                view.xml = jm.getConfig(generatedView.name)
                views.add(view)
            }
        }
    }

    def dsl(Closure closure) {
        def JobParent jobParent = new JobParent() {
            def run() { }
        }
        jobParent.setJm(jm)
        jobParent.with(closure)

        jobParent.getReferencedJobs().each { referencedJob ->
            jm.createOrUpdateConfig(referencedJob.name, referencedJob.xml, true)
            def JenkinsJob job = jobs.findByName(referencedJob.name)
            if (job == null) {
                job = new JenkinsJob(referencedJob.name, jm)
            }
            job.definition = new JenkinsJobDefinition(referencedJob.name)
            job.definition.xml referencedJob.xml
            jobs.add(job)
        }

        jobParent.getReferencedViews().each { referencedView ->
            jm.createOrUpdateView(referencedView.name, referencedView.xml, true)
            def JenkinsView view = jobs.findByName(referencedView.name)
            if (view == null) {
                view = new JenkinsView(referencedView.name, jm)
            }
            view.xml = referencedView.xml
            views.add(view)
        }
    }
}
