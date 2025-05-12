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
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/location")
public class LocationController {
    private final LocationService locationService;

    /* N+1 문제를 테스트하기 위한 컨트롤러 */
//    @GetMapping("/test")
//    public ResponseEntity<String> getTest(
//            @RequestParam(value = "countySeq", required = false) Long locationCountyCode
//    ) {
//        locationService.getTestDtos(locationCountyCode);
//        return ResponseEntity.ok("test");
//    }

    /**
     *
     * @return
     * @throws Exception
     */
    @GetMapping("/county")
    public ResponseEntity<List<LocationCountyDto>> findAllLocationCounty() throws Exception {
        List<LocationCountyDto> locationCounties = locationService.findAllLocationCounty();
        return ResponseEntity.ok(locationCounties);
    }

    /**
     *
     * @param locationCountyCode
     * @return
     * @throws Exception
     */
    @GetMapping("/city")
    public ResponseEntity<List<LocationCityDto>> findLocationCities(
            @RequestParam(value = "countySeq", required = false) Long locationCountyCode
    ) throws Exception {
        List<LocationCityDto> locationCities = locationService.findLocationCities(locationCountyCode);
        return ResponseEntity.ok(locationCities);
    }

    /**
     *
     * @param locationCityCode
     * @return
     * @throws Exception
     */
    @GetMapping("/district")
    public ResponseEntity<List<LocationDistrictDto>> findLocationDistricts(
            @RequestParam(value = "citySeq", required = false) Long locationCityCode
    ) throws Exception {
        List<LocationDistrictDto> locationDistricts = locationService.findLocationDistrict(locationCityCode);
        return ResponseEntity.ok(locationDistricts);
    }
}
