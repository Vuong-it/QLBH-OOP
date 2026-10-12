package com.dao;
import org.junit.Test;
import static org.junit.Assert.*;

public class DescriptionFilterTest {
    public static final String DESCRIPTION = "Đặc điểm: không phù hợp Gaming; thiết kế sang trọng. "
            + "Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 0; Học tập: 85; "
            + "Văn phòng: 90; Thiết kế đồ họa: 60; Làm việc đa nhiệm: 75. "
            + "Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.";
    @Test public void originalDatabaseBranchDescriptionScenario() {
        assertEquals(0, SetupFilterDAO.purposeScore(DESCRIPTION, "Gaming"));
        assertEquals(85, SetupFilterDAO.purposeScore(DESCRIPTION, "Học tập"));
        assertEquals(90, SetupFilterDAO.purposeScore(DESCRIPTION, "Văn phòng"));
        assertEquals(60, SetupFilterDAO.purposeScore(DESCRIPTION, "Thiết kế đồ họa"));
        assertEquals(75, SetupFilterDAO.purposeScore(DESCRIPTION, "Làm việc đa nhiệm"));
        assertTrue(SetupFilterDAO.hasStyle(DESCRIPTION, "Hiện đại"));
        assertTrue(SetupFilterDAO.hasStyle(DESCRIPTION, "Tối giản"));
        assertFalse(SetupFilterDAO.hasStyle(DESCRIPTION, "Sang trọng"));
        assertFalse(SetupFilterDAO.hasStyle(DESCRIPTION, "Năng động"));
        assertEquals(85, SetupFilterDAO.purposeScore(DESCRIPTION.toLowerCase(), "HOC_TAP"));
    }
    @Test public void malformedDuplicateOrMissingMetadataIsNeverGuessed() {
        assertEquals(95, SetupFilterDAO.purposeScore(DESCRIPTION.replace("Gaming: 0", "Gaming: 95"), "Gaming"));
        for (String token : new String[]{"Gaming: 101", "Gaming: -5", "Gaming: 90; Gaming: 80", "Gaming: 90; Gaming: -5"})
            assertEquals(0, SetupFilterDAO.purposeScore(DESCRIPTION.replace("Gaming: 0", token), "Gaming"));
        assertFalse(SetupFilterDAO.hasStyle(DESCRIPTION.replace("Hiện đại; Tối giản", "Không Hiện đại"), "Hiện đại"));
        for (String text : new String[]{null, "", "Gaming hiện đại"}) {
            assertEquals(0, SetupFilterDAO.purposeScore(text, "Gaming")); assertFalse(SetupFilterDAO.hasStyle(text, "Hiện đại"));
        }
    }
    @Test public void ambiguousRepeatedMetadataSectionsAreRejected() {
        assertEquals(0, SetupFilterDAO.purposeScore(DESCRIPTION + " " + DESCRIPTION, "Học tập"));
        assertFalse(SetupFilterDAO.hasStyle(DESCRIPTION + " " + DESCRIPTION, "Hiện đại"));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsUnknownPurpose() { SetupFilterDAO.normalizePurpose("unknown"); }
    @Test(expected=IllegalArgumentException.class) public void rejectsUnknownStyle() { SetupFilterDAO.normalizeStyle("unknown"); }
}
