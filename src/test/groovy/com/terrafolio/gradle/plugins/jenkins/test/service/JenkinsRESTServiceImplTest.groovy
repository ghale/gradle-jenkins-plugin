package com.terrafolio.gradle.plugins.jenkins.test.service

import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsServiceException
import com.terrafolio.gradle.plugins.jenkins.service.PreemptiveAuthInterceptor
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpRequestInterceptor
import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.impl.client.AbstractHttpClient
import org.apache.http.message.BasicHttpResponse
import org.apache.http.message.BasicStatusLine
import org.junit.Ignore
import spock.lang.Specification

class JenkinsRESTServiceImplTest extends Specification {
    def RESTClient mockRESTClient
    def JenkinsRESTServiceImpl service
    def url = "http://testurl.availity.com"
    def username = "testuser"
    def password = "testpassword"

    def setup() {
        mockRESTClient = Mock(RESTClient)
        service = new JenkinsRESTServiceImpl(url, username, password)
        service.client = mockRESTClient
    }

    def "getConfiguration returns xml string" () {
        setup:
        def xml = "<test1><test2>srv value</test2></test1>"
        1 * mockRESTClient.get(_) >> { Map map ->
            HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
            HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, new XmlSlurper().parseText(xml))
            return response
        }

        expect:
        service.getConfiguration("compile", [:]) == xml
    }

    def "getConfiguration returns null on not found" () {
        setup:
        1 * mockRESTClient.get(_) >> { Map map ->
            HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 404, "Not Found"))
            HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
            throw new HttpResponseException(response)
        }

        expect:
        service.getConfiguration("compile", [:]) == null
    }

    def "getConfiguration throws exception on failure" () {
        when:
        service.getConfiguration("compile", [:])

        then:
        1 * mockRESTClient.get(_) >> { Map map ->
            HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Error"))
            HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
            return response
        }
        thrown(JenkinsServiceException)
    }

    def "createConfiguration posts correct information" () {
        setup:
        def xml = "<test1><test2>srv value</test2></test1>"

        when:
        service.createConfiguration("compile", xml, [uri: "/createItem", params: [name: "compile"]])

        then:
        1 * mockRESTClient.post({
            it.body == xml &&
            it.query.name == "compile" &&
            it.path == "/createItem"
        }) >> { Map<String, ?> map ->
            HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
            HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
            return response
        }
    }

    def "createConfiguration throws exception on failure" () {
        when:
        service.createConfiguration("compile", "<test1><test2>srv value</test2></test1>", [:])

        then:
        1 * mockRESTClient.post(_) >> { Map map ->
            HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Error"))
            HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
            return response
        }
        thrown(JenkinsServiceException)
    }

    def "updateConfiguration posts correct information" () {
        setup:
        def xml = "<test1><test2>srv value</test2></test1>"

        when:
        service.updateConfiguration("compile", xml, [uri: "/job/compile/config.xml"])

        then:
        1 * mockRESTClient.post({
            it.body == xml &&
                    it.path == "/job/compile/config.xml"
        }) >> { Map<String, ?> map ->
            HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
            HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
            return response
        }
    }

    def "updateConfiguration throws exception on failure" () {
        when:
        service.updateConfiguration("compile", "<test1><test2>srv value</test2></test1>", [:])

        then:
        1 * mockRESTClient.post(_) >> { Map map ->
            HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Error"))
            HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
            return response
        }
        thrown(JenkinsServiceException)
    }

    def "deleteConfiguration posts correct information" () {
        when:
        service.deleteConfiguration("compile", [uri: "/job/compile/doDelete"])

        then:
        1 * mockRESTClient.post({
            it.path == "/job/compile/doDelete"
        }) >> { Map<String, ?> map ->
            HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
            HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
            return response
        }
    }

    def "deleteConfiguration throws exception on failure" () {
        when:
        service.deleteConfiguration("compile", [:])

        then:
        1 * mockRESTClient.post(_) >> { Map map ->
            HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 500, "Error"))
            HttpResponseDecorator response = new HttpResponseDecorator(baseResponse, null)
            return response
        }
        thrown(JenkinsServiceException)
    }

    @Ignore
    def HttpRequestInterceptor getInterceptorbyType(AbstractHttpClient client, Class theclass) {
        for (int i = 0; i < client.getRequestInterceptorCount(); i++) {
            if (client.getRequestInterceptor(i).class == theclass) {
                return client.getRequestInterceptor(i)
            }
        }
        return null
    }

    def "getRestClient adds interceptor for secure server" () {
        setup:
        def service = new JenkinsRESTServiceImpl(url, username, password)

        expect:
        getInterceptorbyType(service.getRestClient().client, PreemptiveAuthInterceptor) != null
        getInterceptorbyType(service.getRestClient().client, PreemptiveAuthInterceptor).username == username
        getInterceptorbyType(service.getRestClient().client, PreemptiveAuthInterceptor).password == password
    }

    def "getRestClient does not add interceptor for insecure server" () {
        setup:
        def service = new JenkinsRESTServiceImpl(url)

        expect:
        getInterceptorbyType(service.getRestClient().client, PreemptiveAuthInterceptor) == null
    }
}
