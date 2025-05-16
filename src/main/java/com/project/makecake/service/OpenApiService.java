package com.project.makecake.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.project.makecake.domain.product.Cake;
import com.project.makecake.domain.store.OpenTime;
import com.project.makecake.domain.store.Store;
import com.project.makecake.domain.store.StoreUrl;
import com.project.makecake.repository.CakeRepository;
import com.project.makecake.repository.OpenTimeRepository;
import com.project.makecake.repository.StoreRepository;
import com.project.makecake.repository.StoreUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class OpenApiService {

    private final CakeRepository cakeRepository;
    private final StoreUrlRepository storeUrlRepository;
    private final StoreRepository storeRepository;
    private final OpenTimeRepository openTimeRepository;

    // (초기 데이터 쌓기) naver 지도 api 요청으로 레터링 케이크 매장 정보 저장 메소드
    @Transactional
    public void collectStoreData(int storeNo) throws IOException {

        String urlString = "https://map.naver.com/v5/api/sites/summary/" + storeNo + "?lang=ko";

        JsonElement element = getOpenApiResult(urlString);

        long id = element.getAsJsonObject().get("id").getAsLong();
        String name = element.getAsJsonObject().get("name").getAsString();
        float x = element.getAsJsonObject().get("x").getAsFloat();
        float y = element.getAsJsonObject().get("y").getAsFloat();
        String fullAddress = element.getAsJsonObject().get("fullAddress").getAsString();
        String fullRoadAddress = element.getAsJsonObject().get("fullRoadAddress").getAsString();
        String phone = element.getAsJsonObject().get("phone").getAsString();
        String description = element.getAsJsonObject().get("description").getAsString();

        String mainImg = "";
        if(element.getAsJsonObject().get("imageURL")!= null){
            mainImg = element.getAsJsonObject().get("imageURL").getAsString();
        }

        String openTimeString = "";
        if(!element.getAsJsonObject().get("bizhourInfo").isJsonNull()){
            openTimeString = element.getAsJsonObject().get("bizhourInfo").getAsString();
        }

        Store store = new Store();
        store.setNaverId(id);
        store.setName(name);
        store.setX(x);
        store.setY(y);
        store.setFullAddress(fullAddress);
        store.setRoadAddress(fullRoadAddress);
        store.setMainImg(mainImg);
        store.setDescription(description);
        store.setPhone(phone);
        store.setOpenTimeString(openTimeString);

        storeRepository.save(store);

        JsonArray arr = element.getAsJsonObject().get("urlList").getAsJsonArray();
        for (int i=0; i<arr.size();i++) {
            String type = arr.get(i).getAsJsonObject().get("type").getAsString();
            String addUrl = arr.get(i).getAsJsonObject().get("url").getAsString();

            StoreUrl storeUrl = new StoreUrl();
            storeUrl.setUrl(addUrl);
            storeUrl.setType(type);
            storeUrl.setStore(store);

            storeUrlRepository.save(storeUrl);
        }

        // 네이버 지도 검색 결과 중 업체 사진 가져오기
        JsonArray images = element.getAsJsonObject().get("images").getAsJsonArray();

        for(int i = 0; i < images.size(); i++){
            String imgUrl = images.get(i).getAsJsonObject().get("url").getAsString();
            Cake cake = new Cake(imgUrl,store,null);
            cakeRepository.save(cake);
        }

        JsonArray bizhourList = null;
        if(!element.getAsJsonObject().get("bizHour").isJsonNull()) {
            bizhourList = element.getAsJsonObject().get("bizHour").getAsJsonArray();

            for(int i=0; i < bizhourList.size(); i++){
                String type = bizhourList.get(i).getAsJsonObject().get("type").getAsString();
                String startTime = bizhourList.get(i).getAsJsonObject().get("startTime").getAsString();
                String endTime = bizhourList.get(i).getAsJsonObject().get("endTime").getAsString();
                String descriptionTime = bizhourList.get(i).getAsJsonObject().get("description").getAsString();
                Boolean isDayOff = bizhourList.get(i).getAsJsonObject().get("isDayOff").getAsBoolean();

                OpenTime openTime = new OpenTime();
                openTime.setType(type);
                openTime.setStartTime(startTime);
                openTime.setEndTime(endTime);
                openTime.setDescriptionTime(descriptionTime);
                openTime.setIsDayOff(isDayOff);
                openTime.setStore(store);

                openTimeRepository.save(openTime);
            }
        }
    }

    // naver 지도 api 요청 JSON 반환 메소드
    public JsonElement getOpenApiResult(String urlString) throws IOException {

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);

        // 요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = "";
        String result = "";

        while ((line = br.readLine()) != null) {
            result += line;
        }
        br.close();

        // Gson 라이브러리로 JSON파싱
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(result);
        return element;
    }
}