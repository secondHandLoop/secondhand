package com.secondhand.common.geo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

        LatLng ll;
        try {
            ll = new LatLng(
                    Double.parseDouble(results[0].getLat()),
                    Double.parseDouble(results[0].getLon())
            );
        } catch (NullPointerException | NumberFormatException ex) {
            return null; // malformed coordinates — treat as a miss
        }

        cache.put(q, ll);
        return ll;
    }

    /**
     * Returns up to 5 US address suggestions for a partial query — the server-side
     * proxy for typeahead so the browser never calls Nominatim directly (keeps the
     * app within Nominatim's usage policy and lets us cache/throttle centrally).
     */
    public List<AddressSuggestion> autocomplete(String query) {
        if (StringUtils.isBlank(query) || query.trim().length() < 3) {
            return List.of();
        }
        NominatimResult[] results;
        try {
            results = createRestClient().get()
                    .uri(buildAutocompleteUri(query.trim()))
                    .retrieve()
                    .body(NominatimResult[].class);
        } catch (RuntimeException ex) {
            return List.of();
        }
        if (results == null) {
            return List.of();
        }
        List<AddressSuggestion> out = new ArrayList<>(results.length);
        for (NominatimResult r : results) {
            AddressSuggestion s = new AddressSuggestion();
            NominatimAddress a = r.getAddress();
            if (a != null) {
                s.setCity(firstNonBlank(a.getCity(), a.getTown(), a.getVillage(), a.getCounty()));
                s.setState(a.getState());
                s.setZipcode(a.getPostcode());
                s.setAddress(joinNonBlank(a.getHouseNumber(), a.getRoad()));
            }
            s.setDisplayName(r.getDisplayName());
            out.add(s);
        }
        return out;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (StringUtils.isNotBlank(v)) return v;
        }
        return "";
    }

    private static String joinNonBlank(String a, String b) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(a)) sb.append(a);
        if (StringUtils.isNotBlank(b)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(b);
        }
        return sb.toString();
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
        // Finite timeouts so a slow/hung Nominatim response can't stall the caller —
        // important because address geocoding runs inside a DB transaction.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(4000);
        RestClient.Builder builder = RestClient.builder()
                .requestFactory(factory)
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

    private String buildAutocompleteUri(String query) {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        StringBuilder uri = new StringBuilder(
                "/search?format=json&addressdetails=1&limit=5&countrycodes=us&q=").append(encoded);
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
        @JsonProperty("display_name")
        private String displayName;
        private NominatimAddress address;

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

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public NominatimAddress getAddress() {
            return address;
        }

        public void setAddress(NominatimAddress address) {
            this.address = address;
        }
    }

    /** The addressdetails=1 sub-object from a Nominatim result. */
    public static class NominatimAddress {
        @JsonProperty("house_number")
        private String houseNumber;
        private String road;
        private String city;
        private String town;
        private String village;
        private String county;
        private String state;
        private String postcode;

        public String getHouseNumber() { return houseNumber; }
        public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }
        public String getRoad() { return road; }
        public void setRoad(String road) { this.road = road; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getTown() { return town; }
        public void setTown(String town) { this.town = town; }
        public String getVillage() { return village; }
        public void setVillage(String village) { this.village = village; }
        public String getCounty() { return county; }
        public void setCounty(String county) { this.county = county; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getPostcode() { return postcode; }
        public void setPostcode(String postcode) { this.postcode = postcode; }
    }

    /** A structured address suggestion returned to the client typeahead. */
    public static class AddressSuggestion {
        private String address;
        private String city;
        private String state;
        private String zipcode;
        private String displayName;

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getZipcode() { return zipcode; }
        public void setZipcode(String zipcode) { this.zipcode = zipcode; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }
}
