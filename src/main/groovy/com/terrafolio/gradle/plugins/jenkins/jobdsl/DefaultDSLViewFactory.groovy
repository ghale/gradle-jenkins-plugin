package com.terrafolio.gradle.plugins.jenkins.jobdsl

import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.View

class DefaultDSLViewFactory implements DSLViewFactory {
    @Override
    View createView(JobManagement management, String type, String viewName) {
        Class<? extends View> viewClass
        if (type == null) {
            viewClass = DSLViewType.ListView.viewClass
        } else {
            viewClass = DSLViewType.find(type).viewClass
        }

        return viewClass.newInstance(management, viewName)
    }
}
