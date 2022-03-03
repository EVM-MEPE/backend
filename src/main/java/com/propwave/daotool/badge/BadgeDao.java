package com.propwave.daotool.badge;

import com.propwave.daotool.badge.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Repository
public class BadgeDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
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

    public List<Map<String, Object>> getAllBadges(String orderBy){
        String getAllBadgesQuery = null;
        if (orderBy.equals("oldest")){
            getAllBadgesQuery = "select name, image, createdAt from badge order by createdAt ASC";
        }
        else {
            getAllBadgesQuery = "select name, image, createdAt from badge order by createdAt DESC";
        }

        return this.jdbcTemplate.queryForList(getAllBadgesQuery);
    }

    public int getBadgeJoinedWalletCount(String badgeName){
        String getBadgeJoinedWalletCountQuery = "select count(*) from badgeWallet where badgeName = ?";
        return this.jdbcTemplate.queryForObject(getBadgeJoinedWalletCountQuery,
                int.class,
                badgeName
        );
    }
}
