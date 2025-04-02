package uk.co.asepstrath.bank.services.data;

import kong.unirest.core.GetRequest;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.RequestBodyEntity;
import kong.unirest.core.Unirest;

import java.util.Map;

public class UnirestWrapper {


    /**
     * Performs a GET request to the specified URL
     *
     * @param url The base URL to send the request to
     * @return HttpResponse containing the response as a String
     */
    public HttpResponse<String> get(String url) {
        return Unirest.get(url).asString();
    }

    /**
     * Performs a GET request with a single query parameter
     *
     * @param url        The base URL to send the request to
     * @param paramName  The name of the query parameter
     * @param paramValue The value of the query parameter
     * @return HttpResponse containing the response as a String
     */
    public HttpResponse<String> get(String url, String paramName, Object paramValue) {
        return Unirest.get(url).queryString(paramName, paramValue).asString();
    }

    /**
     * Performs a GET request with query parameters and headers
     *
     * @param url     The base URL to send the request to
     * @param params  Map of parameter names to values
     * @param headers Map of header names to values
     * @return HttpResponse containing the response as a String
     */
    public HttpResponse<String> get(String url, Map<String, Object> params, Map<String, String> headers) {
        GetRequest request = Unirest.get(url);

        if (params != null) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                request.queryString(param.getKey(), param.getValue());
            }
        }

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.header(header.getKey(), header.getValue());
            }
        }

        return request.asString();
    }


    public HttpResponse<String> post(String url, String body, Map<String, String> headers) {
        RequestBodyEntity request = Unirest.post(url).body(body);

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.header(header.getKey(), header.getValue());
            }
        }

        return request.asString();
    }
}