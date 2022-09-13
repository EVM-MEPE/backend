package com.propwave.daotool.user;

import com.propwave.daotool.badge.model.BadgeWallet;
import com.propwave.daotool.user.model.*;
import com.propwave.daotool.utils.GetNFT;
import com.propwave.daotool.user.model.UserWallet;
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

    @Autowired
    private final GetNFT getNFT;

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

    public List<UserWallet> getAllUserByWallet(String walletAddress){
    String getAllUserByWalletQuery = "select * from userWallet where walletAddress=?";
        return this.jdbcTemplate.query(getAllUserByWalletQuery,
                (rs, rowNum) -> new UserWallet(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("walletAddress"),
                        rs.getString("chain"),
                        rs.getTimestamp("createdAt")
                ),
                walletAddress
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

    public int createFriendReq(String reqTo, String reqFrom, String reqNickname){
        String createFriendReqQuery = "INSERT INTO friendReq(reqFrom, reqTo, reqNickname) VALUES(?,?,?)";
        Object[] createFriendReqParam = new Object[]{reqFrom, reqTo, reqNickname};
        return this.jdbcTemplate.update(createFriendReqQuery, createFriendReqParam);
    }

    public int updateFriendReq(String reqTo, String reqFrom){
        String updateFriendReqQuery;
        Object[] updateFriendReqParam;
        //if(accepted){
        updateFriendReqQuery = "UPDATE friendReq SET isAccepted = true WHERE reqFrom=? and reqTo = ?";
//        }else{
//            updateFriendReqQuery = "UPDATE friendReq SET isRejected = true WHERE reqFrom=? and reqTo = ?";
//        }
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

    public int getFriendsCount(String userId){
        String getFriendsCountQuery = "SELECT COUNT(*) FROM friend WHERE user=?";
        return this.jdbcTemplate.queryForObject(getFriendsCountQuery, int.class, userId);
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



    // ------------------------------------------

    public List<UserWallet> getAllUserWalletByUserId(String userId){
        String getUserWalletQuery = "select * from userWallet where user=?";
        return this.jdbcTemplate.query(getUserWalletQuery,
                (rs, rowNum) -> new UserWallet(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("walletAddress"),
                        rs.getString("chain"),
                        rs.getTimestamp("createdAt")
                ),
                userId
        );
    }

    public int isUserWalletExist(String userId, String walletAddress){
        String isUserWalletExistIdQuery = "select exists(select * from userWallet where user=? and walletAddress=?)";
        Object[] isUserWalletExistParam = new Object[]{userId, walletAddress};
        return this.jdbcTemplate.queryForObject(isUserWalletExistIdQuery,
                int.class,
                isUserWalletExistParam
        );
    }

    //지갑 유무 확인
    public int isWalletExist(String walletAddress){
        String walletExistQuery = "select exists(select * from wallet where address = ? )";
        return this.jdbcTemplate.queryForObject(walletExistQuery, int.class, walletAddress);
    }

    public String createWallet(String walletAddress, String walletType){
        String walletCreateQuery = "INSERT INTO wallet(address, walletType) VALUES(?,?)";
        Object[] walletCreateParam = new Object[]{walletAddress, walletType};
        this.jdbcTemplate.update(walletCreateQuery, walletCreateParam);
        return walletAddress;
    }

    public int createUserWallet(String userID, String walletAddress){
        String createUserWalletQuery = "INSERT INTO userWallet(user, walletAddress) VALUES(?,?)";
        Object[] createUserWalletParam = new Object[]{userID, walletAddress};
        return this.jdbcTemplate.update(createUserWalletQuery, createUserWalletParam);
    }

    public int addHit(String userId){
        String modifyUserHitsQuery = "update user set hits = hits + 1, todayHits = todayHits + 1 where id = ?";
        return this.jdbcTemplate.update(modifyUserHitsQuery, userId);

    }


    public int deleteUserWallet(String userId, String walletAddress) {
        String deleteUserWalletQuery = "delete from userWallet where user=? and walletAddress=?";
        Object[] deleteUserWalletParams = new Object[] {userId, walletAddress};
        return this.jdbcTemplate.update(deleteUserWalletQuery, deleteUserWalletParams);
    }

    // AdminRequest 하나 가져오기
    public BadgeRequest getBadgeRequest(int index){
        String getBadgeRequestQuery = "select * from badgeRequest where `index`=?";
        return this.jdbcTemplate.queryForObject(getBadgeRequestQuery,
                (rs, rowNum) -> new BadgeRequest(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("badgeName"),
                        rs.getString("srcWalletAddress"),
                        rs.getString("destWalletAddress"),
                        rs.getBoolean("completed"),
                        rs.getTimestamp("createdAt"),
                        rs.getTimestamp("completedAt")
                ),
                index
        );
    }

    public BadgeWallet getBadgeWallet(int index){
        String getBadgeWalletQuery = "select * from badgeWallet where `index`=?";
        return this.jdbcTemplate.queryForObject(getBadgeWalletQuery,
                (rs, rowNum) -> new BadgeWallet(
                        rs.getInt("index"),
                        rs.getString("walletAddress"),
                        rs.getString("badgeName"),
                        rs.getTimestamp("joinedAt"),
                        rs.getInt("hide")),
                index
        );
    }

    public String getCurrentTime(){
        java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

        return sdf.format(date);
    }

    public WalletInfo getWalletInfo(String walletAddress){
        String getWalletInfoQuery = "select wallet.address, wallet.walletType, walletType.icon " +
                                    "from wallet INNER JOIN walletType ON wallet.walletType=walletType.name " +
                                    "where wallet.address = ?";
        return this.jdbcTemplate.queryForObject(getWalletInfoQuery,
                (rs, rowNum) -> new WalletInfo(
                        rs.getString("address"),
                        rs.getString("walletType"),
                        rs.getString("icon")),
                walletAddress
        );

    }

    // ********************************************************** NFT

    public int isNFTExist(String address, int tokenId){
        String isNFTExistQuery = "select exists(select * from nft where address = ? and tokenId = ?)";
        Object[] isNFTExistParams = new Object[]{address, tokenId};
        return this.jdbcTemplate.queryForObject(isNFTExistQuery,
                int.class,
                isNFTExistParams
        );
    }

    public Nft createNFT(JSONObject result, JSONObject metaJsonObject, String chain){
        System.out.println("chain");
        System.out.println("metaJsonObject\n"+metaJsonObject);
        System.out.println("result\n"+result);
        System.out.println(result.get("token_address") +"    1     "+ result.get("token_id") +"    2    "+ result.get("contract_type") +"    3    "+ result.get("name") +"    4    " + metaJsonObject.get("description") +"    5    "+ metaJsonObject.get("image") +"    6    "+ chain+ metaJsonObject.get("token_uri") +"    7    "+  metaJsonObject.get("dna") +"    8    "+ result.get("is_valid") +"    9    "+ metaJsonObject.get("date") );

        String createNFTQuery = "INSERT INTO nft(`address` ,`tokenId` ,`contractType` ,`name` ,`description` ,`image` ,`chain` ,`tokenUri` ,`dna`,`is_valid`,`date`) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        Object[] createNFTParams = new Object[]{result.get("token_address"), result.get("token_id"), (String) result.get("contract_type"), (String) result.get("name"), (String) metaJsonObject.get("description"), (String) metaJsonObject.get("image"), chain, (String) result.get("token_uri"), (String) metaJsonObject.get("dna"), 1, metaJsonObject.get("date") };
        this.jdbcTemplate.update(createNFTQuery, createNFTParams);
        String lastInsertIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        int lastIdx = this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class);
        System.out.println(lastIdx);
        return getNFT(lastIdx);
    }

    public Nft getNFT(int index){
        String getNftQuery = "select * from nft where `index`=?";
        return this.jdbcTemplate.queryForObject(getNftQuery,
                (rs, rowNum) -> new Nft(
                        rs.getString("address"),
                        rs.getInt("tokenID"),
                        rs.getString("contractType"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("image"),
                        rs.getString("chain"),
                        rs.getString("tokenUri"),
                        rs.getInt("is_valid"),
                        rs.getString("date"),
                        rs.getInt("index")
                ),
                index
        );
    }

    public Nft getNFT(String token_address, int tokenId) {
        String getNftQuery = "select * from nft where `address`=? and `tokenId`=?";
        Object[] getNftParams = new Object[]{token_address, tokenId};
        return this.jdbcTemplate.queryForObject(getNftQuery,
                (rs, rowNum) -> new Nft(
                        rs.getString("address"),
                        rs.getInt("tokenID"),
                        rs.getString("contractType"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("image"),
                        rs.getString("chain"),
                        rs.getString("tokenUri"),
                        rs.getInt("is_valid"),
                        rs.getString("date"),
                        rs.getInt("index")
                ),
                getNftParams
        );
    }

    public int isNFTWalletExist(String address, int tokenId, int userWalletIndex){
        String isNFTWalletExistQuery = "select exists(select * from nftWallet where nftAddress = ? and nftTokenId = ? and userWalletIndex = ?)";
        Object[] isNFTWalletExistParams = new Object[]{address, tokenId, userWalletIndex};
        return this.jdbcTemplate.queryForObject(isNFTWalletExistQuery,
                int.class,
                isNFTWalletExistParams
        );
    }

    public int getRefreshLeft(String userId){
        String getNftRefreshLeftQuery = "select nftRefreshLeft from user where id=?";
        return this.jdbcTemplate.queryForObject(getNftRefreshLeftQuery, int.class, userId);
    }

    public void createNFTWallet(String token_address,int tokenId,int userWalletIndex,int amount){
        String createNFTWalletQuery = "INSERT INTO nftWallet(nftAddress, nftTokenId, userWalletIndex, amount) VALUES(?,?,?,?)";
        Object[] createNFTWalletParams = new Object[]{token_address, tokenId, userWalletIndex, amount};
        this.jdbcTemplate.update(createNFTWalletQuery, createNFTWalletParams);
    }

    public List<NftWallet> getNftWallets(int walletIdx){
        String getNftWaleltsQuery = "select * from nftWallet where userWalletIndex = ?";
        return this.jdbcTemplate.query(getNftWaleltsQuery,
                (rs, rowNum) -> new NftWallet(
                        rs.getInt("index"),
                        rs.getString("nftAddress"),
                        rs.getInt("nftTokenId"),
                        rs.getInt("userWalletIndex"),
                        rs.getInt("amount"),
                        rs.getBoolean("hidden")
                ),
                walletIdx
        );
    }

    public void reduceRefreshNftCount(String userId){
        String reduceRefreshNftCountQuery = "UPDATE user SET nftRefreshLeft=nftRefreshLeft+1 where id = ?";
        this.jdbcTemplate.update(reduceRefreshNftCountQuery, userId);
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

    public int isFollowExist(String reqTo, String reqFrom){
        String isFollowExistQuery = "SELECT EXISTS(SELECT * FROM follow WHERE user=? AND following=?)";
        Object[] isFollowExistParams = new Object[]{reqFrom, reqTo};
        return this.jdbcTemplate.queryForObject(isFollowExistQuery, int.class, isFollowExistParams);
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

    public List<Poap> getAllPoaps(){
        String getAllPoapsQuery = "SELECT * FROM poap";
        return this.jdbcTemplate.query(getAllPoapsQuery,
                (rs, rowNum) -> new Poap(
                        rs.getInt("event_id"),
                        rs.getString("fancy_id"),
                        rs.getString("name"),
                        rs.getString("event_url"),
                        rs.getString("img_url"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("description"),
                        rs.getInt("year"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getDate("expiry_date"),
                        rs.getInt("supply_total")
                )
        );
    }

    public List<PoapWallet> getPoapWalletByWalletAddress(String walletAddress) {
        String getPoapsQuery = "SELECT * FROM poapWallet WHERE walletAddress=?";
        return this.jdbcTemplate.query(getPoapsQuery,
                (rs, rowNum) -> new PoapWallet(
                        rs.getInt("index"),
                        rs.getInt("poap_event_id"),
                        rs.getInt("token_id"),
                        rs.getString("walletAddress"),
                        rs.getInt("supply_order"),
                        rs.getTimestamp("createdAt"),
                        rs.getTimestamp("migratedAt")
                )
        );
    }

    public List<PoapWithDetails> getPoapWithDetailsByWalletAddress(String walletAddress) {
        String getPoapsQuery = "SELECT P.*, W.index, W.token_id, W.walletAddress, W.supply_order, W.createdAt, W.migratedAt, U.index, U.user, U.isHide" +
                                "FROM poapWallet W" +
                                "INNER JOIN poap P ON W.poap_event_id = P.event_id" +
                                "INNER JOIN userWalletPoap U ON W.token_id = U.token_id"+
                                "WHERE W.walletAddress=? ORDER BY `createdAt` DESC";
        return this.jdbcTemplate.query(getPoapsQuery,
                (rs, rowNum) -> new PoapWithDetails(
                        rs.getInt("event_id"),
                        rs.getString("fancy_id"),
                        rs.getString("name"),
                        rs.getString("event_url"),
                        rs.getString("img_url"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("description"),
                        rs.getInt("year"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getDate("expiry_date"),
                        rs.getInt("supply_total"),
                        rs.getInt("poapWalletIndex"),
                        rs.getInt("token_id"),
                        rs.getString("walletAddress"),
                        rs.getInt("supply_order"),
                        rs.getTimestamp("createdAt"),
                        rs.getTimestamp("migratedAt"),
                        rs.getInt("userWalletPoapIndex"),
                        rs.getString("user"),
                        rs.getBoolean("isHide")
                )
        );
    }

    public Poap createPoap(Map<Object, Object> event){
        String createPoapQuery = "INSERT INTO poap(event_id, fancy_id, name, event_url, image_url, country, city, description, year, start_date, end_date, expiry_date, supply_total) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Object[] createPoapParams = new Object[] {event.get("id"), event.get("fancy_id"), event.get("name"), event.get("event_url"), event.get("image_url"), event.get("country"), event.get("city"), event.get("description"), event.get("year"), event.get("start_date"), event.get("end_date"), event.get("expiry_date"), event.get("supply")};
        this.jdbcTemplate.update(createPoapQuery, createPoapParams);
        return getPoap((int)event.get("id"));
    }

    public Poap getPoap(int event_id){
        String getPoapQuery = "SELECT * FROM poap WHERE `event_id`=?";
        return this.jdbcTemplate.queryForObject(getPoapQuery,
                (rs, rowNum) -> new Poap(
                        rs.getInt("event_id"),
                        rs.getString("fancy_id"),
                        rs.getString("name"),
                        rs.getString("event_url"),
                        rs.getString("img_url"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("description"),
                        rs.getInt("year"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getDate("expiry_date"),
                        rs.getInt("supply_total")
                ),
                event_id
        );
    }

    public int createPoapWallet(int event_id, int token_id, String walletAddress, Timestamp createdAt, Timestamp modifiedAt){
        String createPoapWalletQuery = "INSERT INTO poapWallet(poap_event_id, token_id, walletAddress, createdAt, modifiedAt) VALUES(?,?,?,?,?)";
        Object[] createPoapWalletParams = new Object[] {event_id, token_id, walletAddress, createdAt, modifiedAt};
        return this.jdbcTemplate.update(createPoapWalletQuery, createPoapWalletParams);
    }

    public int createPoapWallet(int event_id, int token_id, String walletAddress, Timestamp createdAt){
        String createPoapWalletQuery = "INSERT INTO poapWallet(poap_event_id, token_id, walletAddress, createdAt) VALUES(?,?,?,?)";
        Object[] createPoapWalletParams = new Object[] {event_id, token_id, walletAddress, createdAt};
        return this.jdbcTemplate.update(createPoapWalletQuery, createPoapWalletParams);
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

    public int addFollow(String userID){
        String addFollowQuery = "UPDATE user SET todayFollows=todayFollows+1 where id = ?";
        return this.jdbcTemplate.update(addFollowQuery, userID);
    }

    public int reduceFollow(String userID){
        String reduceFollowQuery = "UPDATE user SET todayFollows=todayFollows-1 where id = ?";
        return this.jdbcTemplate.update(reduceFollowQuery, userID);
    }

    public String getFriendNickname(String userID, String friendID){
        String getFriendNicknameQuery = "SELECT friendName FROM friend WHERE user=? AND friend=?";
        Object[] getFriendNicknameParams = new Object[]{userID, friendID};
        return this.jdbcTemplate.queryForObject(getFriendNicknameQuery, String.class, getFriendNicknameParams);
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
        String getQuery = "SELECT C.*, U.nickname, U.profileImg, F.friendName FROM comment C, user U, friend F WHERE C.commentTo=U.id AND U.id=? AND C.isPinned=false AND F.user=U.id ORDER BY C.createdAt DESC ";
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
        String getQuery = "SELECT C.*, U.nickname, U.profileImg, F.friendName FROM comment C, user U, friend F WHERE C.commentTo=U.id AND U.id=? AND C.isPinned=true AND F.user=U.id ORDER BY C.createdAt DESC ";
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


    public String getWalletChain(String walletAddress){
        String getWalletChainQuery = "SELECT walletType FROM wallet WHERE address=?";
        return this.jdbcTemplate.queryForObject(getWalletChainQuery, String.class, walletAddress);
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
