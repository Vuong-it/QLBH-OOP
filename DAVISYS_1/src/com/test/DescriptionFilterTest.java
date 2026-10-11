package com.test;

import com.dao.SetupFilterDAO;

/** Run File: kiem tra cach doc mo ta, khong can ket noi database. */
public final class DescriptionFilterTest {
    public static void main(String[] args) {
        String text = "Đặc điểm: không phù hợp Gaming; thiết kế sang trọng. "
                + "Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 0; Học tập: 85; "
                + "Văn phòng: 90; Thiết kế đồ họa: 60; Làm việc đa nhiệm: 75. "
                + "Phong cách demo: Hiện đại; Tối giản. "
                + "Điểm và phong cách do nhóm quy định; cần đối chiếu thông số thực tế.";
        check(SetupFilterDAO.purposeScore(text, "Gaming") == 0);
        check(SetupFilterDAO.purposeScore(text, "Học tập") == 85);
        check(SetupFilterDAO.purposeScore(text, "Văn phòng") == 90);
        check(SetupFilterDAO.purposeScore(text, "Thiết kế đồ họa") == 60);
        check(SetupFilterDAO.purposeScore(text, "Làm việc đa nhiệm") == 75);
        check(SetupFilterDAO.hasStyle(text, "Hiện đại"));
        check(SetupFilterDAO.hasStyle(text, "Tối giản"));
        check(!SetupFilterDAO.hasStyle(text, "Sang trọng"));
        check(!SetupFilterDAO.hasStyle(text, "Năng động"));
        check(SetupFilterDAO.purposeScore(text.replace("Gaming: 0", "Gaming: 95"), "Gaming") == 95);
        check(SetupFilterDAO.purposeScore(text.replace("Gaming: 0", "Gaming: 101"), "Gaming") == 0);
        check(SetupFilterDAO.purposeScore(text.replace("Gaming: 0", "Gaming: -5"), "Gaming") == 0);
        check(SetupFilterDAO.purposeScore(text.replace("Gaming: 0", "Gaming: 90; Gaming: 80"), "Gaming") == 0);
        check(!SetupFilterDAO.hasStyle(text.replace("Hiện đại; Tối giản", "Không Hiện đại"), "Hiện đại"));
        check(SetupFilterDAO.purposeScore("Gaming hiện đại", "Gaming") == 0);
        check(!SetupFilterDAO.hasStyle("Gaming hiện đại", "Hiện đại"));
        check(SetupFilterDAO.purposeScore(null, "Gaming") == 0);
        check(!SetupFilterDAO.hasStyle(null, "Hiện đại"));
        check(SetupFilterDAO.purposeScore(text.toLowerCase(), "HOC_TAP") == 85);
        System.out.println("PASS: loc tu MOTA, 5 muc dich, 4 phong cach, diem 0, nhan phu dinh va mo ta sai dinh dang.");
    }

    private static void check(boolean ok) {
        if (!ok) throw new AssertionError("Doc mo ta khong dung.");
    }
}
