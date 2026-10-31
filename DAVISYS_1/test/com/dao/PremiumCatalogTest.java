package com.dao;

import com.entity.SetupProduct;
import com.entity.SetupFilterProduct;
import com.service.SetupBuilderService;
import com.service.SetupProductMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.imageio.ImageIO;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exercises the additive catalog with the actual parser, mapper and combo algorithm. No SQL connection. */
public class PremiumCatalogTest {
    private List<String[]> rows() throws Exception {
        var lines = Files.readAllLines(Path.of("../database/demo/premium/products.tsv"), StandardCharsets.UTF_8);
        List<String[]> rows = new ArrayList<>();
        for (int i=1;i<lines.size();i++) rows.add(lines.get(i).split("\t",-1));
        return rows;
    }
    @Test public void tenRealModelRowsHaveImagesAndParseableMetadata() throws Exception {
        List<String[]> rows = rows(); assertEquals(10,rows.size());
        Map<String,Integer> categories = new TreeMap<>(); Set<String> codes = new HashSet<>();
        for(int i=0;i<rows.size();i++) {
            String[] c=rows.get(i); assertEquals(10,c.length);
            assertEquals(String.format("SB%04d",61+i),c[0]); assertTrue(codes.add(c[0]));
            assertTrue(c[1].length()<=50); assertTrue(c[9].length()<=2000);
            assertTrue(Set.of("ASUS","APPLE","LGE","HP").contains(c[3]));
            categories.merge(c[2],1,Integer::sum);
            assertTrue(Long.parseLong(c[6])>1_000_000L); assertTrue(Integer.parseInt(c[4])>0);
            var picture=ImageIO.read(Path.of("src/com/images",c[8]+".PNG").toFile());
            assertNotNull(c[0],picture); assertTrue(picture.getWidth()>=150 && picture.getHeight()>=150);
            for(String purpose:List.of("Gaming","Học tập","Văn phòng","Thiết kế đồ họa","Làm việc đa nhiệm")) {
                int score=SetupFilterDAO.purposeScore(c[9],purpose); assertTrue(c[0]+purpose,score>0 && score<=100);
            }
            assertTrue(SetupFilterDAO.hasStyle(c[9],"Hiện đại"));
            if(c[3].equals("APPLE")) assertTrue(SetupFilterDAO.purposeScore(c[9],"Gaming")<SetupFilterDAO.MIN_PURPOSE_SCORE);
        }
        assertEquals(Map.of("LAP",4,"MH",3,"PC",3),categories);
    }
    private List<SetupProduct> products(String purpose, String style) throws Exception {
        List<SetupProduct> products=new ArrayList<>();
        for(String[] c:rows()) {
            int score=SetupFilterDAO.purposeScore(c[9],purpose);
            if(score<SetupFilterDAO.MIN_PURPOSE_SCORE || !SetupFilterDAO.hasStyle(c[9],style))continue;
            products.add(SetupProductMapper.map(new SetupFilterProduct(c[0],c[1],c[2],c[2],
                    Double.parseDouble(c[6]),Integer.parseInt(c[4]),purpose,score,"Điểm demo",c[8],style,"Phong cách demo",c[9])));
        }
        return products;
    }
    @Test public void gamingLaptopMonitorHasAlternativesAtSixtyMillion() throws Exception {
        var combos=new SetupBuilderService().generateCombos(products("Gaming","Hiện đại"),60_000_000L,List.of("LAP","MH"));
        assertTrue(combos.size()>=2);
        for(var combo:combos){assertTrue(combo.getTongTien()<=60_000_000L);assertEquals(2,combo.getProducts().size());}
    }
    @Test public void largeBudgetDesktopCombosAndInsufficientBudgetUseRealAlgorithm() throws Exception {
        var service=new SetupBuilderService(); var products=products("Thiết kế đồ họa","Hiện đại");
        var combos=service.generateCombos(products,80_000_000L,List.of("PC","MH"));
        assertTrue(combos.size()>=2);
        for(var combo:combos)assertTrue(combo.getTongTien()<=80_000_000L);
        assertTrue(service.generateCombos(products,10_000_000L,List.of("PC","MH")).isEmpty());
    }
}
