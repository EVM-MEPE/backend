package com.propwave.daotool.user;

import com.propwave.daotool.badge.model.Badge;
import com.propwave.daotool.badge.model.BadgeJoinedAt;
import com.propwave.daotool.badge.model.BadgeNameImage;
import com.propwave.daotool.badge.model.BadgeWallet;
import com.propwave.daotool.user.model.*;
import com.propwave.daotool.utils.GetNFT;
import com.propwave.daotool.wallet.model.UserWallet;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

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
        return getUserAllInfo((String)userInfo.get("id"));
    }

    public int checkUserIdExist(String userId){
        String checkUserIdExistQuery = "select exists(select * from user where id = ?)";
        return this.jdbcTemplate.queryForObject(checkUserIdExistQuery,
                int.class,
                userId
        );
    }

    public User editUser(Map<String, String> userInfo){
        String editUserQuery = "UPDATE user SET id=?, profileImage=?, introduction=?, url=? WHERE id = ?";
        Object[] editUserParams = new Object[]{userInfo.get("changedId"), userInfo.get("profileImage"), userInfo.get("introduction"), userInfo.get("url"), userInfo.get("preId")};
        this.jdbcTemplate.update(editUserQuery, editUserParams);
        return getUserAllInfo(userInfo.get("changedId"));
    }

    public int deleteUser(String userId){
        String deleteUserQuery = "delete from user where id=?";
        return this.jdbcTemplate.update(deleteUserQuery, userId);
    }

    public User getUserAllInfo(String id){
        String getUserQuery = "select * from user where id=?";
        return this.jdbcTemplate.queryForObject(getUserQuery,
                (rs, rowNum) -> new User(
                        rs.getString("id"),
                        rs.getString("profileImage"),
                        rs.getString("introduction"),
                        rs.getString("url"),
                        rs.getInt("hits"),
                        rs.getInt("todayHits"),
                        rs.getTimestamp("createdAt"),
                        rs.getInt("nftRefreshLeft")
                ),
                id
        );
    }

    //user의 프로필 이미지 주소 가져오기
    public String getUserImagePath(String userId){
        String getUserImageQuery = "select profileImage from user where id = ?";
        return this.jdbcTemplate.queryForObject(getUserImageQuery, String.class, userId);
    }

    public List<UserWallet> getAllUserWalletByWalletId(String walletAddress){
        // userWallet에서 지갑 주소에 해당하는 record 다 가져오기
        String getUserWalletQuery = "select * from userWallet where walletAddress=?";
        return this.jdbcTemplate.query(getUserWalletQuery,
                (rs, rowNum) -> new UserWallet(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("walletAddress"),
                        rs.getBoolean("loginAvailable"),
                        rs.getBoolean("viewDataAvailable"),
                        rs.getString("walletName"),
                        rs.getTimestamp("createdAt"),
                        rs.getString("chain")
                ),
                walletAddress
        );
    }

    public List<UserWallet> getAllUserWalletByUserId(String userId){
        String getUserWalletQuery = "select * from userWallet where user=?";
        return this.jdbcTemplate.query(getUserWalletQuery,
                (rs, rowNum) -> new UserWallet(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("walletAddress"),
                        rs.getBoolean("loginAvailable"),
                        rs.getBoolean("viewDataAvailable"),
                        rs.getString("walletName"),
                        rs.getTimestamp("createdAt"),
                        rs.getString("chain")
                ),
                userId
        );
    }

    public UserWallet getUserWalletByIndex(int index){
        String getUserWalletByWalletAddressAndUserIdQuery = "select * from userWallet where `index`=?";
        return this.jdbcTemplate.queryForObject(getUserWalletByWalletAddressAndUserIdQuery,
                (rs, rowNum) -> new UserWallet(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("walletAddress"),
                        rs.getBoolean("loginAvailable"),
                        rs.getBoolean("viewDataAvailable"),
                        rs.getString("walletName"),
                        rs.getTimestamp("createdAt"),
                        rs.getString("chain")
                ),
                index
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
                        rs.getTimestamp("createdAt"),
                        rs.getString("chain")
                ),
                getUserWalletByWalletAddressAndUserIdParam
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

    public int isUserWalletExist(String walletAddress){
        String isUserWalletExistQuery = "select exists(select * from userWallet where walletAddress=?)";
        return this.jdbcTemplate.queryForObject(isUserWalletExistQuery,
                int.class,
                walletAddress
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
        return this.jdbcTemplate.queryForObject(walletExistQuery, int.class, walletAddress);
    }

    //지갑이 로그인용으로 있는지 유무 확인
    public int isWalletExistForLogin(String walletAddress){
        String isWalletExistForLoginQuery = "select exists(select * from userWallet where walletAddress = ? AND loginAvailable=1)";
        return this.jdbcTemplate.queryForObject(isWalletExistForLoginQuery, int.class, walletAddress);
    }

    public int isWalletExistForLogin(String userId, String walletAddress){
        String isWalletExistForLoginQuery = "select exists(select * from userWallet where walletAddress = ? AND loginAvailable=1 AND user=?)";
        Object[] isWalletExistForLoginParam = new Object[]{walletAddress, userId};
        return this.jdbcTemplate.queryForObject(isWalletExistForLoginQuery, int.class, isWalletExistForLoginParam);
    }

    public int isWalletExistForLoginNotMe(String userId, String walletAddress){
        String isWalletExistForLoginQuery = "select exists(select * from userWallet where walletAddress = ? AND loginAvailable=1 AND NOT user=?)";
        Object[] isWalletExistForLoginParam = new Object[]{walletAddress, userId};
        return this.jdbcTemplate.queryForObject(isWalletExistForLoginQuery, int.class, isWalletExistForLoginParam);
    }


    public String createWallet(String walletAddress, String walletType){
        String walletCreateQuery = "INSERT INTO wallet(address, walletType) VALUES(?,?)";
        Object[] walletCreateParam = new Object[]{walletAddress, walletType};
        this.jdbcTemplate.update(walletCreateQuery, walletCreateParam);
        return walletAddress;
    }

    public String createUserWallet(Map<String, Object> wallet, String userId){
        String createUserWalletQuery = "INSERT INTO userWallet(user, walletAddress, walletName, loginAvailable, viewDataAvailable, chain) VALUES(?,?,?,?,?,?)";
        if(!wallet.containsKey("walletChain")){
            wallet.put("walletChain", "default");
        }
        System.out.println(wallet);
        System.out.println(userId);
        Object[] createUserWalletParam = new Object[]{userId, wallet.get("walletAddress"), wallet.get("walletName"), wallet.get("loginAvailable"), wallet.get("viewDataAvailable"), wallet.get("walletChain")};
        this.jdbcTemplate.update(createUserWalletQuery, createUserWalletParam);
        return (String)wallet.get("walletAddress");
    }

    public String createUserWalletForLogin(Map<String, Object> wallet, String userId){
        String createUserWalletQuery = "INSERT INTO userWallet(user, walletAddress, walletName, loginAvailable, viewDataAvailable, chain) VALUES(?,?,?,?,?,?)";
        System.out.println(userId+"         "+ wallet.get("walletAddress")+"         "+  wallet.get("walletName")+"         ");
        Object[] createUserWalletParam = new Object[]{userId, wallet.get("walletAddress"), wallet.get("walletName"), wallet.get("loginAvailable"), wallet.get("viewDataAvailable"), wallet.get("chain")};
        this.jdbcTemplate.update(createUserWalletQuery, createUserWalletParam);
        return (String)wallet.get("walletAddress");
    }

    public String createUserWalletForDashBoard(Map<String, Object> wallet, String userId){
        String createUserWalletQuery = "INSERT INTO userWallet(user, walletAddress, walletName, loginAvailable, viewDataAvailable, chain) VALUES(?,?,?,?,?,?)";
        Object[] createUserWalletParam = new Object[]{userId, wallet.get("walletAddress"), wallet.get("walletName"), 0, 1, wallet.get("walletChain")};
        this.jdbcTemplate.update(createUserWalletQuery, createUserWalletParam);
        return (String)wallet.get("walletAddress");
    }


//    public String createUserWallet(WalletSignupReq wallet, String userId){
//        String createUserWalletQuery = "INSERT INTO userWallet(user, walletAddress, walletName, walletIcon, loginAvailable, viewDataAvailable) VALUES(?,?,?,?,?,?)";
//        Object[] createUserWalletParam = new Object[]{userId, wallet.getWalletAddress(), wallet.getWalletName(), wallet.getWalletIcon(), wallet.getLoginAvailable(), wallet.getViewDataAvailable()};
//        this.jdbcTemplate.update(createUserWalletQuery, createUserWalletParam);
//        return (String)wallet.getWalletAddress();
//    }

    public String createUserWallet(Map<String, Object> wallet){
        String createUserWalletQuery = "INSERT INTO userWallet(user, walletAddress, walletName, loginAvailable, viewDataAvailable, chain) VALUES(?,?,?,?,?,?)";
        Object[] createUserWalletParam = new Object[]{wallet.get("user"), wallet.get("walletAddress"), wallet.get("walletName"), wallet.get("loginAvailable"), wallet.get("viewDataAvailable"), wallet.get("walletChain")};
        this.jdbcTemplate.update(createUserWalletQuery, createUserWalletParam);
        return (String)wallet.get("address");
    }

    public int editUserWallet(Map<String, Object> wallet){
        String editUserWalletQuery = "UPDATE userWallet SET walletName = ?, chain = ?, walletAddress=? WHERE `index`=?";
        Object[] editUserWalletParams = new Object[] {wallet.get("walletName"), wallet.get("walletChain"), wallet.get("walletAddress"), wallet.get("walletIndex")};
        return this.jdbcTemplate.update(editUserWalletQuery, editUserWalletParams);
    }


    public List<BadgeWallet> getAllBadgeWallet(String walletAddress){
        String getAllBadgeQuery = "select * from badgeWallet where walletAddress=?";
        return this.jdbcTemplate.query(getAllBadgeQuery,
                (rs, rowNum) -> new BadgeWallet(
                        rs.getInt("index"),
                        rs.getString("walletAddress"),
                        rs.getString("badgeName"),
                        rs.getTimestamp("joinedAt"),
                        rs.getInt("hide")),
                walletAddress
        );
    }

    public List<BadgeWallet> getBadgeWalletByBadgeName(String BadgeName){
        String getBadgeWalletQuery = "select * from badgeWallet where badgeName=? ";
        return this.jdbcTemplate.query(getBadgeWalletQuery,
                (rs, rowNum) -> new BadgeWallet(
                        rs.getInt("index"),
                        rs.getString("walletAddress"),
                        rs.getString("badgeName"),
                        rs.getTimestamp("joinedAt"),
                        rs.getInt("hide")),
                BadgeName
        );
    }

    public Badge getBadge(String badgeName){
        String getBadgeQuery = "select * from badge where name=?";
        return this.jdbcTemplate.queryForObject(getBadgeQuery,
                (rs, rowNum) -> new Badge(
                        rs.getString("name"),
                        rs.getString("image"),
                        rs.getString("explanation"),
                        rs.getTimestamp("createdAt"),
                        rs.getString("chain"),
                        rs.getInt("index")
                ),
                badgeName
        );
    }

    public int addHit(String userId){
        String modifyUserHitsQuery = "update user set hits = hits + 1, todayHits = todayHits + 1 where id = ?";
        return this.jdbcTemplate.update(modifyUserHitsQuery, userId);

    }

    //뱃지 join 날짜
    public List<BadgeJoinedAt> getBadgeJoinedAt(String walletAddress){
        String getBadgeJoinedAtQuery = "select badgeName, joinedAt from badgeWallet where walletAddress=?";
        return this.jdbcTemplate.query(getBadgeJoinedAtQuery,
                (rs, rowNum) -> new BadgeJoinedAt(
                        rs.getString("badgeName"),
                        rs.getTimestamp("joinedAt")
                ),
                walletAddress
        );
    }

    public int checkUser(String userId){
        String getBadgeJoinedAtQuery = "select exists(select * from user where id=?)";
        return this.jdbcTemplate.queryForObject(getBadgeJoinedAtQuery,
                int.class,
                userId);
    }

    // 뱃지 이름, 사진 가져오기
    public BadgeNameImage getBadgeNameImage(String badgeName){
        String getBadgeNameImageQuery = "select name, image from badge where name=?";
        return this.jdbcTemplate.queryForObject(getBadgeNameImageQuery,
                (rs, rowNum) -> new BadgeNameImage(
                        rs.getString("name"),
                        rs.getString("image")
                ),
                badgeName
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

    public int deleteUserWallet(int index) {
        String deleteUserWalletQuery = "delete from userWallet where `index`=?";
        return this.jdbcTemplate.update(deleteUserWalletQuery, index);
    }

    public int deleteWallet(String walletAddress){
        String deleteWalletQuery = "delete from wallet where address=?";
        return this.jdbcTemplate.update(deleteWalletQuery, walletAddress);
    }


    public int makeViewDataAvailable(String userId, String walletAddress) {
        String makeViewDataAvailableQuery = "update userWallet set viewDataAvailable = true where user=? and walletAddress=?";
        Object[] makeViewDataAvailableParams = new Object[] {userId, walletAddress};
        return this.jdbcTemplate.update(makeViewDataAvailableQuery, makeViewDataAvailableParams);
    }

    public int makeViewDataUnavailable(String userId, String walletAddress) {
        String makeViewDataUnavailableQuery = "update userWallet set viewDataAvailable = false where user=? and walletAddress=?";
        Object[] makeViewDataUnavailableParams = new Object[] {userId, walletAddress};
        return this.jdbcTemplate.update(makeViewDataUnavailableQuery, makeViewDataUnavailableParams);
    }

    public int makeViewDataUnavailable(int walletIndex) {
        String makeViewDataUnavailableQuery = "update userWallet set viewDataAvailable = false where `index`=?";
        return this.jdbcTemplate.update(makeViewDataUnavailableQuery, walletIndex);
    }

    // AdminReqeust 생성하기
    public BadgeRequest createBadgeRequest(Map<String, String> request){
        String createBadgeRequestQuery = "INSERT INTO badgeRequest(user, badgeName, srcWalletAddress, destWalletAddress) VALUES(?,?,?,?)";
        Object[] createBadgeRequestParams = new Object[]{request.get("user"), request.get("badgeName"), request.get("srcWalletAddress"), request.get("dstWalletAddress")};
        this.jdbcTemplate.update(createBadgeRequestQuery, createBadgeRequestParams);
        String lastInserIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        int lastInsertId = this.jdbcTemplate.queryForObject(lastInserIdQuery, int.class);
        return getBadgeRequest(lastInsertId); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 userIdx번호를 반환한다.
    }

    public BadgeRequest updateBadgeRequest(int index){
        String updateBadgeRequestQuery = "UPDATE badgeRequest SET completed = true, completedAt = ? WHERE `index` = ?";
        Object[] updateBadgeRequestParams = new Object[] {new Timestamp(System.currentTimeMillis()), index};
        this.jdbcTemplate.update(updateBadgeRequestQuery, updateBadgeRequestParams);
        return getBadgeRequest(index);
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

    public List<BadgeRequest> getAllBadgeRequest(){
        String getBadgeRequestQuery = "select * from badgeRequest";
        return this.jdbcTemplate.query(getBadgeRequestQuery,
                (rs, rowNum) -> new BadgeRequest(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("badgeName"),
                        rs.getString("srcWalletAddress"),
                        rs.getString("destWalletAddress"),
                        rs.getBoolean("completed"),
                        rs.getTimestamp("createdAt"),
                        rs.getTimestamp("completedAt")
                )
        );
    }

    public BadgeWallet createBadgeWallet(String destWalletAddress, String BadgeName){
        String createBadgeWalletQuery = "INSERT INTO badgeWallet(walletAddress, badgeName) VALUES(?,?)";
        Object[] createBadgeWalletParams = new Object[]{destWalletAddress, BadgeName};
        this.jdbcTemplate.update(createBadgeWalletQuery, createBadgeWalletParams);
        String lastInserIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        int lastInsertId = this.jdbcTemplate.queryForObject(lastInserIdQuery, int.class);
        return getBadgeWallet(lastInsertId); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 userIdx번호를 반환한다.
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

    public int checkBadge(String badgeName){
        String checkBadgeQuery = "select exists(select * from badge where name = ?)";
        return this.jdbcTemplate.queryForObject(checkBadgeQuery,
                int.class,
                badgeName
        );
    }

    public String getCurrentTime(){
        java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

        return sdf.format(date);
    }

    public Chain getChain(String chainName){
        String getChainQuery = "select * from chain where `name`=?";
        return this.jdbcTemplate.queryForObject(getChainQuery,
                (rs, rowNum) -> new Chain(
                        rs.getString("name"),
                        rs.getString("image"),
                        rs.getInt("index")),
                chainName
        );
    }

    public List<BadgeTarget> getBadgeTarget(String badgeName){
        String getBadgeTargetQuery = "select * from badgeTarget where `badgeName`=?";
        return this.jdbcTemplate.query(getBadgeTargetQuery,
                (rs, rowNum) -> new BadgeTarget(
                        rs.getInt("index"),
                        rs.getString("badgeName"),
                        rs.getInt("targetIdx")),
                badgeName
        );
    }

    public Target getTarget(int targetIndex){
        String getTargetQuery = "select * from target where `index`=?";
        return this.jdbcTemplate.queryForObject(getTargetQuery,
                (rs, rowNum) -> new Target(
                        rs.getInt("index"),
                        rs.getString("target")),
                targetIndex
        );
    }

    public Chain getChainInfo(String chain){
        String getChainInfoQuery = "select * from chain where name=?";
        return this.jdbcTemplate.queryForObject(getChainInfoQuery,
                (rs, rowNum) -> new Chain(
                        rs.getString("name"),
                        rs.getString("image"),
                        rs.getInt("index")),
                chain
        );
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

    // 초, 분, 시, 일, 월, 주 순서
    @Scheduled(cron = "0 0 0 * * *")
    public void initTodayHits() throws InterruptedException {
        System.out.println("today hit 초기화");
        // 저장된 모든 관심상품을 조회합니다.

        String editUserQuery = "UPDATE user SET todayHits=? where true";
        this.jdbcTemplate.update(editUserQuery, 0);

        System.out.println("refresh nft 초기화");
        String refreshCollectionQuery = "UPDATE user SET collectionRefresh=? where true";
        this.jdbcTemplate.update(refreshCollectionQuery, 10);

    }

    public List<UserWallet> getAllUserWalletForDashBoardByUserId(String userId){
        String getUserWallet = "select * from userWallet where user=? and viewDataAvailable=1";
        return this.jdbcTemplate.query(getUserWallet,
                (rs, rowNum) -> new UserWallet(
                        rs.getInt("index"),
                        rs.getString("user"),
                        rs.getString("walletAddress"),
                        rs.getBoolean("loginAvailable"),
                        rs.getBoolean("viewDataAvailable"),
                        rs.getString("walletName"),
                        rs.getTimestamp("createdAt"),
                        rs.getString("chain")
                ),
                userId
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
                        rs.getString("dna"),
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
                        rs.getString("dna"),
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

    public void createNFTWallet(String token_address,int tokenId,int userWalletIndex,int amount,int nftIndex){
        String createNFTWalletQuery = "INSERT INTO nftWallet(nftAddress, nftTokenId, userWalletIndex, amount, nftIndex) VALUES(?,?,?,?,?)";
        Object[] createNFTWalletParams = new Object[]{token_address, tokenId, userWalletIndex, amount, nftIndex};
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
                        rs.getInt("nftIndex")
                ),
                walletIdx
        );
    }

    public void reduceRefreshNftCount(String userId){
        String reduceRefreshNftCountQuery = "UPDATE user SET nftRefreshLeft=nftRefreshLeft+1 where id = ?";
        this.jdbcTemplate.update(reduceRefreshNftCountQuery, userId);
    }

}
