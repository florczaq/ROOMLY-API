package org.roomly.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.roomly.utils.AvatarsUtil;
import org.roomly.utils.ColorsUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service for loading and caching avatar image files from disk.
 * <p>
 * On startup it reads a JSON catalog file to discover available avatar names,
 * which are then registered in {@link AvatarsUtil} for validation. Individual
 * avatar images are keyed by name + color and cached to avoid repeated disk I/O.
 * </p>
 */
@Slf4j
@Service
public class AvatarService {
    private final String storagePath;
    private final String[] supportedExtensions = new String[]{"png", "jpg"};
    private final String catalogName;

    public AvatarService (
      @Value("${avatar.storage.path}") String storagePath,
      @Value("${avatar.storage.filename}") String catalogName
    ) throws IOException {
        this.storagePath = storagePath;
        this.catalogName = catalogName;
        AvatarsUtil.initialize(getAvailableAvatarsFromCatalog());
    }

    /**
     * Reads the JSON catalog file and returns the list of available avatar names.
     * Returns an empty list if the catalog file does not exist.
     *
     * @return list of avatar name strings extracted from the catalog
     * @throws IOException if the catalog file exists but cannot be read or parsed
     */
    private List<String> getAvailableAvatarsFromCatalog () throws IOException {
        Path catalogPath = Path.of(storagePath, catalogName);
        log.info("Reading avatar catalog from: {}", catalogPath);
        
        try {
            if (!Files.exists(catalogPath)) {
                log.warn("Avatar catalog file not found at: {}", catalogPath);
                return List.of();
            }
            
            String jsonContent = Files.readString(catalogPath);
            ObjectMapper mapper = new ObjectMapper();
            
            // Parse as array of objects with "name" field
            List<Map<String, Object>> avatarObjects = mapper.readValue(
              jsonContent,
              mapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            
            // Extract the "name" field from each object
            return avatarObjects.stream()
              .map(obj -> (String) obj.get("name"))
              .filter(Objects::nonNull)
              .toList();
            
        } catch (IOException e) {
            log.error("Failed to read avatar catalog from: {}", catalogPath, e);
            throw new IOException("Failed to read avatar catalog", e);
        }
    }
    
    /**
     * Loads the raw bytes of an avatar image from disk, resolved by name and color.
     * <p>
     * If {@code color} is a hex code (contains {@code #}), it is first translated to
     * a color name via {@link ColorsUtil}. The file is looked up as
     * {@code <NAME>_<COLOR>.<ext>} trying {@code .png} before {@code .jpg}.
     * Results are cached by the uppercased {@code name_color} key.
     * </p>
     *
     * @param name  avatar name (case-insensitive)
     * @param color color name or hex code (case-insensitive)
     * @return raw image bytes
     * @throws IllegalArgumentException if {@code color} is an unrecognized hex code
     * @throws FileNotFoundException    if no matching avatar file exists on disk
     * @throws IOException              if the file exists but cannot be read
     */
    @Cacheable(value = "avatars", key = "#name.toUpperCase() + '_' + #color.toUpperCase()")
    public byte[] loadAvatarFromStorage (String name, String color) throws IOException {
        if (color.contains("#")) {
            color = ColorsUtil.getColorByHex(color);
            if (color == null) {
                throw new IllegalArgumentException("Invalid color hex code");
            }
        }
        String normalizedColor = color.toUpperCase();
        String baseName = (name + "_" + normalizedColor).toUpperCase();
        
        for (String ext : supportedExtensions) {
            Path filePath = Path.of(storagePath, baseName + "." + ext);
            log.info("Loading avatar from file: {}, {}", filePath, filePath.toFile().exists());
            if (filePath.toFile().exists()) {
                try {
                    return Files.readAllBytes(filePath);
                } catch (IOException e) {
                    throw new IOException("Failed to load avatar file: " + filePath, e);
                }
            }
        }
        throw new FileNotFoundException("Avatar file not found for: " + baseName);
    }
    
}
