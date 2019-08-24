package com.terrafolio.gradle.plugins.tasks;

import org.gradle.api.GradleScriptException;

public class JenkinsValidationException extends GradleScriptException {

    public JenkinsValidationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
