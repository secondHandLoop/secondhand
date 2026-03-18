package com.secondhand.common.geo;

import com.secondhand.common.tool.AssertUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeocodingService {

    // Spring Boot 3: RestClient (recommended). If you don't have it, tell me and I'll switch to RestTemplate.
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            .defaultHeader("User-Agent", "secondhand-app/1.0 (contact: your-email@example.com)")
            .build();

    // very simple in-memory cache to reduce calls (important for Nominatim policy)
    private final Map<String, LatLng> cache = new ConcurrentHashMap<>();

    public LatLng geocode(String state, String city, String address, String zipcode) {
        // build query text (best quality first)
        String q = buildQuery(state, city, address, zipcode);
        if (StringUtils.isBlank(q)) return null;

        LatLng cached = cache.get(q);
        if (cached != null) return cached;

        // Nominatim Search API (format=json, limit=1)
        // Docs: https://nominatim.org/release-docs/latest/api/Search/  :contentReference[oaicite:1]{index=1}
        String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8);

        NominatimResult[] results = restClient.get()
                .uri("/search?format=json&limit=1&q={q}", encoded)
                .retrieve()
                .body(NominatimResult[].class);

        if (results == null || results.length == 0) {
            return null;
        }

        LatLng ll = new LatLng(
                Double.parseDouble(results[0].lat()),
                Double.parseDouble(results[0].lon())
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

    public record LatLng(Double lat, Double lng) {}

    // minimal DTO for Nominatim response
    public record NominatimResult(String lat, String lon) {}
}
