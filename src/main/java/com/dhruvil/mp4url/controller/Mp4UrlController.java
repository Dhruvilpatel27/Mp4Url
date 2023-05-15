package com.dhruvil.mp4url.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Random;
import java.text.SimpleDateFormat;

public class Mp4UrlController {
    @GetMapping("/analyze")
    public ResponseEntity<String> analyzeMP4(@RequestParam("url") String urlParam) {
        try {
            // Validate the URL parameter
            if (!isValidUrl(urlParam)) {
                return ResponseEntity.badRequest().body("Invalid URL");
            }

            // Create URL object from the given URL parameter
            URL url = new URL(urlParam);

            // Generate a unique file name with date/time and random number
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String random = String.format("%04d", new Random().nextInt(10000));
            String tempFilePath = "/tmp/temp_" + timestamp + random + ".mp4";

            // Download the MP4 file to the temporary location on disk
            Files.copy(url.openStream(), Paths.get(tempFilePath), StandardCopyOption.REPLACE_EXISTING);

            // Analyze the MP4 file and return the results
            String analysisResult = analyzeMP4File(tempFilePath);
            return ResponseEntity.ok(analysisResult);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }


}


