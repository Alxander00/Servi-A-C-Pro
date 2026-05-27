package com.climatizacion.sistema_clima.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public double[] geocode(String direccion) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?format=json&q=" +
                    java.net.URLEncoder.encode(direccion, "UTF-8") + "&limit=1";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(response);
            if (root.isArray() && root.size() > 0) {
                double lat = root.get(0).get("lat").asDouble();
                double lon = root.get(0).get("lon").asDouble();
                return new double[]{lat, lon};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}