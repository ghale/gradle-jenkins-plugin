package com.terrafolio.gradle.plugins.jenkins.service

/**
 * Created by ghale on 5/12/14.
 */
class JenkinsRESTServiceFactory implements JenkinsServiceFactory {
    @Override
    JenkinsService getService(String url) {
        return new JenkinsRESTServiceImpl(url)
    }

    @Override
    JenkinsService getService(String url, String username, String password) {
        return new JenkinsRESTServiceImpl(url, username, password)
    }
}
