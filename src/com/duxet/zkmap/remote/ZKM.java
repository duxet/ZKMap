package com.duxet.zkmap.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.duxet.zkmap.models.*;

import android.util.Log;

public class ZKM {
	private static String token;
	private static final String BASE_URL = "http://www.info.zkm.pl:5000/rj_test/";

	public static void initialize() {
		try {
			URL url = new URL(BASE_URL);
			
			URLConnection con = url.openConnection();
			con.setRequestProperty("User-Agent", HTTPClient.userAgent);
			
			InputStream is = con.getInputStream();
			
			HTTPClient.cookies = con.getHeaderFields().get("Set-Cookie");
			
			Log.i("[ZKM]", "Cookies: " + HTTPClient.cookies);

	        BufferedReader in = new BufferedReader(new InputStreamReader(is));
	        String inputLine;
	        
	        // Try to find token embedded in JS function call
	        // Ex: setDomene('http://www.info.zkm.pl:5000/rj_test/PublicService.asmx','PublicService','654870257523533120');
	        Pattern p = Pattern.compile("PublicService.asmx','PublicService','(.+)'");
			
			while ((inputLine = in.readLine()) != null) {
				Matcher m = p.matcher(inputLine);
				
				while (m.find()) {
					HTTPClient.token = m.group(1);
					Log.i("[ZKM]", "Found token: "+ HTTPClient.token);
				}
				
				if(token != null)
					break;
			}
			
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// We need some additional requests to be able to catch all vehicle locations
		fix();
	}
	
	public static void updateLine(String line) {
		Document document = HTTPClient.callSOAP(BASE_URL + "PublicService.asmx",
				"http://PublicService/CNR_DajListeWariantow",
				"<?xml version='1.0' encoding='utf-8'?><soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'><soap:Body><CNR_DajListeWariantow xmlns='http://PublicService/'><routeNr>" + line + " </routeNr><secToken>%token%</secToken></CNR_DajListeWariantow></soap:Body></soap:Envelope>"
				);
		
		if(document == null)
			return;

        Element rootElement = document.getDocumentElement();
        NodeList routesList = rootElement.getElementsByTagName("D");
        
        for(int i = 0; i < routesList.getLength(); i++) {
        	Node route = routesList.item(i);
        	NamedNodeMap routeAttributes = route.getAttributes(); 

        	String desc = routeAttributes.getNamedItem("desc1_2").getNodeValue().trim();
        	String type = routeAttributes.getNamedItem("type").getNodeValue();
        	
        	Routes.addRoute(line, type, desc);
        }	
	}
	
	public static void updateVehicles() {
        Document document = HTTPClient.callGET(
        		BASE_URL + "PublicService.asmx/CNR_GoogleDajPojazdyKlient?secToken=&nr_lini=&war_trasy=&przewoznicy=&nr_zaj=&kursow=&czynnosci=&id_prz=&stan=&odch=&nb=&typy=&typy_awarii=&id_kursu="
        		);

        if(document == null)
        	return;
        
        Element rootElement = document.getDocumentElement();
        NodeList vehiclesList = rootElement.getElementsByTagName("TRemotePojazd");
        
        for(int i = 0; i < vehiclesList.getLength(); i++) {
        	Node vehicle = vehiclesList.item(i);
        	NamedNodeMap vehicleAttributes = vehicle.getAttributes(); 
        	
        	Integer id = Integer.parseInt(vehicleAttributes.getNamedItem("Nb").getNodeValue().trim());
        	String line = vehicleAttributes.getNamedItem("NumerLini").getNodeValue().trim();
        	String line_type = vehicleAttributes.getNamedItem("WarTrasy").getNodeValue().trim();
        	Integer lat = Integer.parseInt(vehicleAttributes.getNamedItem("Szerokosc").getNodeValue().trim());
        	Integer lon = Integer.parseInt(vehicleAttributes.getNamedItem("Dlugosc").getNodeValue().trim());
        	
        	Vehicle foundVehicle = VehiclesContainer.getVehicle(id);
        	
        	if(foundVehicle != null) {
        		foundVehicle.updatePosition(lat, lon);
        	} else {
        		Vehicle vehicleObject = new Vehicle(id, line, line_type, lat, lon);
            	VehiclesContainer.addVehicle(id, vehicleObject);
        	}
        }	
	}
	
	public static void updateStops() {
		Document document = HTTPClient.callSOAP(BASE_URL + "PublicService.asmx",
				"http://PublicService/DajPrzystankiGoogle",
				"<?xml version='1.0' encoding='utf-8'?><soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'><soap:Body><DajPrzystankiGoogle xmlns='http://PublicService/'><secToken>%token%</secToken></DajPrzystankiGoogle></soap:Body></soap:Envelope>"
				);		
		
		if(document == null)
        	return;
		
		Element rootElement = document.getDocumentElement();
        NodeList stopsList = rootElement.getElementsByTagName("S");
        
        for(int i = 0; i < stopsList.getLength(); i++) {
        	Node stop = stopsList.item(i);
        	NamedNodeMap stopAttributes = stop.getAttributes(); 
        	
        	Integer id = Integer.parseInt(stopAttributes.getNamedItem("nr").getNodeValue().trim());
        	String name = stopAttributes.getNamedItem("n").getNodeValue().trim();
        	String type = stopAttributes.getNamedItem("t").getNodeValue().trim();
        	Float lat = Float.parseFloat(stopAttributes.getNamedItem("y").getNodeValue().trim());
        	Float lon = Float.parseFloat(stopAttributes.getNamedItem("x").getNodeValue().trim());
        	
        	Stop foundStop = StopsContainer.getStop(id);
        	
        	if(foundStop != null) {
        		//
        	} else {
        		Stop stopObject = new Stop(id, name, type, lat, lon);
            	StopsContainer.addStop(id, stopObject);
        	}
        }	
	}
	
	private static void fix() {
		HTTPClient.callSOAP(BASE_URL + "PublicService.asmx",
				"http://PublicService/DajLinie",
				"<?xml version='1.0' encoding='utf-8'?><soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'><soap:Body><DajLinie xmlns='http://PublicService/'><typyPoj></typyPoj><secToken>%token%</secToken></DajLinie></soap:Body></soap:Envelope>"
				);
		HTTPClient.callSOAP(BASE_URL + "PublicService.asmx", 
				"http://PublicService/DajUlice",
				"<?xml version='1.0' encoding='utf-8'?><soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'><soap:Body><DajUlice xmlns='http://PublicService/'><secToken>%token%</secToken></DajUlice></soap:Body></soap:Envelope>"
				);
		updateStops();
	}
}
