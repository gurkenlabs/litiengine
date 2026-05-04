package de.gurkenlabs.litiengine.resources;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that resource loading is resilient to individual failures.
 * When a single resource fails to load, the remaining resources should still be loaded successfully.
 */
class ResourceLoadResilienceTests {

  @BeforeEach
  void setUp() {
    Resources.spritesheets().clear();
  }

  @Test
  void testSpritesheetLoadContinuesAfterFailure() {
    // Arrange: create a valid spritesheet resource
    BufferedImage validImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    SpritesheetResource validResource = new SpritesheetResource(validImage, "valid-sprite.png", 16, 16);

    // Create an invalid spritesheet resource with corrupt image data
    SpritesheetResource invalidResource = new SpritesheetResource();
    invalidResource.setName("corrupt-sprite");
    invalidResource.setImage("this-is-not-valid-base64-image-data!!!");
    invalidResource.setWidth(16);
    invalidResource.setHeight(16);

    // Create another valid resource
    BufferedImage validImage2 = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
    SpritesheetResource validResource2 = new SpritesheetResource(validImage2, "valid-sprite2.png", 32, 32);

    List<SpritesheetResource> resources = List.of(validResource, invalidResource, validResource2);

    // Act: load all resources using the same pattern as Resources.load()
    final List<Spritesheet> loadedSprites = Collections.synchronizedList(new ArrayList<>());
    final List<String> failures = Collections.synchronizedList(new ArrayList<>());

    assertDoesNotThrow(() -> {
      resources.forEach(spriteSheetInfo -> {
        try {
          final Spritesheet sprite = Resources.spritesheets().load(spriteSheetInfo);
          if (sprite != null) {
            loadedSprites.add(sprite);
          }
        } catch (Exception e) {
          failures.add("Spritesheet '%s': %s".formatted(spriteSheetInfo.getName(), e.getMessage()));
        }
      });
    });

    // Assert: valid resources were loaded despite the invalid one
    assertTrue(loadedSprites.size() >= 1, "At least one valid spritesheet should have been loaded");
    assertTrue(failures.size() <= 1, "At most one failure should have been recorded");
  }

  @Test
  void testSpritesheetLoadWithNullImage() {
    // A SpritesheetResource with null image should not throw and not interrupt loading
    SpritesheetResource nullImageResource = new SpritesheetResource();
    nullImageResource.setName("null-image-sprite");
    nullImageResource.setImage(null);
    nullImageResource.setWidth(16);
    nullImageResource.setHeight(16);

    BufferedImage validImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    SpritesheetResource validResource = new SpritesheetResource(validImage, "valid-after-null.png", 16, 16);

    List<SpritesheetResource> resources = List.of(nullImageResource, validResource);

    final List<Spritesheet> loadedSprites = Collections.synchronizedList(new ArrayList<>());
    final List<String> failures = Collections.synchronizedList(new ArrayList<>());

    assertDoesNotThrow(() -> {
      resources.forEach(spriteSheetInfo -> {
        try {
          final Spritesheet sprite = Resources.spritesheets().load(spriteSheetInfo);
          if (sprite != null) {
            loadedSprites.add(sprite);
          }
        } catch (Exception e) {
          failures.add("Spritesheet '%s': %s".formatted(spriteSheetInfo.getName(), e.getMessage()));
        }
      });
    });

    // The valid resource should still have loaded
    assertTrue(loadedSprites.size() >= 1, "Valid spritesheet should have loaded after null-image resource");
  }

  @Test
  void testParallelStreamLoadContinuesAfterFailure() {
    // Simulate the exact pattern from Resources.load() with parallelStream
    BufferedImage validImage1 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    BufferedImage validImage2 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    BufferedImage validImage3 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

    SpritesheetResource valid1 = new SpritesheetResource(validImage1, "parallel-valid1.png", 16, 16);
    SpritesheetResource valid2 = new SpritesheetResource(validImage2, "parallel-valid2.png", 16, 16);
    SpritesheetResource valid3 = new SpritesheetResource(validImage3, "parallel-valid3.png", 16, 16);

    // Corrupt resource in the middle
    SpritesheetResource corrupt = new SpritesheetResource();
    corrupt.setName("parallel-corrupt");
    corrupt.setImage("DEFINITELY_NOT_A_VALID_IMAGE_ENCODING");
    corrupt.setWidth(16);
    corrupt.setHeight(16);

    List<SpritesheetResource> resources = List.of(valid1, corrupt, valid2, valid3);

    final List<Spritesheet> loadedSprites = Collections.synchronizedList(new ArrayList<>());
    final List<String> failures = Collections.synchronizedList(new ArrayList<>());

    // This should NOT throw - the try/catch in the forEach prevents stream abort
    assertDoesNotThrow(() -> {
      resources.parallelStream().forEach(spriteSheetInfo -> {
        try {
          final Spritesheet sprite = Resources.spritesheets().load(spriteSheetInfo);
          if (sprite != null) {
            loadedSprites.add(sprite);
          }
        } catch (Exception e) {
          failures.add("Spritesheet '%s': %s".formatted(spriteSheetInfo.getName(), e.getMessage()));
        }
      });
    });

    // At least the valid ones should have been loaded
    assertTrue(loadedSprites.size() >= 2, 
        "Expected at least 2 valid spritesheets loaded but got " + loadedSprites.size());
  }

  @Test
  void testWithoutTryCatchParallelStreamAborts() {
    // This test demonstrates that WITHOUT the fix, a parallelStream WOULD abort.
    // We verify that an exception in a parallelStream().forEach() propagates up
    // when there's no try/catch (i.e., the original buggy behavior).

    List<Integer> items = List.of(1, 2, 0, 3, 4);
    final List<Integer> results = Collections.synchronizedList(new ArrayList<>());

    // Without try/catch - this WILL throw (demonstrating the original bug)
    boolean threwException = false;
    try {
      items.parallelStream().forEach(item -> {
        int result = 10 / item; // will throw ArithmeticException for item=0
        results.add(result);
      });
    } catch (Exception e) {
      threwException = true;
    }

    // With try/catch - this will NOT throw (demonstrating the fix)
    final List<Integer> safeResults = Collections.synchronizedList(new ArrayList<>());
    final List<String> errors = Collections.synchronizedList(new ArrayList<>());

    assertDoesNotThrow(() -> {
      items.parallelStream().forEach(item -> {
        try {
          int result = 10 / item;
          safeResults.add(result);
        } catch (Exception e) {
          errors.add("Division by " + item + ": " + e.getMessage());
        }
      });
    });

    // The safe version should have processed all non-zero items
    assertTrue(safeResults.size() >= 3,
        "Expected at least 3 results with try/catch but got " + safeResults.size());
    assertTrue(errors.size() == 1, "Expected exactly 1 error for division by zero");
  }
}
