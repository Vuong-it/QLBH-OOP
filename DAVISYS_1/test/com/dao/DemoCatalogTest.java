package com.dao;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Validates the shipped demo catalog using the production metadata parser. */
public class DemoCatalogTest {
    @Test public void allSixtyProductsHaveImagesCategoriesAndCompleteDemoMetadata() throws Exception {
        Path root = Path.of("..");
        var rows = Files.readAllLines(root.resolve("database/demo/products.tsv"), StandardCharsets.UTF_8);
        assertEquals(61, rows.size());
        assertEquals("MASP\tTENSP\tMALH\tMAHANG\tSOLUONG\tGIANHAP\tGIABAN\tNGAYNHAP\tHINH\tMOTA", rows.get(0));
        Set<String> codes = new HashSet<>(); Map<String,Integer> groups = new TreeMap<>();
        int unknown = 0, outOfStock = 0;
        String[] purposes={"Gaming","Học tập","Văn phòng","Thiết kế đồ họa","Làm việc đa nhiệm"};
        String[] styles={"Hiện đại","Tối giản","Năng động","Sang trọng"};
        for (int i=1;i<rows.size();i++) {
            String[] c=rows.get(i).split("\t",-1); assertEquals(10,c.length);
            assertEquals(String.format("SB%04d",i),c[0]); assertTrue(codes.add(c[0]));
            groups.merge(c[2],1,Integer::sum); if(c[3].equals("DEMO")) unknown++;
            int stock=Integer.parseInt(c[4]); assertTrue(stock>=0); if(stock==0) outOfStock++;
            assertTrue(Long.parseLong(c[5])>0); assertTrue(Long.parseLong(c[6])>0);
            java.time.LocalDate.parse(c[7]); assertEquals(c[0],c[8]);
            assertTrue(Files.isRegularFile(Path.of("src/com/images",c[8]+".PNG")));
            assertTrue(c[9].contains("Nhu cầu demo")); assertTrue(c[9].contains("Điểm và phong cách do nhóm quy định"));
            assertFalse(c[9].contains("\t"));
            for(String purpose:purposes) {
                var m=java.util.regex.Pattern.compile(java.util.regex.Pattern.quote(purpose)+": ([0-9]{1,3})(?=[;.])").matcher(c[9]);
                assertTrue(c[0]+" missing purpose "+purpose,m.find());
                int score=Integer.parseInt(m.group(1)); assertFalse(m.find()); assertTrue(score>=0 && score<=100);
                assertEquals(c[0]+" parser mismatch",score,SetupFilterDAO.purposeScore(c[9],purpose));
            }
            int styleCount=0; for(String style:styles) if(SetupFilterDAO.hasStyle(c[9],style))styleCount++;
            assertTrue(c[0]+" has no parseable style",styleCount>0);
            if(c[0].equals("SB0004")){assertFalse(c[1].contains("TKL"));assertFalse(c[9].contains("TKL"));}
            if(c[0].equals("SB0012")){assertEquals("TN",c[2]);assertEquals("LG",c[3]);assertTrue(c[9].contains("3,5 mm"));assertFalse(c[9].contains("USB-A"));}
        }
        assertEquals(12,groups.size()); for(int count:groups.values())assertEquals(5,count);
        assertEquals(30,unknown); assertEquals(3,outOfStock);
    }
}
