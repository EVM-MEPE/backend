package com.propwave.daotool.user;

import com.propwave.daotool.badge.model.Badge;
import com.propwave.daotool.badge.model.BadgeJoinedAt;
import com.propwave.daotool.badge.model.BadgeNameImage;
import com.propwave.daotool.badge.model.BadgeWallet;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.user.model.User;
import com.propwave.daotool.wallet.model.UserWallet;
import com.propwave.daotool.wallet.model.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static com.propwave.daotool.config.BaseResponseStatus.DATABASE_ERROR;

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

    public UserWallet getUserWalletByWalletAddressAndUserId(String userId, String walletAddress){
        String getUserWalletByWalletAddressAndUserIdQuery = "select * from userWallet where user=? and walletAddress=?";
        Object[] getUserWalletByWalletAddressAndUserIdParam = new Object[]{userId, walletAddress};
        return this.jdbcTemplate.queryForObject(getUserWalletByWalletAddressAndUserIdQuery,
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
                getUserWalletByWalletAddressAndUserIdParam
        );
    }

    public int isUserWalletByWalletAddressAndUserIdExist(String userId, String walletAddress){
        String getUserWalletByWalletAddressAndUserIdQuery = "select exists(select * from userWallet where user=? and walletAddress=?)";
        Object[] getUserWalletByWalletAddressAndUserIdParam = new Object[]{userId, walletAddress};
        return this.jdbcTemplate.queryForObject(getUserWalletByWalletAddressAndUserIdQuery,
                int.class,
                getUserWalletByWalletAddressAndUserIdParam
        );
    }

    // 로그인용으로 지갑 바꾸기
    public int makeLoginAvailable(int index){
        String makeLoginAvailableQuery = "update userWallet set loginAvailable=true where `index`=?";
        return this.jdbcTemplate.update(makeLoginAvailableQuery, index);
    }


    //지갑 유무 확인
    public int isWalletExist(String walletAddress){
        String walletExistQuery = "select exists(select * from wallet where address = ? )";
        String walletExistParam = walletAddress;
        return this.jdbcTemplate.queryForObject(walletExistQuery, int.class, walletExistParam);
    }

    //지갑이 로그인용으로 있는지 유무 확인
    public int isWalletExistForLogin(String walletAddress){
        String isWalletExistForLoginQuery = "select exists(select * from userWallet where walletAddress = ? AND loginAvailable=1)";
        String isWalletExistForLoginParam = walletAddress;
        return this.jdbcTemplate.queryForObject(isWalletExistForLoginQuery, int.class, isWalletExistForLoginParam);
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

    public List<BadgeWallet> getBadgeWalletByBadgeName(String BadgeName){
        String getBadgeWalletQuery = "select * from badgeWallet where badgeName=? ";
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

    public int addHit(String userId){
        String modifyUserHitsQuery = "update user set hits = hits + 1 where id = ?";
        String modifyUserHitsParam = userId;
        return this.jdbcTemplate.update(modifyUserHitsQuery, modifyUserHitsParam);

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

    // 다른 사람에게 해당 지갑이 있는지 여부
    int isWalletSomeoneElse(String userId, String walletAddress){
        String isWalletSomeoneElseQuery = "select exists(select * from userWallet where (not user=?) and walletAddress=? )";
        Object[] isWalletSomeoneElseParams = new Object[] {userId, walletAddress};
        return this.jdbcTemplate.queryForObject(isWalletSomeoneElseQuery,
                int.class,
                isWalletSomeoneElseParams
        );
    }

    // 나에게 대시보드 용도 있는지 여부
    public int isWalletMyDashboard(String userId, String walletAddress){
        String isWalletMyDashboardQuery = "select exists(select * from userWallet where user=? and walletAddress=? and viewDataAvailable=1)";
        Object[] isWalletMyDashboardParams = new Object[] {userId, walletAddress};
        return this.jdbcTemplate.queryForObject(isWalletMyDashboardQuery,
                int.class,
                isWalletMyDashboardParams
        );
    }

    //
    public int makeLoginUnavailable(String userId, String walletAddress) {
        String makeLoginAvailableQuery = "update userWallet set loginAvailable=false where user=? and walletAddress=?";
        Object[] isWalletMyDashboardParams = new Object[] {userId, walletAddress};
        return this.jdbcTemplate.update(makeLoginAvailableQuery, isWalletMyDashboardParams);
    }

    public int deleteUserWallet(String userId, String walletAddress) {
        String deleteUserWalletQuery = "delete from userWallet where user=? and walletAddress=?";
        Object[] deleteUserWalletParams = new Object[] {userId, walletAddress};
        return this.jdbcTemplate.update(deleteUserWalletQuery, deleteUserWalletParams);
    }

    public int deleteWallet(String walletAddress){
        String deleteWalletQuery = "delete from wallet where address=?";
        return this.jdbcTemplate.update(deleteWalletQuery, walletAddress);
    }
}
