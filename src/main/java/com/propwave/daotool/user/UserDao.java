package com.propwave.daotool.user;

import com.propwave.daotool.badge.model.Badge;
import com.propwave.daotool.badge.model.BadgeWallet;
import com.propwave.daotool.user.model.User;
import com.propwave.daotool.wallet.model.UserWallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
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

    public List<UserWallet> getAllUserWalletByWalletId(String walletAddress){
        // userWallet에서 로그인 가능한 친구 가져오기
        String getUserWalletQuery = "select * from userWallet where walletAddress=?";
        String getUserWalletParam = walletAddress;
        return this.jdbcTemplate.query(getUserWalletQuery,
                (rs, rowNum) -> new UserWallet(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("walletAddress"),
                        rs.getBoolean("loginAvailable"),
                        rs.getBoolean("viewDataAvailable"),
                        rs.getString("walletName"),
                        rs.getString("walletIcon"),
                        rs.getTimestamp("createdAt")
                ),
                getUserWalletParam
        );
    }

    public List<UserWallet> getAllUserWalletByUserId(String userId){
        String getUserWalletQuery = "select * from userWallet where user=?";
        String getUserWalletParam = userId;
        return this.jdbcTemplate.query(getUserWalletQuery,
                (rs, rowNum) -> new UserWallet(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("walletAddress"),
                        rs.getBoolean("loginAvailable"),
                        rs.getBoolean("viewDataAvailable"),
                        rs.getString("walletName"),
                        rs.getString("walletIcon"),
                        rs.getTimestamp("createdAt")
                ),
                getUserWalletParam
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

    public List<BadgeWallet> getAllBadgeWallet(String walletAddress){
        String getAllBadgeQuery = "select * from badgeWallet where walletAddress=?";
        String getAllBadgeParam = walletAddress;
        return this.jdbcTemplate.query(getAllBadgeQuery,
                (rs, rowNum) -> new BadgeWallet(
                        rs.getInt("index"),
                        rs.getString("walletAddress"),
                        rs.getString("badgeName"),
                        rs.getTimestamp("joinedAt")
                ),
                getAllBadgeParam
        );
    }

    public Badge getBadge(String badgeName){
        String getBadgeQuery = "select * from badge where name=?";
        String getBadgeParam = badgeName;
        return this.jdbcTemplate.queryForObject(getBadgeQuery,
                (rs, rowNum) -> new Badge(
                        rs.getString("name"),
                        rs.getString("image"),
                        rs.getString("explanation"),
                        rs.getTimestamp("createdAt")
                ),
                getBadgeParam
        );
    }
}
