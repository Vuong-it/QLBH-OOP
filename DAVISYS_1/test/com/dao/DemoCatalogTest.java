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
        int unknown = 0, randomAssigned = 0, outOfStock = 0;
        Set<String> existingBrands=Set.of("B01","B02","B03","B04","B05","B06","B07","B08","B09","B10","B11","B12","B13","B14","B16","B17","B18","B19","B20","B21","B22","B23","B24","B25","B26","LG");
        Map<String,String> assignments=Map.ofEntries(Map.entry("SB0003","B21"),Map.entry("SB0004","B23"),Map.entry("SB0005","B16"),Map.entry("SB0007","B16"),Map.entry("SB0008","B08"),Map.entry("SB0011","B21"),Map.entry("SB0014","B24"),Map.entry("SB0015","B22"),Map.entry("SB0018","B02"),Map.entry("SB0019","B21"),Map.entry("SB0020","B14"),Map.entry("SB0021","B01"),Map.entry("SB0022","B07"),Map.entry("SB0023","B24"),Map.entry("SB0024","B24"),Map.entry("SB0025","B10"),Map.entry("SB0027","B17"),Map.entry("SB0028","B09"),Map.entry("SB0029","B02"),Map.entry("SB0030","B10"),Map.entry("SB0031","B12"),Map.entry("SB0037","B18"),Map.entry("SB0043","B08"),Map.entry("SB0050","B09"),Map.entry("SB0051","B04"),Map.entry("SB0052","B10"),Map.entry("SB0053","B20"),Map.entry("SB0054","B23"),Map.entry("SB0055","B23"),Map.entry("SB0056","LG"));
        String[] purposes={"Gaming","Học tập","Văn phòng","Thiết kế đồ họa","Làm việc đa nhiệm"};
        String[] styles={"Hiện đại","Tối giản","Năng động","Sang trọng"};
        for (int i=1;i<rows.size();i++) {
            String[] c=rows.get(i).split("\t",-1); assertEquals(10,c.length);
            assertEquals(String.format("SB%04d",i),c[0]); assertTrue(codes.add(c[0]));
            groups.merge(c[2],1,Integer::sum); if(c[3].equals("DEMO")) unknown++;
            assertTrue(existingBrands.contains(c[3]));
            if(assignments.containsKey(c[0])){randomAssigned++;assertEquals(assignments.get(c[0]),c[3]);}
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
        assertEquals(0,unknown); assertEquals(30,randomAssigned); assertEquals(3,outOfStock);
    }
}
