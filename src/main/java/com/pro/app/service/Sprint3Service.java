package com.pro.app.service;

import com.pro.app.component.FileUtils;
import com.pro.app.domain.DatasourceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.*;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.net.URL;


@Service
public class Sprint3Service {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    @Autowired
    private DatasourceProperties datasourceProperties;



    public String loadDownwardApiFile(String path)  {

        String allContents = "";

        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            List<Path> fileList = paths.filter(Files::isRegularFile).collect(Collectors.toList());
            for (Path file : fileList) {
                allContents += "<b>File: " + file  +"</b><br>";
                List<String> fileContent = Files.readAllLines(file);
                for (String line : fileContent) {
                    allContents += line + "<br>" ;
                }
                allContents += "---<br>";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return allContents;
    }

    public String getSelfPodKubeApiServer(String podName, String tokenPath) {

        FileUtils fileUtils = new FileUtils();
        String NAMESPACE = fileUtils.readFile(tokenPath + "namespace");
        String API_URL = "https://kubernetes.default/api/v1/namespace/"+NAMESPACE+"/pods/"+podName;
        String TOKEN = fileUtils.readFile(tokenPath + "token");
        String CA_CRT = fileUtils.readFile(tokenPath + "ca.crt");
        String responseString = "";
        log.info("NAMESPACE: " +NAMESPACE);
        log.info("API_URL: " +API_URL);
        log.info("TOKEN: " +TOKEN);
        log.info("CA_CRT: " +CA_CRT);

        try {

            // URL 객체 생성
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 요청 메소드 설정
            conn.setRequestMethod("GET");
            // 토큰을 사용하여 인증 헤더 추가
            conn.setRequestProperty("Authorization", "Bearer " + TOKEN);

            // 설정된 CA 인증서 파일을 사용하여 SSL 연결 설정
            System.setProperty("javax.net.ssl.trustStore", CA_CRT);

            // 응답 코드 가져오기
            int responseCode = conn.getResponseCode();
            log.info("Response Code : " + responseCode);

            // 버퍼 리더를 사용하여 응답 내용 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 응답 출력
            responseString = response.toString();
            log.info(responseString);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseString;
    }



}