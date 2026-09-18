package com.abisheikmart.dao;

import com.abisheikmart.model.User;
import com.abisheikmart.util.DBUtil;
import org.junit.jupiter.api.*;

import java.io.InputStream; import java.nio.charset.StandardCharsets; import java.sql.*; import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest {
    private static final UserDao DAO = new UserDao();
    @BeforeAll static void init() throws Exception { Properties p=new Properties(); p.setProperty("db.url","jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"); p.setProperty("db.driver","org.h2.Driver"); DBUtil.initDataSource(p); try(InputStream in=UserDaoTest.class.getClassLoader().getResourceAsStream("schema.sql"); Connection c=DBUtil.getConnection(); Statement s=c.createStatement()){ assertNotNull(in); for(String sql:new String(in.readAllBytes(),StandardCharsets.UTF_8).split(";")) if(!sql.isBlank()) try{s.execute(sql);}catch(SQLException ignored){} } }
    @BeforeEach void reset() throws Exception { try(Connection c=DBUtil.getConnection(); Statement s=c.createStatement()){ s.executeUpdate("DELETE FROM users"); } }
    @AfterAll static void close(){DBUtil.closeDataSource();}
    @Test void savesFindsAndFiltersUsersWithoutExposingPersistenceBehaviorToCallers() throws Exception { User u=new User(); u.setName("Buyer");u.setEmail("buyer@test");u.setPasswordHash("bcrypt-hash");u.setRole("CUSTOMER"); User saved=DAO.save(u); assertNotNull(saved.getId()); assertEquals("buyer@test",DAO.findByEmail("buyer@test").orElseThrow().getEmail()); assertTrue(DAO.existsByEmail("buyer@test")); assertEquals(1,DAO.findAllUsers("buyer","CUSTOMER",true).size()); assertTrue(DAO.updateUserStatus(saved.getId(),false)); assertTrue(DAO.findAllUsers(null,null,false).stream().anyMatch(x->x.getId().equals(saved.getId()))); }
    @Test void emailUniquenessIsEnforcedByDatabase() throws Exception { User a=new User();a.setName("A");a.setEmail("same@test");a.setPasswordHash("h");a.setRole("CUSTOMER");DAO.save(a); User b=new User();b.setName("B");b.setEmail("same@test");b.setPasswordHash("h");b.setRole("CUSTOMER");assertThrows(SQLException.class,()->DAO.save(b)); }
}
