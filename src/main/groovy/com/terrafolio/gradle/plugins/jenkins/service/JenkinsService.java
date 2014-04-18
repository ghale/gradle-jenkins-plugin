package com.terrafolio.gradle.plugins.jenkins.service;

import java.util.Map;

public interface JenkinsService {

    public String getConfiguration(String jobName, Map overrides) throws JenkinsServiceException;

    public void createConfiguration(String jobName, String configXml, Map overrides) throws JenkinsServiceException;

    public void updateConfiguration(String jobName, String configXml, Map overrides) throws JenkinsServiceException;

    public void deleteConfiguration(String jobName, Map overrides) throws JenkinsServiceException;
}
