package com.duxet.zkmap.remote;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.w3c.dom.Document;


public class HTTPClient {
	public static String token;
	public static List<String> cookies;

	public static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:19.0) Gecko/20100101 Firefox/19.0";
	
	
	public static Document callGET(String url){
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		
		if(cookies == null || cookies.isEmpty() || token == null)
			return null;

        get.addHeader("User-Agent", userAgent);
        		
        for (String cookie : cookies) {
             get.addHeader("Cookie", cookie.split(";", 2)[0]);
        }
        
        HttpResponse response = null;
		try {
			response = client.execute(get);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        return processResponse(response);
	}
	
	public static Document callSOAP(String url, String action, String body){
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		
		if(cookies == null || cookies.isEmpty() || token == null)
			return null;

        post.addHeader("User-Agent", userAgent);
        post.addHeader("SOAPAction", action);
        
        for (String cookie : cookies) {
            post.addHeader("Cookie", cookie.split(";", 2)[0]);
       }
        
        // Add post body with proper content
        String withToken = body.replace("%token%", token);
		try {
			StringEntity entity = new StringEntity(withToken, "UTF-8");
			entity.setContentType(new BasicHeader("Content-Type", "text/xml"));
	        post.setEntity(entity);
		} catch (Exception e) {
		}
        
        HttpResponse response = null;
		try {
			response = client.execute(post);
		} catch (Exception e) {
			return null;
		}
        
        return processResponse(response);
	}
	
	private static Document processResponse(HttpResponse response)
	{
		HttpEntity resEntityGet = response.getEntity(); 

        DocumentBuilderFactory builderFactory =
                DocumentBuilderFactory.newInstance();
        
        try {
        	DocumentBuilder builder = builderFactory.newDocumentBuilder();
			return builder.parse(resEntityGet.getContent());
		} catch (Exception e) {
			return null;
		}  
	}
}
