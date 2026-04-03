package com.secondhand.common.geo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeocodingService {

    @Value("${geocoding.nominatim.base-url:https://nominatim.openstreetmap.org}")
    private String baseUrl;

    @Value("${geocoding.nominatim.user-agent:secondhand-app/1.0}")
    private String userAgent;

    @Value("${geocoding.nominatim.referer:}")
    private String referer;

    @Value("${geocoding.nominatim.contact-email:}")
    private String contactEmail;

    // very simple in-memory cache to reduce calls (important for Nominatim policy)
    private final Map<String, LatLng> cache = new ConcurrentHashMap<>();

    public LatLng geocode(String state, String city, String address, String zipcode) {
        // build query text (best quality first)
        String q = buildQuery(state, city, address, zipcode);
        if (StringUtils.isBlank(q)) return null;

        LatLng cached = cache.get(q);
        if (cached != null) return cached;

        NominatimResult[] results;
        try {
            results = createRestClient().get()
                    .uri(buildSearchUri(q))
                    .retrieve()
                    .body(NominatimResult[].class);
        } catch (RestClientResponseException ex) {
            return null;
        } catch (RuntimeException ex) {
            return null;
        }

        if (results == null || results.length == 0) {
            return null;
        }

        LatLng ll = new LatLng(
                Double.parseDouble(results[0].getLat()),
                Double.parseDouble(results[0].getLon())
        );

        cache.put(q, ll);
        return ll;
    }

    private String buildQuery(String state, String city, String address, String zipcode) {
        // Prefer full address; fallback to city/state/zip; fallback to zip only
        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotBlank(address)) sb.append(address).append(", ");
        if (StringUtils.isNotBlank(city)) sb.append(city).append(", ");
        if (StringUtils.isNotBlank(state)) sb.append(state).append(", ");
        if (StringUtils.isNotBlank(zipcode)) sb.append(zipcode);

        String q = sb.toString().trim();
        if (q.endsWith(",")) q = q.substring(0, q.length() - 1).trim();

        if (StringUtils.isBlank(q) && StringUtils.isNotBlank(zipcode)) {
            q = zipcode; // last fallback
        }
        return q;
    }

    private RestClient createRestClient() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", userAgent);
        if (StringUtils.isNotBlank(referer)) {
            builder.defaultHeader("Referer", referer);
        }
        return builder.build();
    }

    private String buildSearchUri(String query) {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        StringBuilder uri = new StringBuilder("/search?format=json&limit=1&q=").append(encoded);
        if (StringUtils.isNotBlank(contactEmail)) {
            uri.append("&email=")
                    .append(URLEncoder.encode(contactEmail, StandardCharsets.UTF_8));
        }
        return uri.toString();
    }

    public static class LatLng {
        private final Double lat;
        private final Double lng;

        public LatLng(Double lat, Double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public Double getLat() {
            return lat;
        }

        public Double getLng() {
            return lng;
        }
    }

    public static class NominatimResult {
        private String lat;
        private String lon;

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLon() {
            return lon;
        }

        public void setLon(String lon) {
            this.lon = lon;
        }
    }
}
