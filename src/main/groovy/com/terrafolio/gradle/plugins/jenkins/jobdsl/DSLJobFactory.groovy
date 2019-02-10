package com.terrafolio.gradle.plugins.jenkins.jobdsl

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobManagement


interface DSLJobFactory {
    Job create(JobManagement management, String type, String jobName)
}
