package com.terrafolio.gradle.plugins.jenkins

import java.util.Map;

import groovy.xml.StreamingMarkupBuilder
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseException
import static groovyx.net.http.ContentType.*

class JenkinsRESTServiceImpl implements JenkinsService {
	private RESTClient client
	def url
	def username
	def password
	
	public JenkinsRESTServiceImpl(String url, String username, String password) {
		this.url = url
		this.username = username
		this.password = password
	}
	
	public JenkinsRESTServiceImpl(String url) {
		this.url = url
	}
	
	def getRestClient() {
		if (client == null) {
			client = new RESTClient(url)
			if (username != null) {
				client.client.addRequestInterceptor(new PreemptiveAuthInterceptor(username, password))
			}
		}
		
		return client
	}
	
	def restServiceGET(path, query) {
		def client = getRestClient()
		
		def response = client.get(path: path, query: query) 
		if (response.success) {
			return response.getData()
		} else {
			throw new Exception('REST Service call failed with response code: ' + response.status)
		}
	}
	
	def restServicePOST(path, query, payload) {
		def client = getRestClient()
		def lastException

		def response = client.post(path: path, query: query, requestContentType: XML, body: payload)
		
		if (response) {
			if (response.success) {
				return response.getData()
			} else {
				throw new Exception('REST Service call failed with response code: ' + response.status)
			}
		} else return null;
	}
	
	@Override
	public String getJobConfiguration(String jobName) throws JenkinsServiceException {
		return getJobConfiguration(jobName, [:])
	}
	
	@Override
	public String getJobConfiguration(String jobName, Map overrides)
			throws JenkinsServiceException {
		def responseXml
		try {
			def uri = "/job/${jobName}/config.xml"
			def params = [:]
			
			if (overrides.containsKey("uri")) {
				uri = overrides.uri
			}
			
			if (overrides.containsKey("params")) {
				params = overrides.params
			}
			
			responseXml = restServiceGET(uri, params)
		} catch (HttpResponseException hre) {
			if (hre.response.status == 404) {
				responseXml = null
			} else {
				throw new JenkinsServiceException("Jenkins Service Call failed", hre)
			}
		} catch (Exception e) {
			throw new JenkinsServiceException("Jenkins Service Call failed", e)
		}
		
		if (responseXml != null) {
			def sbuilder = new StreamingMarkupBuilder()
			return sbuilder.bind { mkp.yield responseXml }.toString()
		} else {
			return null
		}
	}

	@Override
	public void updateJobConfiguration(String jobName, String configXml) throws JenkinsServiceException {
		updateJobConfiguration(jobName, configXml, [:])
	}
	
	@Override
	public void updateJobConfiguration(String jobName, String configXml,
			Map overrides) throws JenkinsServiceException {
		def response
		try {
			def uri = "/job/${jobName}/config.xml"
			def params = [:]
			
			if (overrides.containsKey("uri")) {
				uri = overrides.uri
			}
			
			if (overrides.containsKey("params")) {
				params = overrides.params
			}
			
			response = restServicePOST(uri, params, configXml)
		} catch (Exception e) {
			throw new JenkinsServiceException("Jenkins Service Call failed", e)
		}
	}

	@Override
	public void deleteJob(String jobName) throws JenkinsServiceException {
		deleteJob(jobName, [:])
	}
	
	@Override
	public void deleteJob(String jobName, Map overrides)
			throws JenkinsServiceException {
		def response
		try {
			
			def uri = "/job/${jobName}/doDelete"
			def params = [:]
			
			if (overrides.containsKey("uri")) {
				uri = overrides.uri
			}
			
			if (overrides.containsKey("params")) {
				params = overrides.params
			}
			
			response = restServicePOST(uri, params, "")
		} catch (Exception e) {
			throw new JenkinsServiceException("Jenkins Service Call failed", e)
		}
		
	}

	@Override
	public void createJob(String jobName, String configXml) throws JenkinsServiceException {
		createJob(jobName, configXml, [:])
	}

	@Override
	public void createJob(String jobName, String configXml, Map overrides)
			throws JenkinsServiceException {
		def response
		try {
			def uri = "/createItem"
			def params = [ name : jobName ]
			
			if (overrides.containsKey("uri")) {
				uri = overrides.uri
			}
			
			if (overrides.containsKey("params")) {
				params = overrides.params
			}
			
			response = restServicePOST(uri, params, configXml)
		} catch (Exception e) {
			throw new JenkinsServiceException("Jenkins Service Call failed", e)
		}
		
	}

}
