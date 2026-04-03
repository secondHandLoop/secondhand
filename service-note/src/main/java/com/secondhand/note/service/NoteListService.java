package com.secondhand.note.service;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.jdbc.JdbcQueryBuilder;
import com.secondhand.common.output.IDataToMap;
import com.secondhand.common.param.Param;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.note.entity.Note;
import com.secondhand.note.entity.TopCategory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.secondhand.common.jdbc.JdbcQueryBuilder.LIKE;
import static com.secondhand.common.jdbc.JdbcQueryBuilder.Sort.DESC;

@Document(desc = "帖子列表", module = "note", name = "帖子列表", author = "Jeffrey", platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS}, creatAt = "2026-02-16",
        request = {
                @ApiParam(name = "page", value = "页数", dataType = "Integer"),
                @ApiParam(name = "limit", value = "一页条数", dataType = "Integer"),
                @ApiParam(name = "keyword", value = "关键词", dataType = "String")
        },
        response = {
                @ApiParam(name = "id", value = "帖子id", dataType = "String"),
                @ApiParam(name = "userId", value = "用户id", dataType = "String"),
                @ApiParam(name = "username", value = "用户名", dataType = "String"),
                @ApiParam(name = "title", value = "标题", dataType = "String"),
                @ApiParam(name = "description", value = "描述", dataType = "String"),
                @ApiParam(name = "photos", value = "图片", dataType = "List<String>"),
                @ApiParam(name = "state", value = "州", dataType = "String"),
                @ApiParam(name = "city", value = "城市", dataType = "String"),
                @ApiParam(name = "address", value = "地址", dataType = "String"),
                @ApiParam(name = "zipcode", value = "邮编", dataType = "String"),
                @ApiParam(name = "latitude", value = "纬度", dataType = "Double"),
                @ApiParam(name = "longitude", value = "经度", dataType = "Double"),
                @ApiParam(name = "price", value = "价格", dataType = "String"),
                @ApiParam(name = "topCategory", value = "一级类", dataType = "enum"),
                @ApiParam(name = "delivery", value = "是否运输", dataType = "boolean"),
                @ApiParam(name = "updateAt", value = "更新时间", dataType = "long")
        }
)
@Component("noteListService1.0")
public class NoteListService extends ReflectApiService {

    // ─── Explore pool: how many recent notes to score before paginating ───
    private static final int EXPLORE_POOL_SIZE = 300;

    // ─── Recency half-life: score = 0.5 at this age ───
    private static final double RECENCY_HALF_LIFE_SECONDS = 7 * 86400.0;  // 7 days

    // ─── Proximity half-life: score = 0.5 at this distance ───
    private static final double PROXIMITY_HALF_LIFE_KM = 20.0;

    public Map<String, Object> service(
            @Param(value = "1",  name = "page",    required = false) Integer page,
            @Param(value = "10", name = "limit",   required = false) Integer limit,
            Long currentUserId,
            String keyword,

            // optional filters
            TopCategory topCategory,
            Boolean delivery,
            @Param(value = "false", name = "myNotes",  required = false) Boolean myNotes,

            // explore ranking mode
            @Param(value = "false", name = "explore",  required = false) Boolean explore,
            List<TopCategory> interestCategories,

            // nearby sort
            @Param(value = "false", name = "nearby",   required = false) Boolean nearby,

            // user location
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
            return success(Collections.emptyList());
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

        return success(result, (IDataToMap<Note>) (map, source) -> {
            map.put("id",            source.getId());
            map.put("userId",        source.getUserId());
            map.put("username",      source.getUsername());
            map.put("title",         source.getTitle());
            map.put("description",   source.getDescription());
            map.put("photos",        source.getPhotoList());
            map.put("state",         source.getState());
            map.put("city",          source.getCity());
            map.put("address",       source.getAddress());
            map.put("zipcode",       source.getZipcode());
            map.put("latitude",      source.getLatitude());
            map.put("longitude",     source.getLongitude());
            map.put("price",         source.getPrice());
            map.put("topCategory",   source.getTopCategory());
            map.put("noteCondition", source.getNoteCondition());
            map.put("delivery",      source.isDelivery());
            map.put("updateAt",      source.getUpdateAt());
            map.put("noteStatus",    source.getNoteStatus());
        });
    }

    // ── Explore scoring ───────────────────────────────────────────────────────

    /**
     * Combined relevance score in [0, 1].
     *
     * With location:    35% proximity + 30% recency + 20% interest + 15% quality
     * Without location: 50% recency  + 30% interest + 20% quality
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

    private JdbcQueryBuilder buildMyNoteQuery(Long userId, boolean count) {
        String sql = count
                ? "select count(1) total from note where status = 'ACTIVE' and user_id = " + userId
                : "select * from note where status = 'ACTIVE' and user_id = " + userId;
        return JdbcQueryBuilder.build().select(sql);
    }

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

    private int compareByCityState(Note a, Note b, String userCity, String userState, String userZipcode) {
        int tierA = geographicTier(a, userCity, userState, userZipcode);
        int tierB = geographicTier(b, userCity, userState, userZipcode);
        if (tierA != tierB) return Integer.compare(tierA, tierB);
        return Long.compare(b.getUpdateAt(), a.getUpdateAt());
    }

    private int geographicTier(Note note, String userCity, String userState, String userZipcode) {
        if (StringUtils.isNotBlank(userZipcode) && userZipcode.equals(note.getZipcode())) return 0;
        if (StringUtils.isNotBlank(userCity) && userCity.equalsIgnoreCase(note.getCity())) return 0;
        if (StringUtils.isNotBlank(userState) && userState.equalsIgnoreCase(note.getState())) return 1;
        return 2;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                  * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2.0 * Math.asin(Math.sqrt(a));
    }


    private int calcBuffer(int page, int limit) {
        int buffer = page * limit * 5;
        if (buffer < limit * 5) buffer = limit * 5;
        return Math.min(buffer, 500);
    }

    private <T> List<T> slicePage(List<T> list, int page, int limit) {
        int from = (page - 1) * limit;
        if (from >= list.size()) return Collections.emptyList();
        return list.subList(from, Math.min(from + limit, list.size()));
    }
}
