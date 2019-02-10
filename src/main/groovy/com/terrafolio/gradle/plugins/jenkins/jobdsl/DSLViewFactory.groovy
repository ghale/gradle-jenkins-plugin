package com.terrafolio.gradle.plugins.jenkins.jobdsl

import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.View


interface DSLViewFactory {
    View createView(JobManagement management, String type, String viewName)
}
