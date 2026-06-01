package org.roomly.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.roomly.utils.AvatarsUtil;
import org.roomly.utils.ColorsUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class AvatarService {
    private final String[] supportedExtensions = new String[]{"png", "jpg"};
    private final String catalogName;

    public AvatarService(
        @Value("${avatar.storage.filename}") String catalogName
    ) throws IOException {
        this.catalogName = catalogName;
        AvatarsUtil.initialize(getAvailableAvatarsFromCatalog());
    }

    private List<String> getAvailableAvatarsFromCatalog() throws IOException {
        ClassPathResource resource = new ClassPathResource("assets/avatars/" + catalogName);
        log.info("Reading avatar catalog from classpath: assets/avatars/{}", catalogName);

        if (!resource.exists()) {
            log.warn("Avatar catalog file not found on classpath: assets/avatars/{}", catalogName);
            return List.of();
        }

        try {
            String jsonContent = new String(resource.getInputStream().readAllBytes());
            ObjectMapper mapper = new ObjectMapper();

            List<Map<String, Object>> avatarObjects = mapper.readValue(
                jsonContent,
                mapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );

            return avatarObjects.stream()
                .map(obj -> (String) obj.get("name"))
                .filter(Objects::nonNull)
                .toList();

        } catch (IOException e) {
            log.error("Failed to read avatar catalog from classpath: assets/avatars/{}", catalogName, e);
            throw new IOException("Failed to read avatar catalog", e);
        }
    }

    @Cacheable(value = "avatars", key = "#name.toUpperCase() + '_' + #color.toUpperCase()")
    public byte[] loadAvatarFromStorage(String name, String color) throws IOException {
        if (color.contains("#")) {
            color = ColorsUtil.getColorByHex(color);
            if (color == null) {
                throw new IllegalArgumentException("Invalid color hex code");
            }
        }
        String normalizedColor = color.toUpperCase();
        String baseName = (name + "_" + normalizedColor).toUpperCase();

        for (String ext : supportedExtensions) {
            ClassPathResource resource = new ClassPathResource("assets/avatars/" + baseName + "." + ext);
            log.info("Loading avatar from classpath: assets/avatars/{}.{}, exists={}", baseName, ext, resource.exists());
            if (resource.exists()) {
                try {
                    return resource.getInputStream().readAllBytes();
                } catch (IOException e) {
                    throw new IOException("Failed to load avatar file: " + baseName + "." + ext, e);
                }
            }
        }
        throw new FileNotFoundException("Avatar file not found for: " + baseName);
    }
}
