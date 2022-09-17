package com.propwave.daotool.Friend;

import com.propwave.daotool.Friend.model.Follow;
import com.propwave.daotool.Friend.model.Friend;
import com.propwave.daotool.Friend.model.FriendReq;
import com.propwave.daotool.Friend.model.FriendWithFriendImg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class FriendDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){this.jdbcTemplate = new JdbcTemplate(dataSource);}

    public int createFriendReq(String reqTo, String reqFrom, String reqNickname){
        String createFriendReqQuery = "INSERT INTO friendReq(reqFrom, reqTo, reqNickname) VALUES(?,?,?)";
        Object[] createFriendReqParam = new Object[]{reqFrom, reqTo, reqNickname};
        return this.jdbcTemplate.update(createFriendReqQuery, createFriendReqParam);
    }

    public Friend getFriend(String user, String friend){
        String getFriendQuery = "SELECT * FROM friend WHERE user=? and friend=?";
        Object[] getFriendParam = new Object[]{user, friend};
        return this.jdbcTemplate.queryForObject(getFriendQuery,
                (rs, rowNum) -> new Friend(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("friend"),
                        rs.getString("friendName"),
                        rs.getTimestamp("createdAt")
                ),
                getFriendParam
        );
    }

    public Friend getFriend(int index){
        String getFriendQuery = "SELECT * FROM friend WHERE `index`=?";
        return this.jdbcTemplate.queryForObject(getFriendQuery,
                (rs, rowNum) -> new Friend(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("friend"),
                        rs.getString("friendName"),
                        rs.getTimestamp("createdAt")
                ),
                index
        );
    }

    public FriendReq getFriendReq(String reqTo, String reqFrom){
        System.out.println(reqTo + reqFrom);
        String getFriendReqQuery = "SELECT * FROM friendReq WHERE reqFrom=? AND reqTo=? ORDER BY `index` DESC LIMIT 1";
        Object[] getFriendReqParam = new Object[]{reqFrom, reqTo};
        return this.jdbcTemplate.queryForObject(getFriendReqQuery,
                (rs, rowNum) -> new FriendReq(
                        rs.getInt("index"),
                        rs.getString("reqFrom"),
                        rs.getString("reqTo"),
                        rs.getString("reqNickname"),
                        rs.getBoolean("isAccepted"),
                        rs.getBoolean("isRejected"),
                        rs.getTimestamp("createdAt")
                )
                , getFriendReqParam);

    }

    public FriendReq getFriendReq(int index){
        String getFriendReqQuery = "SELECT * FROM friendReq WHERE `index`=?";
        return this.jdbcTemplate.queryForObject(getFriendReqQuery,
                (rs, rowNum) -> new FriendReq(
                        rs.getInt("index"),
                        rs.getString("reqFrom"),
                        rs.getString("reqTo"),
                        rs.getString("reqNickname"),
                        rs.getBoolean("isAccepted"),
                        rs.getBoolean("isRejected"),
                        rs.getTimestamp("createdAt")
                ),
                index
        );
    }

    public int updateFriendReq(String reqTo, String reqFrom){
        String updateFriendReqQuery;
        Object[] updateFriendReqParam;
        updateFriendReqQuery = "UPDATE friendReq SET isAccepted = true WHERE reqFrom=? and reqTo = ?";
        updateFriendReqParam = new Object[]{reqFrom, reqTo};
        return this.jdbcTemplate.update(updateFriendReqQuery, updateFriendReqParam);
    }

    // user1에게 user2라는 친구가 nickname 이라는 닉네임으로 생김
    public int createFriend(String user1, String user2, String nickname){
        String createFriendQuery = "INSERT INTO friend(user, friend, friendName) VALUES(?,?,?)";
        Object[] createFriendParam = new Object[]{user1, user2, nickname};
        return this.jdbcTemplate.update(createFriendQuery, createFriendParam);
    }

    public String getFriendReqNickname(String reqFrom, String reqTo){
        String getFriendReqNicknameQuery = "SELECT reqNickname FROM friendReq WHERE reqFrom=? and reqTo=?";
        Object[] getFriendReqNicknameParam = new Object[]{reqFrom, reqTo};
        return this.jdbcTemplate.queryForObject(getFriendReqNicknameQuery, String.class, getFriendReqNicknameParam);
    }

    public int deleteFriendReq(String reqFrom, String reqTo){
        String deleteFriendReqQuery = "delete from friendReq where reqFrom = ? and reqTo = ?";
        Object[] deleteFriendReqParams = new Object[] {reqFrom, reqTo};
        return this.jdbcTemplate.update(deleteFriendReqQuery, deleteFriendReqParams);
    }

    public Friend editFriendNickname(String user, String friend, String newNickname){
        String editFriendNicknameQuery = "UPDATE friend SET friendName=? WHERE user=? and friend=?";
        Object[] editFriendNicknameParam = new Object[]{newNickname, user, friend};
        this.jdbcTemplate.update(editFriendNicknameQuery, editFriendNicknameParam);
        return getFriend(user, friend);
    }

    public int createFollow(String reqTo, String reqFrom){
        String createFollowQuery = "INSERT INTO follow(user, following) VALUES(?,?)";
        Object[] createFollowParams = new Object[]{reqFrom, reqTo};
        return this.jdbcTemplate.update(createFollowQuery, createFollowParams);
    }

    public int deleteFollow(String reqTo, String reqFrom){
        String deleteFollowQuery = "DELETE FROM follow WHERE user=? AND following=?";
        Object[] deleteFollowParams = new Object[]{reqFrom, reqTo};
        return this.jdbcTemplate.update(deleteFollowQuery, deleteFollowParams);
    }

    public int isFollowExist(String reqTo, String reqFrom){
        String isFollowExistQuery = "SELECT EXISTS(SELECT * FROM follow WHERE user=? AND following=?)";
        Object[] isFollowExistParams = new Object[]{reqFrom, reqTo};
        return this.jdbcTemplate.queryForObject(isFollowExistQuery, int.class, isFollowExistParams);
    }

    public int getFriendsCount(String userId){
        String getFriendsCountQuery = "SELECT COUNT(*) FROM friend WHERE user=?";
        return this.jdbcTemplate.queryForObject(getFriendsCountQuery, int.class, userId);
    }

    public List<FriendWithFriendImg> getAllFriendsWithFriendImg(String userID){
        String getAllFriendsWithFriendImgQuery = "SELECT A.*, B.profileImg FROM friend A, user B WHERE B.id = A.friend AND A.user=?";
        return this.jdbcTemplate.query(getAllFriendsWithFriendImgQuery,
                (rs, rowNum) -> new FriendWithFriendImg(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("friend"),
                        rs.getString("profileImg"),
                        rs.getString("friendName"),
                        rs.getTimestamp("createdAt")
                ),
                userID
        );
    }

    public String getFriendNickname(String userID, String friendID){
        String getFriendNicknameQuery = "SELECT friendName FROM friend WHERE user=? AND friend=?";
        Object[] getFriendNicknameParams = new Object[]{userID, friendID};
        return this.jdbcTemplate.queryForObject(getFriendNicknameQuery, String.class, getFriendNicknameParams);
    }




    public Follow getFollow(String reqTo, String reqFrom){
        String getFollowingQuery = "SELECT * FROM follow WHERE user=? AND following=?";
        Object[] getFollowingParam = new Object[]{reqFrom, reqTo};
        return this.jdbcTemplate.queryForObject(getFollowingQuery,
                (rs, rowNum) -> new Follow(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("following"),
                        rs.getTimestamp("createdAt")
                ),
                getFollowingParam
        );
    }

    public Follow getFollow(int index){
        String getFollow = "SELECT * FROM follow WHERE `index`=?";
        return this.jdbcTemplate.queryForObject(getFollow,
                (rs, rowNum) -> new Follow(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("following"),
                        rs.getTimestamp("createdAt")
                ),
                index
        );
    }

    public List<Follow> getFollowingList(String userID){
        String getFollowingList = "SELECT * FROM follow WHERE user=?";
        return this.jdbcTemplate.query(getFollowingList,
                (rs, rowNum) -> new Follow(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("following"),
                        rs.getTimestamp("createdAt")
                ),
                userID
        );
    }

    public List<Follow> getFollowerList(String userID){
        String getFollowingList = "SELECT * FROM follow WHERE following=?";
        return this.jdbcTemplate.query(getFollowingList,
                (rs, rowNum) -> new Follow(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("following"),
                        rs.getTimestamp("createdAt")
                ),
                userID
        );
    }

    public int getFollowerCount(String userID){
        String getFollowerCountQuery = "SELECT COUNT(*) FROM follow WHERE following=?";
        return this.jdbcTemplate.queryForObject(getFollowerCountQuery, int.class, userID);
    }

    public int getFollowingCount(String userID){
        String getFollowingCountQuery = "SELECT COUNT(*) FROM follow WHERE user=?";
        return this.jdbcTemplate.queryForObject(getFollowingCountQuery, int.class, userID);
    }

    public int addFollow(String userID){
        String addFollowQuery = "UPDATE user SET todayFollows=todayFollows+1 where id = ?";
        return this.jdbcTemplate.update(addFollowQuery, userID);
    }

    public int reduceFollow(String userID){
        String reduceFollowQuery = "UPDATE user SET todayFollows=todayFollows-1 where id = ?";
        return this.jdbcTemplate.update(reduceFollowQuery, userID);
    }
}


