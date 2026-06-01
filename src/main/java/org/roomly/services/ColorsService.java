package org.roomly.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.roomly.utils.ColorsUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class ColorsService {

    @Value("${colors.storage.filename}")
    private String colorsCatalogFilename;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() throws IOException {
        Map<String, String> parsedHexToColor = new HashMap<>();
        Map<String, String> parsedColorToHex = new HashMap<>();

        ClassPathResource resource = new ClassPathResource("assets/colors/" + colorsCatalogFilename);
        if (!resource.exists()) {
            log.warn("Colors catalog not found on classpath: assets/colors/{}", colorsCatalogFilename);
            ColorsUtil.initialize(parsedHexToColor, parsedColorToHex);
            return;
        }

        try {
            log.info("Loading colors catalog from classpath: assets/colors/{}", colorsCatalogFilename);
            String jsonContent = new String(resource.getInputStream().readAllBytes());
            Object parsed = new JSONParser(jsonContent).parse();

            if (!(parsed instanceof List)) {
                log.error("Colors catalog JSON is not an array: {}", colorsCatalogFilename);
                ColorsUtil.initialize(parsedHexToColor, parsedColorToHex);
                return;
            }

            List<Object> colorsList = (List<Object>) parsed;
            for (Object object : colorsList) {
                if (!(object instanceof Map<?, ?> map)) {
                    continue;
                }
                String hex = ColorsUtil.normalizeHex((String) map.get("hex"));
                String colorName = ColorsUtil.normalizeColorName((String) map.get("name"));
                if (hex != null && colorName != null) {
                    parsedHexToColor.put(hex, colorName);
                    parsedColorToHex.put(colorName, hex);
                }
            }
            log.info("Loaded {} colors into catalog", parsedHexToColor.size());
        } catch (Exception e) {
            throw new IOException("Failed to load colors catalog", e);
        } finally {
            ColorsUtil.initialize(parsedHexToColor, parsedColorToHex);
        }
    }
}
