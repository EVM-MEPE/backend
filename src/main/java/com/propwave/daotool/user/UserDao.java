package com.propwave.daotool.user;

import com.propwave.daotool.badge.model.BadgeWallet;
import com.propwave.daotool.user.model.*;
import com.propwave.daotool.user.model.UserWallet;
import com.propwave.daotool.wallet.model.*;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@EnableScheduling
@Repository
public class UserDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){this.jdbcTemplate = new JdbcTemplate(dataSource);}

    public User createUser(Map<String, Object> userInfo){
        String createUserQuery = "INSERT INTO user(id, profileImage, introduction, url) VALUES(?,?,?,?)";
        Object[] createUserParams = new Object[]{userInfo.get("id"), userInfo.get("profileImage"), userInfo.get("introduction"), userInfo.get("url")};
        this.jdbcTemplate.update(createUserQuery, createUserParams);
        return getUserInfo((String)userInfo.get("id"));
    }

    public User createUser(String userID){
        String createUserQuery = "INSERT INTO user(id, nickname) VALUES (?, ?)";
        Object[] createUserParams = new Object[]{userID, userID};
        this.jdbcTemplate.update(createUserQuery, createUserParams);
        return getUserInfo(userID);
    }

    public User getUserInfo(String id){
        String getUserQuery = "select * from user where id=?";
        return this.jdbcTemplate.queryForObject(getUserQuery,
                (rs, rowNum) -> new User(
                        rs.getString("id"),
                        rs.getString("introduction"),
                        rs.getString("url"),
                        rs.getInt("hits"),
                        rs.getInt("todayHits"),
                        rs.getInt("todayFollows"),
                        rs.getTimestamp("createdAt"),
                        rs.getInt("nftRefreshLeft"),
                        rs.getString("backImage"),
                        rs.getString("nickname"),
                        rs.getInt("index")
                ),
                id
        );
    }

    public int checkUserIdExist(String userId){
        String checkUserIdExistQuery = "select exists(select * from user where id = ?)";
        return this.jdbcTemplate.queryForObject(checkUserIdExistQuery,
                int.class,
                userId
        );
    }

    public int editUserProfileImg(String userID, String profileImagePath){
        System.out.println(profileImagePath);
        String editUserProfileImgQuery ="INSERT INTO profileImg(user, imgUrl) VALUES(?,?)";
        //String editUserProfileImgQuery = "UPDATE user SET profileImage=? WHERE id = ?";
        Object[] editUserProfileImgParams = new Object[]{userID, profileImagePath};
        return this.jdbcTemplate.update(editUserProfileImgQuery, editUserProfileImgParams);
    }

    public int deleteProfileImgHistory(String userID, int profileIndex){
        String deleteProfileImgQuery = "DELETE FROM profileImg WHERE `user`=? AND `index`=?";
        Object[] deleteProfileImgParam = new Object[]{userID, profileIndex};
        return this.jdbcTemplate.update(deleteProfileImgQuery, deleteProfileImgParam);
    }

    public int hideProfileImgHistory(String userID, int profileIndex, boolean hide){
        String hideProfileImgQuery = "UPDATE profileImg SET `isHide`=? WHERE `user`=? AND `index`=?";
        Object[] hideProfileImgParam = new Object[]{hide, userID, profileIndex};
        return this.jdbcTemplate.update(hideProfileImgQuery, hideProfileImgParam);
    }

    public int editUserBackImg(String userID, String backImagePath){
        String editUserBackImgQuery = "UPDATE user SET backImage=? WHERE id = ?";
        Object[] editUserBackImgParams = new Object[]{backImagePath, userID};
        return this.jdbcTemplate.update(editUserBackImgQuery, editUserBackImgParams);
    }

    public User editUserProfile(String userID, String profileName, String introduction, String url){
        String editUserQuery = "UPDATE user SET nickname=?, introduction=?, url=? WHERE id = ?";
        Object[] editUserParams = new Object[]{profileName, introduction, url, userID};
        this.jdbcTemplate.update(editUserQuery, editUserParams);
        return getUserInfo(userID);
    }

    public Social createUserSocial(String userID, String twitter, String facebook, String discord, String link){
        String createUserSocialQuery = "INSERT INTO social(userId, twitter, facebook, discord, link) VALUES(?,?,?,?,?)";
        Object[] createUserSocialParam = new Object[]{userID, twitter, facebook, discord, link};
        this.jdbcTemplate.update(createUserSocialQuery, createUserSocialParam);
        return getUserSocial(userID);
    }

    public Social changeUserSocial(String userID, String twitter, String facebook, String discord, String link){
        String changeUserSocial = "UPDATE social SET twitter=?, facebook=?, discord=?, link=? WHERE userId=?";
        Object[] createUserSocialParam = new Object[]{twitter, facebook, discord, link, userID};
        this.jdbcTemplate.update(changeUserSocial, createUserSocialParam);
        return getUserSocial(userID);
    }

    public Social getUserSocial(String userID){
        String getUserSocialQuery = "select * from social where userId = ?";
        return this.jdbcTemplate.queryForObject(getUserSocialQuery,
                (rs, rowNum) -> new Social(
                        rs.getString("userId"),
                        rs.getString("twitter"),
                        rs.getString("facebook"),
                        rs.getString("discord"),
                        rs.getString("link")
                ),
                userID
        );
    }

    public int deleteUser(String userId){
        String deleteUserQuery = "delete from user where id=?";
        return this.jdbcTemplate.update(deleteUserQuery, userId);
    }

    //user의 프로필 이미지 주소 가져오기
    public String getUserImagePath(String userId){
        String getUserImageQuery = "SELECT imgUrl FROM profileImg WHERE user = ? ORDER BY `index` DESC LIMIT 1";
        return this.jdbcTemplate.queryForObject(getUserImageQuery, String.class, userId);
    }

    public List<ProfileImg> getProfileImgHistory(String userID){
        String getProfileImgHistoryQuery = "SELECT * FROM profileImg WHERE user = ? ORDER BY `index` DESC";
        return this.jdbcTemplate.query(getProfileImgHistoryQuery,
                (rs, rowNum) -> new ProfileImg(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("imgUrl"),
                        rs.getBoolean("isHide"),
                        rs.getTimestamp("createdAt")
                ),
                userID
        );
    }

    public int addHit(String userId){
        String modifyUserHitsQuery = "update user set hits = hits + 1, todayHits = todayHits + 1 where id = ?";
        return this.jdbcTemplate.update(modifyUserHitsQuery, userId);

    }

    public Social getSocial(String userId){
        try{
            String getSocialQuery = "SELECT * FROM social WHERE userId=?";
            return this.jdbcTemplate.queryForObject(getSocialQuery,
                    (rs, rowNum) -> new Social(
                            rs.getString("userId"),
                            rs.getString("twitter"),
                            rs.getString("facebook"),
                            rs.getString("discord"),
                            rs.getString("link")
                    ),
                    userId
            );
        }catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int createNotification(String userID, int type, String message, int... optionIdx){
        switch(type){
            case 1: String createNotificationQuery1 = "INSERT INTO notification(user, type, message) VALUES(?,?,?)";
                    Object[] createNotificationParam1 = new Object[]{userID, type, message};
                    return this.jdbcTemplate.update(createNotificationQuery1, createNotificationParam1);
            case 2: String createNotificationQuery2 = "INSERT INTO notification(user, type, message, friendReq) VALUES(?,?,?,?)";
                    Object[] createNotificationParam2 = new Object[]{userID, type, message, optionIdx[0]};
                    return this.jdbcTemplate.update(createNotificationQuery2, createNotificationParam2);
            case 3: String createNotificationQuery3 = "INSERT INTO notification(user, type, message, friend) VALUES(?,?,?,?)";
                    Object[] createNotificationParam3 = new Object[]{userID, type, message, optionIdx[0]};
                    return this.jdbcTemplate.update(createNotificationQuery3, createNotificationParam3);
            case 4: String createNotificationQuery4 = "INSERT INTO notification(user, type, message, comment) VALUES(?,?,?,?)";
                    Object[] createNotificationParam4 = new Object[]{userID, type, message, optionIdx[0]};
                    return this.jdbcTemplate.update(createNotificationQuery4, createNotificationParam4);
            case 5: String createNotificationQuery5 = "INSERT INTO notification(user, type, message, follow) VALUES(?,?,?,?)";
                    Object[] createNotificationParam5 = new Object[]{userID, type, message, optionIdx[0]};
                    return this.jdbcTemplate.update(createNotificationQuery5, createNotificationParam5);
            default:
                break;
        }
        return -1;
    }

    public List<Notification> getUserNotificationList(String userID){
        String getUserNotificationListQuery = "SELECT * FROM notification WHERE user=? ORDER BY `index` DESC";
        return this.jdbcTemplate.query(getUserNotificationListQuery,
                (rs, rowNum) -> new Notification(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getInt("type"),
                        rs.getInt("friendReq"),
                        rs.getInt("friend"),
                        rs.getInt("comment"),
                        rs.getInt("follow"),
                        rs.getInt("transaction"),
                        rs.getInt("tokenReq"),
                        rs.getString("message"),
                        rs.getBoolean("isChecked"),
                        rs.getTimestamp("createdAt")
                ),
                userID
        );
    }

    public int checkNotification(int index){
        String checkNotificationQuery = "UPDATE notification SET isChecked=true where `index` = ?";
        return this.jdbcTemplate.update(checkNotificationQuery, index);
    }

    public boolean isUncheckedNotificationLeft(String userID){
        String isUncheckedNotificationLeftQuery = "select exists(select * from notification where user = ? AND isChecked=false)";
        return this.jdbcTemplate.queryForObject(isUncheckedNotificationLeftQuery,
                Boolean.class,
                userID
        );
    }

    public Notification getNotification(int index){
        String getNotificationQuery = "SELECT * FROM notification WHERE `index`=?";
        return this.jdbcTemplate.queryForObject(getNotificationQuery,
                (rs, rowNum) -> new Notification(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getInt("type"),
                        rs.getInt("friendReq"),
                        rs.getInt("friend"),
                        rs.getInt("comment"),
                        rs.getInt("follow"),
                        rs.getInt("transaction"),
                        rs.getInt("tokenReq"),
                        rs.getString("message"),
                        rs.getBoolean("isChecked"),
                        rs.getTimestamp("createdAt")
                ),
                index
        );
    }

    public int deleteANotification(int notiID){
        String deleteANotificationQuery = "DELETE FROM notification WHERE `index`=?";
        return this.jdbcTemplate.update(deleteANotificationQuery, notiID);
    }

    public int deleteAllNotification(String userID){
        String deleteAllNotification = "DELETE FROM notification WHERE user=?";
        return this.jdbcTemplate.update(deleteAllNotification, userID);
    }

    public List<User> getUserList(String orderBy){
        String getUserList = "SELECT * FROM user ORDER BY `createdAt`";
        if(orderBy.equals("todayHits")){
            getUserList = "SELECT * FROM user ORDER BY todayHits DESC";
        }else if(orderBy.equals("todayFollows")){
            getUserList = "SELECT * FROM user ORDER BY todayFollows DESC";
        }
        return this.jdbcTemplate.query(getUserList,
                (rs, rowNum) -> new User(
                        rs.getString("id"),
                        rs.getString("introduction"),
                        rs.getString("url"),
                        rs.getInt("hits"),
                        rs.getInt("todayHits"),
                        rs.getInt("todayFollows"),
                        rs.getTimestamp("createdAt"),
                        rs.getInt("nftRefreshLeft"),
                        rs.getString("backImage"),
                        rs.getString("nickname"),
                        rs.getInt("index")
                )
        );
    }

    public int createComment(String userID, String friendID, String message){
        String createCommentQuery = "INSERT INTO comment(commentTo, commentFrom, message) VALUES(?,?,?)";
        Object[] createCommentParam = new Object[]{friendID, userID, message};
        return this.jdbcTemplate.update(createCommentQuery, createCommentParam);
    }

    public Comment getComment(String userID, String friendID, String message){
        String getCommentQuery = "SELECT * FROM comment WHERE commentTo=? and commentFrom=? and message=?";
        Object[] getCommentParam = new Object[]{friendID, userID, message};
        return this.jdbcTemplate.queryForObject(getCommentQuery,
                (rs, rowNum) -> new Comment(
                        rs.getInt("index"),
                        rs.getString("commentTo"),
                        rs.getString("commentFrom"),
                        rs.getString("message"),
                        rs.getBoolean("isPinned"),
                        rs.getBoolean("isHide"),
                        rs.getTimestamp("createdAt")
                ),
                getCommentParam
        );
    }

    public Optional<Comment> getOptionalComment(int idx){
        String getCommentQuery = "SELECT * FROM comment WHERE 'index'=?";
        return Optional.ofNullable(this.jdbcTemplate.queryForObject(getCommentQuery,
                (rs, rowNum) -> new Comment(
                        rs.getInt("index"),
                        rs.getString("commentTo"),
                        rs.getString("commentFrom"),
                        rs.getString("message"),
                        rs.getBoolean("isPinned"),
                        rs.getBoolean("isHide"),
                        rs.getTimestamp("createdAt")
                ),
                idx
        ));
    }


    public Comment getComment(int index){
        String getCommentQuery = "SELECT * FROM comment WHERE `index`=?";
        return this.jdbcTemplate.queryForObject(getCommentQuery,
                (rs, rowNum) -> new Comment(
                    rs.getInt("index"),
                    rs.getString("commentTo"),
                    rs.getString("commentFrom"),
                    rs.getString("message"),
                    rs.getBoolean("isPinned"),
                    rs.getBoolean("isHide"),
                    rs.getTimestamp("createdAt")
            ),
            index);
    }

    public List<CommentWithInfo> getAllCommentsExceptPinnedForUser(String userID){
        String getQuery = "SELECT C.*, U1.nickname, U2.profileImg, F.friendName FROM comment C, user U1, user U2, friend F WHERE C.commentTo=U1.id AND U1.id=? AND C.isPinned=false AND F.user=U1.id AND F.friend=C.commentFrom AND U2.id=C.commentFrom ORDER BY C.createdAt DESC ";
        return this.jdbcTemplate.query(getQuery,
                (rs, rowNum) -> new CommentWithInfo(
                        rs.getInt("index"),
                        rs.getString("commentTo"),
                        rs.getString("commentFrom"),
                        rs.getString("message"),
                        rs.getBoolean("isPinned"),
                        rs.getBoolean("isHide"),
                        rs.getTimestamp("createdAt"),
                        rs.getString("nickname"),
                        rs.getString("profileImg"),
                        rs.getString("friendName")
                ),
                userID
        );
    }

    public List<CommentWithInfo> getAllPinnedCommentsForUser(String userID){
        String getQuery = "SELECT C.*, U1.nickname, U2.profileImg, F.friendName FROM comment C, user U1, user U2, friend F WHERE C.commentTo=U1.id AND U1.id=? AND C.isPinned=true AND F.user=U1.id AND F.friend=C.commentFrom AND U2.id=C.commentFrom ORDER BY C.createdAt DESC ";
        return this.jdbcTemplate.query(getQuery,
                (rs, rowNum) -> new CommentWithInfo(
                        rs.getInt("index"),
                        rs.getString("commentTo"),
                        rs.getString("commentFrom"),
                        rs.getString("message"),
                        rs.getBoolean("isPinned"),
                        rs.getBoolean("isHide"),
                        rs.getTimestamp("createdAt"),
                        rs.getString("nickname"),
                        rs.getString("profileImg"),
                        rs.getString("friendName")
                ),
                userID
        );
    }

    public int hideComment(int commentIdx, String userID, boolean hide){
        String hideCommentQuery = "UPDATE comment SET `isHide`=? WHERE `index`=? AND commentTo=?";
        Object[] hideCommentParam = new Object[]{hide, commentIdx, userID};
        return this.jdbcTemplate.update(hideCommentQuery, hideCommentParam);
    }

    public List<CommentWithInfo> getAllCommentsExceptHidden(String userID){
        String getCommentsQuery = "SELECT DISTINCT C.*, U1.nickname, U2.profileImg, F.friendName FROM comment C, user U1, friend F, user U2 WHERE C.commentTo=U1.id AND U1.id=? AND C.isHide=false AND F.user=U1.id AND F.friend=C.commentFrom AND U2.id=C.commentFrom ORDER BY C.createdAt DESC;";
        return this.jdbcTemplate.query(getCommentsQuery,
                (rs, rowNum) -> new CommentWithInfo(
                        rs.getInt("index"),
                        rs.getString("commentTo"),
                        rs.getString("commentFrom"),
                        rs.getString("message"),
                        rs.getBoolean("isPinned"),
                        rs.getBoolean("isHide"),
                        rs.getTimestamp("createdAt"),
                        rs.getString("nickname"),
                        rs.getString("profileImg"),
                        rs.getString("friendName")
                ),
                userID
        );
    }

    public int pinComment(int commentIdx, boolean pin){
        String pinCommentQuery = "UPDATE comment SET `isPinned`=? WHERE `index`=?";
        Object[] pinCommentParam = new Object[]{pin, commentIdx};
        return this.jdbcTemplate.update(pinCommentQuery, pinCommentParam);
    }

    public List<CommentWithInfo> getNRecentComments(int count, String userID){
        String getCommentsQuery = "SELECT C.*, U.nickname, U.profileImg, F.friendName FROM comment C, user U, friend F WHERE C.commentTo=U.id AND U.id=? AND C.isHide=false AND C.isPinned=false AND F.user=U.id ORDER BY C.createdAt DESC LIMIT ?";
        Object[] getCommentsParam = new Object[]{userID, count};
        return this.jdbcTemplate.query(getCommentsQuery,
                (rs, rowNum) -> new CommentWithInfo(
                        rs.getInt("index"),
                        rs.getString("commentTo"),
                        rs.getString("commentFrom"),
                        rs.getString("message"),
                        rs.getBoolean("isPinned"),
                        rs.getBoolean("isHide"),
                        rs.getTimestamp("createdAt"),
                        rs.getString("nickname"),
                        rs.getString("profileImg"),
                        rs.getString("friendName")
                ),
                getCommentsParam
        );
    }


    // 초, 분, 시, 일, 월, 주 순서
    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void initTodayHits() throws InterruptedException {
        System.out.println("today hit 초기화");
        // 저장된 모든 관심상품을 조회합니다.

        String editUserQuery = "UPDATE user SET todayHits=? where true";
        this.jdbcTemplate.update(editUserQuery, 0);

        String editUserQuery2 = "UPDATE user SET todayFollows=? where true";
        this.jdbcTemplate.update(editUserQuery2, 0);

        System.out.println("refresh nft 초기화");
        String refreshCollectionQuery = "UPDATE user SET collectionRefresh=? where true";
        this.jdbcTemplate.update(refreshCollectionQuery, 10);

    }

}
