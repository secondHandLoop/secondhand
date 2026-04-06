package com.secondhand.note.service;

import com.secondhand.common.jdbc.JdbcQueryBuilder;
import com.secondhand.note.dto.NoteResponseMapper;
import com.secondhand.note.entity.Note;
import com.secondhand.note.entity.TopCategory;
import com.secondhand.note.openapi.schema.NoteListItemResponse;
import com.secondhand.note.openapi.schema.NoteListResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.secondhand.common.jdbc.JdbcQueryBuilder.LIKE;
import static com.secondhand.common.jdbc.JdbcQueryBuilder.Sort.DESC;

/**
 * Builds the note list returned to app clients.
 *
 * This service supports three feed behaviors:
 * 1. default feed ordered by most recent updates
 * 2. explore feed scored by recency, quality, interests, and distance
 * 3. nearby feed ranked by GPS distance or by matching city/state metadata
 */
@Service
public class NoteListService {

    // ─── Explore pool: how many recent notes to score before paginating ───
    private static final int EXPLORE_POOL_SIZE = 300;

    // ─── Recency half-life: score = 0.5 at this age ───
    private static final double RECENCY_HALF_LIFE_SECONDS = 7 * 86400.0;  // 7 days

    // ─── Proximity half-life: score = 0.5 at this distance ───
    private static final double PROXIMITY_HALF_LIFE_KM = 20.0;

    /**
     * Returns a page of notes visible to the current user.
     *
     * The request mode is derived from the incoming flags:
     * - {@code myNotes}: only the current user's active notes
     * - {@code explore}: personalized ranking without explicit search filters
     * - {@code nearby}: geographic ranking using coordinates or location strings
     * - otherwise: normal recency-sorted listing
     */
    public NoteListResponse listNotes(
            Integer page,
            Integer limit,
            Long currentUserId,
            String keyword,
            TopCategory topCategory,
            Boolean delivery,
            Boolean myNotes,
            Boolean explore,
            List<TopCategory> interestCategories,
            Boolean nearby,
            Double userLat,
            Double userLng,
            String userCity,
            String userState,
            String userZipcode
    ) {
        NoteListResponse response = new NoteListResponse();
        response.setStatus(200);
        response.setMessage("success");
        response.setBody(listNoteItems(page, limit, currentUserId, keyword, topCategory, delivery, myNotes, explore,
                interestCategories, nearby, userLat, userLng, userCity, userState, userZipcode));
        return response;
    }

    private List<NoteListItemResponse> listNoteItems(
            Integer page,
            Integer limit,
            Long currentUserId,
            String keyword,
            TopCategory topCategory,
            Boolean delivery,
            Boolean myNotes,
            Boolean explore,
            List<TopCategory> interestCategories,
            Boolean nearby,
            Double userLat,
            Double userLng,
            String userCity,
            String userState,
            String userZipcode
    ) {
        page  = (page  == null || page  < 1) ? 1  : page;
        limit = (limit == null || limit < 1) ? 10 : limit;

        boolean isMyNotes = Boolean.TRUE.equals(myNotes) && currentUserId != null;
        boolean isExplore = Boolean.TRUE.equals(explore)
                && !isMyNotes
                && StringUtils.isBlank(keyword)
                && topCategory == null
                && delivery == null
                && !Boolean.TRUE.equals(nearby);

        // ── COUNT (always from full visible set) ──────────────────────────────
        JdbcQueryBuilder countQ = isMyNotes
                ? buildMyNoteQuery(currentUserId, true)
                : buildVisibleNoteQuery(currentUserId, true);
        if (!isExplore) {
            applyFilters(countQ, keyword, topCategory, delivery);
        }

        Long total = countQ.total();
        if (total == null || total == 0) {
            return Collections.emptyList();
        }

        // ── LIST ──────────────────────────────────────────────────────────────
        List<Note> result;

        if (isExplore) {
            // ── EXPLORE: fetch a large pool, score each note, sort, then page ──
            JdbcQueryBuilder listQ = buildVisibleNoteQuery(currentUserId, false);
            List<Note> pool = listQ
                    .orderBy("update_at", DESC)
                    .limit(1, EXPLORE_POOL_SIZE)
                    .find(Note.class);

            boolean hasLatLng = (userLat != null && userLng != null);
            long nowSeconds   = System.currentTimeMillis() / 1000L;
            Set<TopCategory> interests = interestCategories != null
                    ? new HashSet<>(interestCategories)
                    : Collections.emptySet();

            pool.sort((a, b) -> Double.compare(
                    computeExploreScore(b, userLat, userLng, hasLatLng, interests, nowSeconds),
                    computeExploreScore(a, userLat, userLng, hasLatLng, interests, nowSeconds)
            ));

            result = slicePage(pool, page, limit);

        } else if (Boolean.TRUE.equals(nearby)) {
            // ── NEARBY: tiered city/state sort (GPS falls back to haversine) ──
            boolean hasLatLng  = (userLat != null && userLng != null);
            boolean hasCityState = StringUtils.isNotBlank(userCity) || StringUtils.isNotBlank(userState) || StringUtils.isNotBlank(userZipcode);

            JdbcQueryBuilder listQ = isMyNotes
                    ? buildMyNoteQuery(currentUserId, false)
                    : buildVisibleNoteQuery(currentUserId, false);
            applyFilters(listQ, keyword, topCategory, delivery);

            int buffer = calcBuffer(page, limit);
            List<Note> bufferNotes = listQ
                    .orderBy("update_at", DESC)
                    .limit(1, buffer)
                    .find(Note.class);

            if (hasLatLng) {
                bufferNotes.sort((a, b) -> compareByLatLng(a, b, userLat, userLng));
            } else if (hasCityState) {
                bufferNotes.sort((a, b) -> compareByCityState(a, b, userCity, userState, userZipcode));
            }

            result = slicePage(bufferNotes, page, limit);

        } else {
            // ── DEFAULT: recency ──────────────────────────────────────────────
            JdbcQueryBuilder listQ = isMyNotes
                    ? buildMyNoteQuery(currentUserId, false)
                    : buildVisibleNoteQuery(currentUserId, false);
            applyFilters(listQ, keyword, topCategory, delivery);

            result = listQ
                    .orderBy("update_at", DESC)
                    .limit(page, limit)
                    .find(Note.class);
        }

        List<NoteListItemResponse> items = new ArrayList<>(result.size());
        for (Note note : result) {
            items.add(NoteResponseMapper.toNoteListItem(note));
        }
        return items;
    }

    // ── Explore scoring ───────────────────────────────────────────────────────

    /**
     * Scores a note for the explore feed.
     *
     * With location data the score blends proximity, recency, interests, and
     * content quality. Without location data the proximity weight is shifted to
     * recency so notes can still be ranked predictably.
     */
    private double computeExploreScore(
            Note note,
            Double userLat, Double userLng,
            boolean hasLatLng,
            Set<TopCategory> interests,
            long nowSeconds
    ) {
        // 1. Recency (exponential decay, half-life = 7 days)
        double ageSeconds   = Math.max(0, nowSeconds - note.getUpdateAt());
        double recencyScore = Math.exp(-ageSeconds * Math.log(2) / RECENCY_HALF_LIFE_SECONDS);

        // 2. Quality (photos + description length)
        double qualityScore = 0.0;
        List<String> photos = note.getPhotoList();
        if (photos != null && !photos.isEmpty()) qualityScore += 0.7;
        if (note.getDescription() != null && note.getDescription().length() > 50) qualityScore += 0.3;

        // 3. Interest match (user's top categories from their history/collections)
        double interestScore = 0.0;
        if (!interests.isEmpty() && note.getTopCategory() != null) {
            interestScore = interests.contains(note.getTopCategory()) ? 1.0 : 0.0;
        }

        if (!hasLatLng || note.getLatitude() == null || note.getLongitude() == null) {
            // No location data — redistribute proximity weight to recency
            return 0.50 * recencyScore + 0.30 * interestScore + 0.20 * qualityScore;
        }

        // 4. Proximity (exponential decay, half-life = 20 km)
        double distKm        = haversineKm(userLat, userLng, note.getLatitude(), note.getLongitude());
        double proximityScore = Math.exp(-distKm * Math.log(2) / PROXIMITY_HALF_LIFE_KM);

        return 0.35 * proximityScore + 0.30 * recencyScore + 0.20 * interestScore + 0.15 * qualityScore;
    }

    // ── Query builders ────────────────────────────────────────────────────────

    /**
     * Creates the base query for the current user's own active notes.
     */
    private JdbcQueryBuilder buildMyNoteQuery(Long userId, boolean count) {
        String sql = count
                ? "select count(1) total from note where status = 'ACTIVE' and user_id = " + userId
                : "select * from note where status = 'ACTIVE' and user_id = " + userId;
        return JdbcQueryBuilder.build().select(sql);
    }

    /**
     * Creates the base query for notes visible in shared feeds.
     *
     * Published active notes are returned, and when a user is logged in their
     * own notes are excluded so feed recommendations do not echo their posts.
     */
    private JdbcQueryBuilder buildVisibleNoteQuery(Long currentUserId, boolean count) {
        String sql;
        if (currentUserId == null) {
            sql = count
                    ? "select count(1) total from note where status = 'ACTIVE' and note_status = 'PUBLISHED'"
                    : "select * from note where status = 'ACTIVE' and note_status = 'PUBLISHED'";
        } else {
            // Exclude the logged-in user's own notes from Explore, Local, and Search feeds
            sql = count
                    ? "select count(1) total from note where status = 'ACTIVE' and note_status = 'PUBLISHED' and user_id != " + currentUserId
                    : "select * from note where status = 'ACTIVE' and note_status = 'PUBLISHED' and user_id != " + currentUserId;
        }
        return JdbcQueryBuilder.build().select(sql);
    }

    /**
     * Applies optional search filters shared by the default and nearby feeds.
     */
    private void applyFilters(JdbcQueryBuilder q, String keyword, TopCategory topCategory, Boolean delivery) {
        if (StringUtils.isNotBlank(keyword)) {
            List<String> keys   = Arrays.asList("LOWER(username)", "LOWER(title)", "LOWER(description)");
            List<Object> values = new ArrayList<>();
            List<String> ops    = new ArrayList<>();
            String lowerKeyword = keyword.toLowerCase();
            values.addAll(Collections.nCopies(keys.size(), lowerKeyword));
            ops.addAll(Collections.nCopies(keys.size(), LIKE));
            q.or(keys, values, ops);
        }
        if (topCategory != null) q.eq("top_category", topCategory.name());
        if (delivery     != null) q.eq("delivery", delivery);
    }

    // ── Nearby comparators ────────────────────────────────────────────────────

    /**
     * Orders notes by coordinate distance and falls back to update time.
     *
     * Notes with coordinates are always preferred over notes without them.
     */
    private int compareByLatLng(Note a, Note b, double userLat, double userLng) {
        boolean aHas = (a.getLatitude() != null && a.getLongitude() != null);
        boolean bHas = (b.getLatitude() != null && b.getLongitude() != null);
        if (aHas && !bHas) return -1;
        if (!aHas && bHas) return 1;
        if (aHas) {
            double da = haversineKm(userLat, userLng, a.getLatitude(), a.getLongitude());
            double db = haversineKm(userLat, userLng, b.getLatitude(), b.getLongitude());
            if (da != db) return Double.compare(da, db);
        }
        return Long.compare(b.getUpdateAt(), a.getUpdateAt());
    }

    /**
     * Orders notes by coarse geographic match when exact coordinates are unavailable.
     */
    private int compareByCityState(Note a, Note b, String userCity, String userState, String userZipcode) {
        int tierA = geographicTier(a, userCity, userState, userZipcode);
        int tierB = geographicTier(b, userCity, userState, userZipcode);
        if (tierA != tierB) return Integer.compare(tierA, tierB);
        return Long.compare(b.getUpdateAt(), a.getUpdateAt());
    }

    /**
     * Assigns a location tier used by nearby sorting.
     *
     * Tier 0 is zipcode or city match, tier 1 is state match, and tier 2 is
     * everything else.
     */
    private int geographicTier(Note note, String userCity, String userState, String userZipcode) {
        if (StringUtils.isNotBlank(userZipcode) && userZipcode.equals(note.getZipcode())) return 0;
        if (StringUtils.isNotBlank(userCity) && userCity.equalsIgnoreCase(note.getCity())) return 0;
        if (StringUtils.isNotBlank(userState) && userState.equalsIgnoreCase(note.getState())) return 1;
        return 2;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    /**
     * Calculates the great-circle distance between two coordinates in kilometers.
     */
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                  * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2.0 * Math.asin(Math.sqrt(a));
    }

    /**
     * Expands the candidate pool for nearby sorting so reordering still has
     * enough items to produce a stable page after distance-based ranking.
     */
    private int calcBuffer(int page, int limit) {
        int buffer = page * limit * 5;
        if (buffer < limit * 5) buffer = limit * 5;
        return Math.min(buffer, 500);
    }

    /**
     * Returns the requested page from an already materialized and sorted list.
     */
    private <T> List<T> slicePage(List<T> list, int page, int limit) {
        int from = (page - 1) * limit;
        if (from >= list.size()) return Collections.emptyList();
        return list.subList(from, Math.min(from + limit, list.size()));
    }
}
