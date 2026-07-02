package com.climatizacion.sistema_clima.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public GeocodingService() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getInterceptors().add((request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.set("User-Agent", "ServiACPro-Backend/1.0 (contacto@tudominio.com)");
            return execution.execute(request, body);
        });
    }

    public double[] geocode(String direccion) {
        if (direccion == null || direccion.trim().isEmpty()) {
            System.out.println("⚠️ Dirección vacía, no se puede geocodificar.");
            return null;
        }

        // 1. Intentar con la dirección completa
        double[] resultado = intentarGeocode(direccion.trim());
        if (resultado != null) {
            return resultado;
        }

        // 2. Fallback: intentar con una versión más simple (ciudad + país)
        String[] partes = direccion.split(",");
        if (partes.length >= 2) {
            // Tomar los últimos 2 o 3 elementos significativos
            String ciudad = partes[partes.length - 2].trim();
            String pais = partes[partes.length - 1].trim();
            String querySimple = ciudad + ", " + pais;
            System.out.println("⚠️ Intentando con búsqueda más simple: " + querySimple);
            return intentarGeocode(querySimple);
        }

        // 3. Último fallback: solo el país
        if (direccion.toLowerCase().contains("el salvador")) {
            System.out.println("⚠️ Intentando con búsqueda por país: El Salvador");
            return intentarGeocode("El Salvador");
        }

        System.out.println("❌ No se pudo geocodificar la dirección: " + direccion);
        return null;
    }

    private double[] intentarGeocode(String query) {
        try {
            // Usar countrycodes=sv para limitar a El Salvador
            String url = "https://nominatim.openstreetmap.org/search?format=json&q="
                    + java.net.URLEncoder.encode(query, "UTF-8")
                    + "&limit=1&addressdetails=1&countrycodes=sv";

            System.out.println("🌐 Consultando: " + url);
            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isEmpty()) {
                System.out.println("⚠️ Respuesta vacía para: " + query);
                return null;
            }

            JsonNode root = mapper.readTree(response);
            if (root.isArray() && root.size() > 0) {
                double lat = root.get(0).get("lat").asDouble();
                double lon = root.get(0).get("lon").asDouble();
                System.out.println("📍 Coordenadas encontradas: " + lat + ", " + lon);
                return new double[]{lat, lon};
            } else {
                System.out.println("⚠️ No se encontraron resultados para: " + query);
                System.out.println("📦 Respuesta: " + response);
            }
        } catch (Exception e) {
            System.err.println("❌ Error en geocodificación para '" + query + "': " + e.getMessage());
        }
        return null;
    }
}