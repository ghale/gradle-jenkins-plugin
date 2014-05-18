package com.terrafolio.gradle.plugins.jenkins.service

/**
 * Created by ghale on 5/12/14.
 */
public interface JenkinsServiceFactory {
    public JenkinsService getService(String url)

    public JenkinsService getService(String url, String username, String password)
}