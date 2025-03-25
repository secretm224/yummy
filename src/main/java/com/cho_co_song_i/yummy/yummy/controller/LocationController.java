package com.cho_co_song_i.yummy.yummy.controller;

import com.cho_co_song_i.yummy.yummy.dto.LocationCityDto;
import com.cho_co_song_i.yummy.yummy.dto.LocationCountyDto;
import com.cho_co_song_i.yummy.yummy.dto.LocationDistrictDto;
import com.cho_co_song_i.yummy.yummy.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/location")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) { this.locationService = locationService; }

    @GetMapping("/county")
    public ResponseEntity<List<LocationCountyDto>> getAllLocationCounty() {
        List<LocationCountyDto> locationCounties = locationService.getAllLocationCounty();
        return ResponseEntity.ok(locationCounties);
    }

    @GetMapping("/city")
    public ResponseEntity<List<LocationCityDto>> getLocationCities(
            @RequestParam(value = "countySeq", required = false) Long locationCountyCode
    ) {
        List<LocationCityDto> locationCities = locationService.getLocationCities(locationCountyCode);
        return ResponseEntity.ok(locationCities);
    }

    @GetMapping("/district")
    public ResponseEntity<List<LocationDistrictDto>> getLocationDistricts(
            @RequestParam(value = "citySeq", required = false) Long locationCityCode
    ) {
        List<LocationDistrictDto> locationDistricts = locationService.getLocationDistrict(locationCityCode);
        return ResponseEntity.ok(locationDistricts);
    }
}
