package com.propwave.daotool.badge;

import com.propwave.daotool.badge.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BadgeDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 뱃지 이름, 사진 가져오기
    public BadgeNameImage getBadgeNameImage(String badgeName){
        String getBadgeNameImageQuery = "select name, image from badge where name=?";
        String getBadgeNameParams = badgeName;
        return this.jdbcTemplate.queryForObject(getBadgeNameImageQuery,
                (rs, rowNum) -> new BadgeNameImage(
                        rs.getString("name"),
                        rs.getString("image")
                ),
                getBadgeNameParams
                );
    }

    //뱃지 join 날짜
    public List<BadgeJoinedAt> getBadgeJoinedAt(String walletAddress){
        String getBadgeJoinedAtQuery = "select badgeName, joinedAt from badgeWallet where walletAddress=?";
        String getBadgeJoinedAtParam = walletAddress;
        return this.jdbcTemplate.query(getBadgeJoinedAtQuery,
                (rs, rowNum) -> new BadgeJoinedAt(
                        rs.getString("badgeName"),
                        rs.getTimestamp("joinedAt")
                ),
                getBadgeJoinedAtParam
                );
    }

    public int checkUser(String userId){
        String getBadgeJoinedAtQuery = "select exists(select * from user where id=?)";
        String getBadgeJoinedAtParam = userId;
        return this.jdbcTemplate.queryForObject(getBadgeJoinedAtQuery,
                int.class,
                getBadgeJoinedAtParam);
    }

    public Badge getBadgeInfo(String badgeName){
        String getBadgeInfoQuery = "select * from badge where name=?";
        String getBadgeInfoParam = badgeName;
        return this.jdbcTemplate.queryForObject(getBadgeInfoQuery,
                (rs, rowNum) -> new Badge(
                        rs.getString("name"),
                        rs.getString("image"),
                        rs.getString("explanation"),
                        rs.getTimestamp("createdAt")),
                getBadgeInfoParam);
    }

    public List<BadgeWallet> getBadgeWalletByBadgeName(String BadgeName){
        String getBadgeWalletQuery = "select * from badgeWallet where badgeName=?";
        String getBadgeWalletParam = BadgeName;
        return this.jdbcTemplate.query(getBadgeWalletQuery,
                (rs, rowNum) -> new BadgeWallet(
                        rs.getInt("index"),
                        rs.getString("walletAddress"),
                        rs.getString("badgeName"),
                        rs.getTimestamp("joinedAt")),
                getBadgeWalletParam
        );
    }

    public List<UserDataAvailable> getUserNameByWallet(String walletAddress){
        String getUserNameByWalletQuery = "select user, viewDataAvailable from userWallet where walletAddress = ?";
        String getUserNameByWalletParam = walletAddress;
        return this.jdbcTemplate.query(getUserNameByWalletQuery,
                (rs, rowNum) -> new UserDataAvailable(
                        rs.getString("user"),
                        rs.getBoolean("viewDataAvailable")),
            getUserNameByWalletParam);
    }

    public List<UserSimple> getUserSimple(String userName){
        String getUserSimpleQuery = "select id, profileImage from user where id=?";
        String getUserSimpleParam = userName;
        return this.jdbcTemplate.query(getUserSimpleQuery,
                (rs, rowNum) -> new UserSimple(
                        rs.getString("id"),
                        rs.getString("profileImage")),
                getUserSimpleParam);
    }
}
