package com.dhruvil.mp4url.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Random;
import java.text.SimpleDateFormat;

@RestController
public class Mp4UrlController {
    @GetMapping("/analyze")
    public Mono<ResponseEntity<String>> analyzeMP4(@RequestParam("url") String urlParam) {
        // Validate the URL parameter
        if (!isValidUrl(urlParam)) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid URL"));
        }

        // Create URL object from the given URL parameter
        URL url;
        try {
            url = new URL(urlParam);
        } catch (IOException e) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid URL"));
        }

        // Generate a unique file name with date/time and random number
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String random = String.format("%04d", new Random().nextInt(10000));
        String tempFilePath = "/tmp/temp_" + timestamp + random + ".mp4";

        // Download the MP4 file to the temporary location on disk asynchronously
        return downloadFile(url, tempFilePath)
                .flatMap(filePath -> analyzeMP4File(filePath)
                        .map(analysisResult -> ResponseEntity.ok(analysisResult))
                        .defaultIfEmpty(ResponseEntity.ok("No analysis result found")))
                .onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().body(ex.getMessage())))
                .doFinally(signalType -> cleanUpFile(tempFilePath));
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private Mono<String> downloadFile(URL url, String filePath) {
        return Mono.fromCallable(() -> {
            Files.copy(url.openStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
            return filePath;
        });
    }

    private void cleanUpFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            // Handle the exception as per system's grace
            e.printStackTrace();
        }
    }

       //  Check if the file has the MP4 file signature (first 8 bytes)
//        byte[] signatureBytes = new byte[8];
//        is.read(signatureBytes);
//        String signature = new String(signatureBytes);
//
//        if (!signature.equals(".mp4")) {
//            is.close();
//            throw new IllegalArgumentException("The provided file is not an MP4 file");
//        }

        private Mono<String> analyzeMP4File(String filePath) {
            return Mono.fromCallable(() -> {
                ObjectMapper mapper = new ObjectMapper();
                File file = new File(filePath);
                try (InputStream is = new FileInputStream(file)) {
                    is.skip(8);

                    ArrayNode boxes = mapper.createArrayNode();
                    readBoxes(is, boxes);

                    return mapper.writeValueAsString(boxes);
                } catch (IOException e) {
                    throw new RuntimeException("Error analyzing MP4 file: " + e.getMessage());
                }
            });
        }

    private void readBoxes(InputStream is, ArrayNode parentBox) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        while (is.available() > 0) {
            // Read the box size (4 bytes)
            byte[] sizeBytes = new byte[4];
            is.read(sizeBytes);
            int size = ByteBuffer.wrap(sizeBytes).getInt();

            // Read the box type (4 bytes)
            byte[] typeBytes = new byte[4];
            is.read(typeBytes);
            String type = new String(typeBytes);

            // Create a new object to represent this box
            ObjectNode box = mapper.createObjectNode();
            box.put("type", type);
            box.put("size", size);

            if (type.equals("moof") || type.equals("mfhd") || type.equals("traf") || type.equals("tfhd")
                    || type.equals("trun") || type.equals("uuid") || type.equals("mdat")) {
                System.out.println("Type: " + type + ", Size: " + size);
            }

            if (type.equals("moof") || type.equals("traf")) {
                // This box contains other boxes, so recurse into it
                ArrayNode subBoxes = mapper.createArrayNode();
                readBoxes(is, subBoxes);
                box.set("boxes", subBoxes);
            } else {
                // This box contains payload, so skip over it
                is.skip(size - 8);
            }

            // Add the box to the parent box's list of sub-boxes
            parentBox.add(box);
        }

    }
}



