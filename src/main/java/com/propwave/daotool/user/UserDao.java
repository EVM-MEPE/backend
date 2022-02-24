package com.propwave.daotool.user;

import com.propwave.daotool.user.model.User;
import com.propwave.daotool.wallet.model.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Map;

@Repository
public class UserDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){this.jdbcTemplate = new JdbcTemplate(dataSource);}

    public User createUser(Map<String, Object> userInfo, String profileImageS3Path){
        userInfo.replace("profileImage", profileImageS3Path);
        String createUserQuery = "INSERT INTO user(id, profileImage, introduction, url) VALUES(?,?,?,?)";
        Object[] createUserParams = new Object[]{userInfo.get("id"), userInfo.get("profileImage"), userInfo.get("introduction"), userInfo.get("url")};
        this.jdbcTemplate.update(createUserQuery, createUserParams);
        User newUser = getUserAllInfo((String)userInfo.get("id"));
        return newUser;
    }

    public User getUserAllInfo(String id){
        String getUserQuery = "select * from user where id=?";
        String getUserParams = id;
        return this.jdbcTemplate.queryForObject(getUserQuery,
                (rs, rowNum) -> new User(
                        rs.getString("id"),
                        rs.getString("profileImage"),
                        rs.getString("introduction"),
                        rs.getString("url"),
                        rs.getInt("hits"),
                        rs.getTimestamp("createdAt")
                ),
                getUserParams
        );
    }

    //지갑 유무 확인
    public int isWalletExist(String walletAddress){
        String walletExistQuery = "select exists(select * from wallet where address = ?)";
        String walletExistParam = walletAddress;
        return this.jdbcTemplate.queryForObject(walletExistQuery, int.class, walletExistParam);
    }

    public String createWallet(String walletAddress){
        String walletCreateQuery = "INSERT INTO wallet(address) VALUES(?)";
        String walletCreateParam = walletAddress;
        System.out.println(walletCreateParam);
        this.jdbcTemplate.update(walletCreateQuery, walletCreateParam);
        return walletAddress;
    }

    public String createUserWallet(Map<String, Object> wallet, String userId){
        String createUserWalletQuery = "INSERT INTO userWallet(user, walletAddress, walletName, walletIcon, loginAvailable, viewDataAvailable) VALUES(?,?,?,?,?,?)";
        Object[] createUserWalletParam = new Object[]{userId, wallet.get("address"), wallet.get("name"), wallet.get("icon"), wallet.get("loginAvailable"), wallet.get("viewDataAvailable")};
        this.jdbcTemplate.update(createUserWalletQuery, createUserWalletParam);
        return (String)wallet.get("address");
    }
}
