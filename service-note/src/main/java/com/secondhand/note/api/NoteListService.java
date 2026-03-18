package com.secondhand.note.api;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.entity.Status;
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

    public Map<String, Object> service(
            @Param(value = "1", name = "page", required = false) Integer page,
            @Param(value = "10", name = "limit", required = false) Integer limit,
            Long uid,
            String keyword,

            // optional filters
            TopCategory topCategory,
            Boolean delivery,

            // sort options
            @Param(value = "false", name = "nearby", required = false) Boolean nearby,

            // user location for nearby sort (preferred)
            Double userLat,
            Double userLng,

            // fallback location for nearby sort (if no lat/lng)
            String userCity,
            String userZipcode
    ) {

        // defaults
        page = (page == null || page < 1) ? 1 : page;
        limit = (limit == null || limit < 1) ? 10 : limit;
        nearby = Boolean.TRUE.equals(nearby);

        boolean noFilters =
                StringUtils.isBlank(keyword)
                        && topCategory == null
                        && delivery == null;

        // If user didn’t choose filters, default to nearby
        if (nearby == null) {
            nearby = noFilters;
        }

        // If nearby is true but user location is missing, fallback to newest
        boolean hasLatLng = (userLat != null && userLng != null);
        boolean hasCityZip = StringUtils.isNotBlank(userCity)
                || StringUtils.isNotBlank(userZipcode);

        if (Boolean.TRUE.equals(nearby) && !(hasLatLng || hasCityZip)) {
            nearby = false; // cannot do nearby without location
        }


        // -------- 1) Build COUNT query (total) --------
        JdbcQueryBuilder countQ = JdbcQueryBuilder
                .build(Note.class)
                .eq("status", Status.ACTIVE.name());

        applyFilters(countQ, keyword, topCategory, delivery);

        Long total = countQ.total();
        if (total == null || total == 0) {
            // if your success(...) supports total/page/limit, you can return those too
            return success(Collections.emptyList());
        }

        // -------- 2) Build LIST query --------
        List<Note> result;

        if (!nearby) {
            // Normal: DB order + DB pagination
            JdbcQueryBuilder listQ = JdbcQueryBuilder
                    .build(Note.class)
                    .eq("status", Status.ACTIVE.name());

            applyFilters(listQ, keyword, topCategory, delivery);

            result = listQ
                    .orderBy("update_at", DESC)
                    .limit(page, limit)
                    .find(Note.class);

        } else {
            // Nearby: buffer fetch -> sort in memory -> slice page
            int buffer = calcBuffer(page, limit); // protects performance

            JdbcQueryBuilder listQ = JdbcQueryBuilder
                    .build(Note.class)
                    .eq("status", Status.ACTIVE.name());

            applyFilters(listQ, keyword, topCategory, delivery);

            List<Note> bufferNotes = listQ
                    .orderBy("update_at", DESC)   // fetch newest first, then sort by distance
                    .limit(1, buffer)
                    .find(Note.class);

            // Choose comparator
            final boolean hasUserLatLng = (userLat != null && userLng != null);

            Collections.sort(bufferNotes, new Comparator<Note>() {
                @Override
                public int compare(Note a, Note b) {
                    if (hasUserLatLng) {
                        return compareByLatLng(a, b, userLat.doubleValue(), userLng.doubleValue());
                    }
                    return compareByCityZip(a, b, userCity, userZipcode);
                }
            });

            result = slicePage(bufferNotes, page, limit);
        }

        // -------- 3) Return mapped output (Banner style) --------
        // If you have success(list, total, page, limit, mapper) overload, use it (recommended).
        return success(result, (IDataToMap<Note>) (map, source) -> {
            map.put("id", source.getId());
            map.put("userId", source.getUserId());
            map.put("username", source.getUsername());
            map.put("title", source.getTitle());
            map.put("description", source.getDescription());
            map.put("photos", source.getPhotos());
            map.put("state", source.getState());
            map.put("city", source.getCity());
            map.put("address", source.getAddress());
            map.put("zipcode", source.getZipcode());
            map.put("latitude", source.getLatitude());
            map.put("longitude", source.getLongitude());
            map.put("price", source.getPrice());
            map.put("topCategory", source.getTopCategory());
            map.put("delivery", source.isDelivery());
            map.put("updateAt", source.getUpdateAt());
        });
    }

    private void applyFilters(JdbcQueryBuilder q, String keyword, TopCategory topCategory, Boolean delivery) {
        // keyword OR like over multiple columns
        if (StringUtils.isNotBlank(keyword)) {
            List<String> keys = Arrays.asList(
                    "username",
                    "title",
                    "description",
                    "state",
                    "city",
                    "address",
                    "zipcode"
            );

            List<Object> values = new ArrayList<Object>();
            List<String> ops = new ArrayList<String>();
            for (int i = 0; i < keys.size(); i++) {
                values.add(keyword);
                ops.add(LIKE);
            }
            q.or(keys, values, ops);
        }

        // topCategory filter
        if (topCategory != null) {
            q.eq("top_category", topCategory.name());
        }

        // delivery filter
        if (delivery != null) {
            q.eq("delivery", delivery);
        }
    }

    /**
     * Nearby comparator using (lat,lng). Notes without coords are ranked after those with coords.
     * Tie-breaker: updateAt desc.
     */
    private int compareByLatLng(Note a, Note b, double userLat, double userLng) {
        boolean aHas = (a.getLatitude() != null && a.getLongitude() != null);
        boolean bHas = (b.getLatitude() != null && b.getLongitude() != null);

        if (aHas && !bHas) return -1;
        if (!aHas && bHas) return 1;

        if (aHas && bHas) {
            double da = haversineKm(userLat, userLng, a.getLatitude().doubleValue(), a.getLongitude().doubleValue());
            double db = haversineKm(userLat, userLng, b.getLatitude().doubleValue(), b.getLongitude().doubleValue());
            int cmp = Double.compare(da, db);
            if (cmp != 0) return cmp;
        }

        return Long.compare(b.getUpdateAt(), a.getUpdateAt());
    }

    /**
     * Fallback nearby comparator using city match first, then zipcode numeric difference.
     * Tie-breaker: updateAt desc.
     */
    private int compareByCityZip(Note a, Note b, String userCity, String userZipcode) {
        boolean aCityMatch = StringUtils.isNotBlank(userCity) && userCity.equalsIgnoreCase(a.getCity());
        boolean bCityMatch = StringUtils.isNotBlank(userCity) && userCity.equalsIgnoreCase(b.getCity());

        if (aCityMatch && !bCityMatch) return -1;
        if (!aCityMatch && bCityMatch) return 1;

        Integer uz = safeParseInt(userZipcode);
        Integer az = safeParseInt(a.getZipcode());
        Integer bz = safeParseInt(b.getZipcode());

        if (uz != null && az != null && bz != null) {
            int da = Math.abs(az.intValue() - uz.intValue());
            int db = Math.abs(bz.intValue() - uz.intValue());
            if (da != db) return Integer.compare(da, db);
        }

        return Long.compare(b.getUpdateAt(), a.getUpdateAt());
    }

    private Integer safeParseInt(String s) {
        if (StringUtils.isBlank(s)) return null;
        try {
            return Integer.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2.0 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    private int calcBuffer(int page, int limit) {
        // Example: page=3,limit=10 => 150 buffer (3*10*5)
        int buffer = page * limit * 5;
        if (buffer < limit * 5) buffer = limit * 5;
        if (buffer > 500) buffer = 500; // cap
        return buffer;
    }

    private <T> List<T> slicePage(List<T> list, int page, int limit) {
        int from = (page - 1) * limit;
        if (from >= list.size()) return Collections.emptyList();
        int to = Math.min(from + limit, list.size());
        return list.subList(from, to);
    }
}
