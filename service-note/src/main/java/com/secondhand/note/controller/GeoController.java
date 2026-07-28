package com.secondhand.note.controller;

import com.secondhand.common.geo.GeocodingService;
import com.secondhand.common.output.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Server-side proxy for address typeahead. The client calls this instead of hitting
 * Nominatim directly, so the app stays within Nominatim's usage policy and requests
 * can be cached/throttled centrally.
 */
@RestController
@RequestMapping("/note/geo")
public class GeoController {

    @Resource
    private GeocodingService geocodingService;

    @Operation(summary = "Address autocomplete", description = "Returns up to 5 US address suggestions for a partial query.")
    @GetMapping("/autocomplete")
    public ApiResponse<List<GeocodingService.AddressSuggestion>> autocomplete(@RequestParam("q") String q) {
        return ApiResponse.success(geocodingService.autocomplete(q));
    }
}
