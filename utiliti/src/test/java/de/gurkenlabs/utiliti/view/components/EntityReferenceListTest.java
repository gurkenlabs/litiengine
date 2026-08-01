package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.gurkenlabs.litiengine.test.SwingTestSuite;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class EntityReferenceListTest {

  @Test
  void resolvesStoredIdsToReadableEntityLabelsWithoutChangingTheirOrder() {
    EntityReferenceList list = new EntityReferenceList(
        "Targets",
        () -> List.of(reference(4, "Gate", "PROP"), reference(7, null, "CREATURE")));

    list.setJoinedString("4,7,4");

    assertEquals(
        List.of("Gate [#4]", "#7 [#7]", "Gate [#4]"),
        list.getDisplayValuesForTest());
    assertEquals("4,7,4", list.getJoinedString());
  }

  @Test
  void preservesMissingLegacyReferencesUntilTheUserRemovesThem() {
    EntityReferenceList list = new EntityReferenceList("Activators", List::of);

    list.setJoinedString("999");

    assertEquals(List.of("Missing entity (#999)"), list.getDisplayValuesForTest());
    assertEquals("999", list.getJoinedString());
    assertNull(list.getReferenceIconForTest(0));
  }

  @Test
  void rendersTheSelectedEntityTypeIconInTheGrid() {
    EntityReferenceList list = new EntityReferenceList("Targets", () -> List.of(reference(4, "Gate", "PROP")));

    list.setJoinedString("4");

    assertNotNull(list.getReferenceIconForTest(0));
  }

  @Test
  void addsAndRemovesEntityReferencesByTheirStoredIds() {
    EntityReferenceList list = new EntityReferenceList("Targets", List::of);
    list.addReferenceForTest(reference(3, "Switch", "PROP"));
    list.addReferenceForTest(reference(5, "Hero", "CREATURE"));
    list.addReferenceForTest(reference(3, "Switch", "PROP"));

    list.removeReferencesForTest(1);

    assertEquals("3,3", list.getJoinedString());
  }

  private static EntityReferenceList.EntityReference reference(int id, String name, String type) {
    return new EntityReferenceList.EntityReference(id, name, type, false);
  }
}
