package com.terrafolio.gradle.plugins.jenkins.service;

import java.util.Map;

public interface JenkinsService {
	public String getJobConfiguration(String jobName) throws JenkinsServiceException;
	
	public String getJobConfiguration(String jobName, Map overrides) throws JenkinsServiceException;
	
	public void createJob(String jobName, String configXml) throws JenkinsServiceException;
	
	public void createJob(String jobName, String configXml, Map overrides) throws JenkinsServiceException;
	
	public void updateJobConfiguration(String jobName, String configXml) throws JenkinsServiceException;
	
	public void updateJobConfiguration(String jobName, String configXml, Map overrides) throws JenkinsServiceException;
	
	public void deleteJob(String jobName) throws JenkinsServiceException;
	
	public void deleteJob(String jobName, Map overrides) throws JenkinsServiceException;
}
