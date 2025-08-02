package com.example.farm4u.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class GeocodingService {

    @Value("${kakao.restApi.key}")
    private String KAKAO_API_KEY;

    private final String apiUrl = "https://dapi.kakao.com/v2/local/search/address.json?query=";

    public Double[] convertAddressToLatLng(String address) {
        // 1. URI encode
        String url = apiUrl + URLEncoder.encode(address, StandardCharsets.UTF_8);
        System.out.println("[DEBUG] Kakao Geocoding 요청: " + url);

        // 2. HttpHeaders 생성
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + KAKAO_API_KEY);
//        System.out.println("[DEBUG] Authorization 헤더:" + headers.get("Authorization"));

        // 3. GET 요청
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = new RestTemplate().exchange(
                url, HttpMethod.GET, entity, String.class);
        System.out.println("[DEBUG] Kakao Geocoding 응답: " + response.getBody());

        // 4. 결과 파싱 (좌표 추출)
        JSONObject root;
        try {
            root = new JSONObject(response.getBody());
            JSONArray documents = root.optJSONArray("documents");
            if (documents.isEmpty()) {
//                throw new IllegalArgumentException("주소 결과가 없습니다.");
                return new Double[]{null, null};
            }

            JSONObject first = documents.getJSONObject(0);
            double lng = first.getDouble("x");
            double lat = first.getDouble("y");

            return new Double[]{lat, lng};

        } catch (JSONException e) {
            System.out.println("[예외 발생]: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
