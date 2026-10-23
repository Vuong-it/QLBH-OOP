package com.dao;

import com.dao.SetupCartDAO.CartContext;
import com.entity.SetupProduct;
import com.service.SetupBuilderFacade;
import com.service.SetupBuilderService;
import java.sql.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/** Opt-in, destructive fixtures only inside an explicitly named disposable review database. */
public class SetupSqlServerIntegrationTest {
    private static final String DESCRIPTION = "Đặc điểm: USB-A. Nhu cầu demo (điểm /100; 0 = tạm loại): Gaming: 90; Học tập: 85. Phong cách demo: Hiện đại; Tối giản. Điểm và phong cách do nhóm quy định.";
    private final CartContext cart = new CartContext("GH-SETUP-1", "QA1", "0900000001", "Khách thử một", "qa_setup");
    public static Connection open() throws SQLException {
        String url = System.getenv("DAVISY_TEST_URL");
        if (url == null || !url.matches("(?is).*databaseName=DAVISYS_SETUP_REVIEW_[A-Z0-9_]+(?:;.*)?"))
            throw new SQLException("Explicit disposable review DB required.");
        Connection c = DriverManager.getConnection(url, System.getenv("DAVISY_TEST_USER"), System.getenv("DAVISY_TEST_PASSWORD"));
        if (!c.getCatalog().startsWith("DAVISYS_SETUP_REVIEW_")) { c.close(); throw new SQLException("Unsafe database target"); }
        return c;
    }
    private static void execute(String sql, Object... args) throws SQLException {
        try (Connection c = open(); PreparedStatement s = c.prepareStatement(sql)) {
            for (int i=0;i<args.length;i++) s.setObject(i+1,args[i]); s.executeUpdate();
        }
    }
    private static long scalar(String sql) throws SQLException {
        try(Connection c=open(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)){r.next();return r.getLong(1);}
    }
    @Before public void reset() throws Exception {
        assumeTrue("Set DAVISY_TEST_URL/USER/PASSWORD to run SQL Server integration tests", System.getenv("DAVISY_TEST_URL") != null);
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        execute("DROP TRIGGER IF EXISTS dbo.ReviewFailCart; DROP TRIGGER IF EXISTS dbo.ReviewFailCheckout; "
                + "DELETE dbo.CHITIETHOADON; DELETE dbo.HOADON; DELETE dbo.GIOHANGTAM; DELETE dbo.GIOHANG; "
                + "DELETE dbo.KHACHHANG; DELETE dbo.TAIKHOAN; DELETE dbo.CHUCVU; DELETE dbo.SANPHAM; DELETE dbo.HANG; DELETE dbo.LOAIHANG;");
        execute("INSERT dbo.CHUCVU VALUES(0,N'Admin',N'Test only'); "
                + "INSERT dbo.TAIKHOAN VALUES('qa_setup',0,N'Nhân viên thử','qa@example.invalid','qa-setup-only',N'Test','0900000010','20000101',1,1); "
                + "INSERT dbo.TAIKHOAN VALUES('qa_other',0,N'Nhân viên khác','other@example.invalid','qa-other-only',N'Test','0900000020','20000101',1,1); "
                + "INSERT dbo.KHACHHANG VALUES('QA1',N'Khách thử một','0900000001',N'Test',0),('QA2',N'Khách thử hai','0900000002',N'Test',0); "
                + "INSERT dbo.GIOHANG VALUES('GH-SETUP-1','QA1','qa_setup'),('0900000002','QA2','qa_setup'),('GH-OTHER','QA1','qa_other'); "
                + "INSERT dbo.HANG VALUES('DEMO',N'Test brand'); INSERT dbo.LOAIHANG VALUES('BP',N'Bàn phím'),('CH',N'Chuột'),('TN',N'Tai nghe');");
        String[] codes={"SB0001","SB0002","SB0006","SB0008","SB0012","SB0013"};
        String[] categories={"BP","BP","CH","CH","TN","TN"};
        int[] prices={100000,200000,80000,250000,120000,180000};
        for(int i=0;i<codes.length;i++) execute("INSERT dbo.SANPHAM VALUES(?,?,?,'DEMO',20,50000,?,'20261023',?,?)",
                codes[i],"Sản phẩm thử "+codes[i],categories[i],prices[i],codes[i],DESCRIPTION);
    }
    private SetupCartDAO cartDao(){return new SetupCartDAO(SetupSqlServerIntegrationTest::open);}
    private CheckoutDAO checkoutDao(){return new CheckoutDAO(SetupSqlServerIntegrationTest::open);}
    private List<SetupProduct> combo(){
        var facade=new SetupBuilderFacade(new SetupFilterDAO(SetupSqlServerIntegrationTest::open),new SetupBuilderService());
        return facade.generateCombos(facade.getProducts("Gaming","Hiện đại"),1000000,List.of("BP","CH","TN")).get(0).getProducts();
    }
    @Test public void generateReplaceAddCorrectCartOneUnitPerProduct() throws Exception {
        var facade=new SetupBuilderFacade(new SetupFilterDAO(SetupSqlServerIntegrationTest::open),new SetupBuilderService());
        var products=facade.getProducts("Gaming","Hiện đại");
        var original=facade.generateCombos(products,1000000,List.of("BP","CH","TN")).get(0);
        var first=original.getProducts().get(0);
        var replacement=products.stream().filter(p->p.getMaLoai().equals(first.getMaLoai())&&!p.getMaSP().equals(first.getMaSP())).findFirst().orElseThrow();
        var changed=facade.replaceProduct(original,replacement,1000000);
        assertTrue(changed.getProducts().contains(replacement));
        cartDao().addCombo(cart,original.getProducts());
        assertEquals(3,scalar("SELECT COUNT(*) FROM dbo.GIOHANGTAM WHERE MAGH='GH-SETUP-1' AND SOLUONG=1"));
        assertEquals(0,scalar("SELECT COUNT(*) FROM dbo.GIOHANGTAM WHERE MAGH<>'GH-SETUP-1'"));
        assertEquals(120,scalar("SELECT SUM(SOLUONG) FROM dbo.SANPHAM"));
    }
    @Test public void stalePriceRollsBackWholeCombo() throws Exception {
        var products=combo(); execute("UPDATE dbo.SANPHAM SET GIABAN=GIABAN+1000 WHERE MASP=?",products.get(1).getMaSP());
        try{cartDao().addCombo(cart,products);fail();}catch(IllegalStateException expected){}
        assertEquals(0,scalar("SELECT COUNT(*) FROM dbo.GIOHANGTAM"));
    }
    @Test public void staleStockRollsBackWholeCombo() throws Exception {
        var products=combo(); execute("UPDATE dbo.SANPHAM SET SOLUONG=0 WHERE MASP=?",products.get(1).getMaSP());
        try{cartDao().addCombo(cart,products);fail();}catch(IllegalStateException expected){}
        assertEquals(0,scalar("SELECT COUNT(*) FROM dbo.GIOHANGTAM"));
    }
    @Test public void deletedProductRollsBackWholeCombo() throws Exception {
        var products=combo(); execute("DELETE dbo.SANPHAM WHERE MASP=?",products.get(1).getMaSP());
        try{cartDao().addCombo(cart,products);fail();}catch(IllegalStateException expected){}
        assertEquals(0,scalar("SELECT COUNT(*) FROM dbo.GIOHANGTAM"));
    }
    @Test public void secondCartLineFailureRollsBackFirstWrite() throws Exception {
        execute("CREATE TRIGGER dbo.ReviewFailCart ON dbo.GIOHANGTAM AFTER INSERT,UPDATE AS BEGIN IF EXISTS(SELECT 1 FROM inserted WHERE MASP='SB0006') THROW 51001,'Injected second cart line failure',1; END");
        try{cartDao().addCombo(cart,combo());fail();}catch(IllegalStateException expected){}
        assertEquals(0,scalar("SELECT COUNT(*) FROM dbo.GIOHANGTAM"));
    }
    @Test public void simultaneousAddsDoNotLoseUpdates() throws Exception {
        var products=combo();ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch ready=new CountDownLatch(2),go=new CountDownLatch(1);
        Callable<Void> work=()->{ready.countDown();if(!go.await(5,TimeUnit.SECONDS))throw new TimeoutException();cartDao().addCombo(cart,products);return null;};
        try{var one=pool.submit(work);var two=pool.submit(work);assertTrue(ready.await(5,TimeUnit.SECONDS));go.countDown();one.get(15,TimeUnit.SECONDS);two.get(15,TimeUnit.SECONDS);}finally{pool.shutdownNow();}
        assertEquals(3,scalar("SELECT COUNT(*) FROM dbo.GIOHANGTAM WHERE MAGH='GH-SETUP-1' AND SOLUONG=2"));
    }
    @Test public void checkoutKeepsOtherCustomerCartAndInvoicesEveryLine() throws Exception {
        cartDao().addCombo(cart,combo());execute("INSERT dbo.GIOHANGTAM VALUES('0900000002','SB0002',2)");
        long total=scalar("SELECT SUM(P.GIABAN*G.SOLUONG) FROM dbo.GIOHANGTAM G JOIN dbo.SANPHAM P ON P.MASP=G.MASP WHERE G.MAGH='GH-SETUP-1'");
        var receipt=checkoutDao().checkout(cart,total);
        assertEquals(3,receipt.lines());assertEquals(total,receipt.total());
        assertEquals(1,scalar("SELECT COUNT(*) FROM dbo.HOADON WHERE MAKH='QA1' AND MAGH='GH-SETUP-1' AND TENDN='qa_setup'"));
        assertEquals(3,scalar("SELECT COUNT(*) FROM dbo.CHITIETHOADON WHERE SOLUONG=1"));
        assertEquals(0,scalar("SELECT COUNT(*) FROM dbo.GIOHANGTAM WHERE MAGH='GH-SETUP-1'"));
        assertEquals(2,scalar("SELECT SOLUONG FROM dbo.GIOHANGTAM WHERE MAGH='0900000002'"));
        assertEquals(117,scalar("SELECT SUM(SOLUONG) FROM dbo.SANPHAM"));
        assertEquals(total/100000,scalar("SELECT TICHDIEM FROM dbo.KHACHHANG WHERE MAKH='QA1'"));
        assertEquals(3,new HoaDonCTDAOForTest().details(receipt.invoiceId()));
    }
    @Test public void checkoutSecondLineFailureRollsBackInvoiceStockCartAndPoints() throws Exception {
        cartDao().addCombo(cart,combo());
        execute("CREATE TRIGGER dbo.ReviewFailCheckout ON dbo.CHITIETHOADON AFTER INSERT AS BEGIN IF EXISTS(SELECT 1 FROM inserted WHERE MASP='SB0006') THROW 51002,'Injected second invoice line failure',1; END");
        long total=scalar("SELECT SUM(P.GIABAN*G.SOLUONG) FROM dbo.GIOHANGTAM G JOIN dbo.SANPHAM P ON P.MASP=G.MASP");
        try{checkoutDao().checkout(cart,total);fail();}catch(IllegalStateException expected){}
        assertEquals(0,scalar("SELECT COUNT(*) FROM dbo.HOADON"));assertEquals(0,scalar("SELECT COUNT(*) FROM dbo.CHITIETHOADON"));
        assertEquals(3,scalar("SELECT COUNT(*) FROM dbo.GIOHANGTAM"));assertEquals(120,scalar("SELECT SUM(SOLUONG) FROM dbo.SANPHAM"));
        assertEquals(0,scalar("SELECT SUM(TICHDIEM) FROM dbo.KHACHHANG"));
    }
    @Test public void checkoutRejectsChangedTotalAndForeignOwner() throws Exception {
        cartDao().addCombo(cart,combo());
        try{checkoutDao().checkout(cart,1);fail();}catch(IllegalStateException expected){}
        var foreign=new CartContext(cart.cartId,cart.customerId,cart.phone,cart.customerName,"qa_other");
        try{checkoutDao().checkout(foreign,300000);fail();}catch(IllegalStateException expected){}
        assertEquals(0,scalar("SELECT COUNT(*) FROM dbo.HOADON"));assertEquals(3,scalar("SELECT COUNT(*) FROM dbo.GIOHANGTAM"));
    }
    @Test public void checkoutRejectsStockChangesWithoutPartialWrites() throws Exception {
        cartDao().addCombo(cart,combo());
        execute("UPDATE dbo.SANPHAM SET SOLUONG=0 WHERE MASP='SB0006'");
        try { checkoutDao().checkout(cart,300000); fail(); } catch(IllegalStateException expected) { }
        assertEquals(0,scalar("SELECT COUNT(*) FROM dbo.HOADON"));
        assertEquals(3,scalar("SELECT COUNT(*) FROM dbo.GIOHANGTAM"));
        assertEquals(20,scalar("SELECT SOLUONG FROM dbo.SANPHAM WHERE MASP='SB0001'"));
    }
    @Test public void checkoutIdsUseMaxNotCountAndLargeTotalsRemainExact() throws Exception {
        execute("INSERT dbo.HOADON(MAHD,TENDN,MAKH,MAGH,NGAYLAP,TONGTIEN,PHANTRAMGG,TRUTIENTICHDIEM,THANHTIEN) VALUES('HD02','qa_setup','QA1','GH-SETUP-1','20261023',0,0,0,0)");
        execute("UPDATE dbo.SANPHAM SET GIABAN=3000000000 WHERE MASP='SB0001'");
        execute("INSERT dbo.GIOHANGTAM VALUES('GH-SETUP-1','SB0001',1)");
        var receipt=checkoutDao().checkout(cart,3000000000L);
        assertEquals("HD03",receipt.invoiceId());assertEquals(3000000000L,receipt.total());
        assertEquals(3000000000L,scalar("SELECT TONGTIEN FROM dbo.HOADON WHERE MAHD='HD03'"));
    }
    @Test public void checkoutRejectsLegacyFloatRounding() throws Exception {
        execute("UPDATE dbo.SANPHAM SET GIABAN=16777217 WHERE MASP='SB0001'");
        execute("INSERT dbo.GIOHANGTAM VALUES('GH-SETUP-1','SB0001',1)");
        try { checkoutDao().checkout(cart,16777217); fail(); } catch(IllegalStateException expected) { }
        assertEquals(0,scalar("SELECT COUNT(*) FROM dbo.HOADON"));
        assertEquals(1,scalar("SELECT COUNT(*) FROM dbo.GIOHANGTAM"));
    }
    // Exercise the legacy invoice joins over the same database without overriding global JDBC config.
    private static class HoaDonCTDAOForTest {
        long details(String id) throws SQLException {
            try(Connection c=open();PreparedStatement s=c.prepareStatement("SELECT COUNT(*) FROM dbo.CHITIETHOADON D JOIN dbo.HOADON H ON H.MAHD=D.MAHD JOIN dbo.KHACHHANG K ON K.MAKH=H.MAKH JOIN dbo.TAIKHOAN T ON T.TENDN=H.TENDN WHERE H.MAHD=?")){s.setString(1,id);try(ResultSet r=s.executeQuery()){r.next();return r.getLong(1);}}
        }
    }
    public static void main(String[] args) throws Exception {new SetupSqlServerIntegrationTest().reset();System.out.println("Review fixtures reset for GUI");}
}
