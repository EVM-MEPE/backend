package com.propwave.daotool.wallet;

import com.propwave.daotool.badge.model.BadgeWallet;
import com.propwave.daotool.user.model.UserWallet;
import com.propwave.daotool.wallet.model.*;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Repository
public class WalletDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){this.jdbcTemplate = new JdbcTemplate(dataSource);}



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

    public String getWalletChain(String walletAddress){
        String getWalletChainQuery = "SELECT walletType FROM wallet WHERE address=?";
        return this.jdbcTemplate.queryForObject(getWalletChainQuery, String.class, walletAddress);
    }

    public Transaction getTransaction(int trxIdx){
        String getTransactionQuery = "SELECT * FROM transaction WHERE `index`=?";
        return this.jdbcTemplate.queryForObject(getTransactionQuery,
                (rs, rowNum) -> new Transaction(
                        rs.getInt("index"),
                        rs.getString("toWalletAddress"),
                        rs.getString("fromWalletAddress"),
                        rs.getString("toUser"),
                        rs.getString("fromUser"),
                        rs.getString("gasPrice"),
                        rs.getString("gas"),
                        rs.getString("value"),
                        rs.getString("chainID"),
                        rs.getString("memo"),
                        rs.getString("udenom"),
                        rs.getString("walletType"),
                        rs.getString("txHash"),
                        rs.getTimestamp("createdAt")
                ),
                trxIdx
        );
    }

    public List<Transaction> getAllTransaction(String userID){
        String getTransactionQuery = "SELECT * FROM transaction WHERE toUser=? OR fromUser=?";
        Object[] getTransactionParam = new Object[] {userID, userID};
        return this.jdbcTemplate.query(getTransactionQuery,
                (rs, rowNum) -> new Transaction(
                        rs.getInt("index"),
                        rs.getString("toWalletAddress"),
                        rs.getString("fromWalletAddress"),
                        rs.getString("toUser"),
                        rs.getString("fromUser"),
                        rs.getString("gasPrice"),
                        rs.getString("gas"),
                        rs.getString("value"),
                        rs.getString("chainID"),
                        rs.getString("memo"),
                        rs.getString("udenom"),
                        rs.getString("walletType"),
                        rs.getString("txHash"),
                        rs.getTimestamp("createdAt")
                ),
                getTransactionParam
        );
    }

    public TokenReq getTokenReq(int tokenReqIdx){
        String getTokenReqQuery = "SELECT * FROM tokenReq WHERE `index`=?";
        return this.jdbcTemplate.queryForObject(getTokenReqQuery,
                (rs, rowNum) -> new TokenReq(
                        rs.getInt("index"),
                        rs.getString("reqWalletAddress"),
                        rs.getInt("reqTokenAmount"),
                        rs.getString("toUser"),
                        rs.getString("fromUser"),
                        rs.getString("chainID"),
                        rs.getString("walletType"),
                        rs.getString("memo"),
                        rs.getTimestamp("createdAt")
                ),
                tokenReqIdx
        );
    }

    public int saveRemit(Map<String, String> remitRes){
        String creatsaveRemitQuery = "INSERT INTO transaction(toWalletAddress, fromWalletAddress, toUser, fromUser, gasPrice, gas, value, chainID, memo, udenom, walletType, txHash) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        Object[] creatsaveRemitParam = new Object[] {remitRes.get("toWalletAddress"), remitRes.get("fromWalletAddress"), remitRes.get("toUser"), remitRes.get("fromUser"), remitRes.get("gasPrice"), remitRes.get("gas"), remitRes.get("value"), remitRes.get("chainID"), remitRes.get("memo"), remitRes.get("udenom"), remitRes.get("walletType"), remitRes.get("trxHash")};
        this.jdbcTemplate.update(creatsaveRemitQuery, creatsaveRemitParam);
        return jdbcTemplate.queryForObject("select last_insert_id()", Integer.class);
    }

    public int createTokenRequest(Map<String, String> tokenRequest){
        String createTokenRequestQuery = "INSERT INTO tokenReq(reqWalletAddress, reqTokenAmount, toUser, fromUser, chainID, walletType, memo) VALUES(?,?,?,?,?,?,?)";
        Object[] createTokenRequestParam = new Object[] {tokenRequest.get("reqWalletAddress"), tokenRequest.get("reqTokenAmount"), tokenRequest.get("toUser"), tokenRequest.get("fromUser"), tokenRequest.get("chainID"), tokenRequest.get("walletType"), tokenRequest.get("memo")};
        this.jdbcTemplate.update(createTokenRequestQuery, createTokenRequestParam);
        return jdbcTemplate.queryForObject("select last_insert_id()", Integer.class);
    }
}
