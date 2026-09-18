package com.abisheikmart.dao;

import com.abisheikmart.model.Product;
import com.abisheikmart.util.DBUtil;
import org.junit.jupiter.api.*;

import java.io.InputStream; import java.nio.charset.StandardCharsets; import java.sql.*; import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class ProductDaoTest {
    private static final ProductDao DAO = new ProductDao();
    @BeforeAll static void init() throws Exception { Properties p=new Properties();p.setProperty("db.url","jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");p.setProperty("db.driver","org.h2.Driver");DBUtil.initDataSource(p);try(InputStream in=ProductDaoTest.class.getClassLoader().getResourceAsStream("schema.sql");Connection c=DBUtil.getConnection();Statement s=c.createStatement()){assertNotNull(in);for(String sql:new String(in.readAllBytes(),StandardCharsets.UTF_8).split(";"))if(!sql.isBlank())try{s.execute(sql);}catch(SQLException ignored){}}}
    @BeforeEach void reset() throws Exception {try(Connection c=DBUtil.getConnection();Statement s=c.createStatement()){s.executeUpdate("DELETE FROM reviews");s.executeUpdate("DELETE FROM products");s.executeUpdate("DELETE FROM categories");s.executeUpdate("DELETE FROM users");s.executeUpdate("INSERT INTO users (id,name,email,password_hash,role) VALUES (1,'Seller','seller@t','h','SELLER')");s.executeUpdate("INSERT INTO categories (id,name) VALUES (1,'Books'),(2,'Games')");s.executeUpdate("INSERT INTO products (id,seller_id,category_id,name,description,original_price,price,stock,image_url) VALUES (1,1,1,'Alpha Book','alpha text',20,10,3,''),(2,1,2,'Beta Game','beta text',30,25,4,'')");}}
    @AfterAll static void close(){DBUtil.closeDataSource();}
    @Test void retrievesSearchesFiltersSortsAndScopesSellerProducts() throws Exception {assertEquals(2,DAO.findAll().size());assertEquals(1,DAO.findByCategoryId(1L).size());assertEquals(1,DAO.search("alpha",null,"name").size());assertEquals(2,DAO.findBySellerId(1L).size());assertEquals(1,DAO.findById(1L).orElseThrow().getId());assertEquals(10.0,DAO.search(null,null,"price-low").get(0).getPrice());}
    @Test void inactiveProductsLeaveNormalCatalogAndCanBeRestored() throws Exception {assertTrue(DAO.updateProductStatus(1L,false));assertTrue(DAO.findAll().stream().noneMatch(p->p.getId().equals(1L)));assertEquals(1,DAO.findAdminProducts(null,null,null,false).size());assertTrue(DAO.updateProductStatus(1L,true));}
}
