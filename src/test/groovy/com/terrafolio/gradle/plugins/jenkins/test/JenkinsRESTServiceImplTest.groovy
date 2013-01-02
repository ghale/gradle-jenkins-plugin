package com.terrafolio.gradle.plugins.jenkins.test;

import static org.junit.Assert.*

import org.apache.http.HttpResponse
import org.apache.http.message.BasicHttpResponse
import org.apache.http.message.BasicStatusLine
import org.apache.http.HttpVersion
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import org.junit.Before
import org.junit.Test

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.JenkinsConfiguration
import com.terrafolio.gradle.plugins.jenkins.JenkinsRESTServiceImpl
import com.terrafolio.gradle.plugins.jenkins.JenkinsServiceException
import com.terrafolio.gradle.plugins.jenkins.PreemptiveAuthInterceptor

import groovy.mock.interceptor.MockFor
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient

class JenkinsRESTServiceImplTest {
	private MockFor mockRESTClient
	def url = "http://testurl.availity.com"
	def username = "testuser"
	def password = "testpassword"
	
	@Before
	def void setupProject() {
		mockRESTClient = new MockFor(RESTClient.class)
	}
	
	@Test 
	def void getJobConfiguration_returnsXmlString() {
		mockRESTClient.demand.with {
			get() { Map<String, ?> map ->
				HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
				HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
				return response
			}
		}
		
		mockRESTClient.ignore('getClient')
		
		mockRESTClient.use {
			def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
			def xml = service.getJobConfiguration("compile")
			assert xml == "<test1><test2>srv value</test2></test1>"
		}
	}
	
	@Test
	def void getJobConfiguration_returnsNullOnNotFound() {
		mockRESTClient.demand.with {
			get() { Map<String, ?> map ->
				HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 404, "Not Found"))
				HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
				throw new HttpResponseException(response)
			}
		}
		
		mockRESTClient.ignore('getClient')
		
		mockRESTClient.use {
			def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
			def xml = service.getJobConfiguration("compile")
			assert xml == null
		}
	}
	
	@Test (expected = JenkinsServiceException.class)
	def void getJobConfiguration_throwsExceptionOnFailure() {
		mockRESTClient.demand.with {
			get() { Map<String, ?> map ->
				HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Error"))
				HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
				return response
			}
		}
		
		mockRESTClient.ignore('getClient')
		
		mockRESTClient.use {
			def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
			def xml = service.getJobConfiguration("compile")
		}
	}
	
	@Test 
	def void createJob_postsConfigXml() {
		def postMap
		mockRESTClient.demand.with {
			post() { Map<String, ?> map ->
				postMap = map
				HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
				HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
				return response
			}
		}
		
		mockRESTClient.ignore('getClient')
		
		mockRESTClient.use {
			def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
			service.createJob("compile", "<test1><test2>srv value</test2></test1>")
			assert postMap.body == "<test1><test2>srv value</test2></test1>"
			assert postMap.query.name == "compile"
			assert postMap.path == "/createItem"
		}
	}
	
	@Test (expected = JenkinsServiceException.class)
	def void createJob_throwsExceptionOnFailure() {
		mockRESTClient.demand.with {
			post() { Map<String, ?> map ->
				HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Error"))
				HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
				return response
			}
		}
		
		mockRESTClient.ignore('getClient')
		
		mockRESTClient.use {
			def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
			service.createJob("compile", "<test1><test2>srv value</test2></test1>")
		}
	}
	
	@Test
	def void updateJobConfiguration_postsConfigXml() {
		def postMap
		mockRESTClient.demand.with {
			post() { Map<String, ?> map ->
				postMap = map
				HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
				HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
				return response
			}
		}
		
		mockRESTClient.ignore('getClient')
		
		mockRESTClient.use {
			def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
			service.updateJobConfiguration("compile", "<test1><test2>srv value</test2></test1>")
			assert postMap.body == "<test1><test2>srv value</test2></test1>"
			assert postMap.path == "/job/compile/config.xml"
		}
	}
	
	@Test (expected = JenkinsServiceException.class)
	def void updateJobConfiguration_throwsExceptionOnFailure() {
		mockRESTClient.demand.with {
			post() { Map<String, ?> map ->
				HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Error"))
				HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
				return response
			}
		}
		
		mockRESTClient.ignore('getClient')
		
		mockRESTClient.use {
			def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
			service.updateJobConfiguration("compile", "<test1><test2>srv value</test2></test1>")
		}
	}
	
	@Test
	def void deleteJob_postsToUrl() {
		def postMap
		mockRESTClient.demand.with {
			post() { Map<String, ?> map ->
				postMap = map
				HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
				HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
				return response
			}
		}
		
		mockRESTClient.ignore('getClient')
		
		mockRESTClient.use {
			def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
			service.deleteJob("compile")
			assert postMap.path == "/job/compile/doDelete"
		}
	}
	
	@Test (expected = JenkinsServiceException.class)
	def void deleteJob_throwsExceptionOnFailure() {
		mockRESTClient.demand.with {
			post() { Map<String, ?> map ->
				HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Error"))
				HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
				return response
			}
		}
		
		mockRESTClient.ignore('getClient')
		
		mockRESTClient.use {
			def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url, username, password)
			service.deleteJob("compile")
		}
	}
	
	@Test
	def void getJobConfiguration_doesNotAddInterceptorForInSecureServer() {
		mockRESTClient.demand.with {
			get() { Map<String, ?> map ->
				HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
				HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText("<test1><test2>srv value</test2></test1>"))
				return response
			}
		}
		
		mockRESTClient.ignore('getClient')
		
		mockRESTClient.use {
			def JenkinsRESTServiceImpl service = new JenkinsRESTServiceImpl(url)
			def xml = service.getJobConfiguration("compile")
			assert xml == "<test1><test2>srv value</test2></test1>"
			for (int i=0; i < service.client.client.getRequestInterceptorCount(); i++) { 
				assert ! (service.client.client.getRequestInterceptor(i) instanceof PreemptiveAuthInterceptor)
			}
		}
	}
}
