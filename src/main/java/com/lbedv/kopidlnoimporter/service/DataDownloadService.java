package com.lbedv.kopidlnoimporter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class DataDownloadService {

    private static final Logger log = LoggerFactory.getLogger(DataDownloadService.class);

    private final String downloadUrl;
    private final HttpClient httpClient;

    public DataDownloadService(@Value("${ruian.url:https://www.smartform.cz/download/kopidlno.xml.zip}") String downloadUrl) {
        this.downloadUrl = downloadUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public InputStream downloadData() throws IOException, InterruptedException {
        log.info("Downloading RUIAN data from URL: {}", downloadUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() >= 400) {
            throw new IOException("Failed to download data, HTTP status: " + response.statusCode());
        }

        return response.body();
    }
}