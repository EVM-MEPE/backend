package com.propwave.daotool.wallet;

import com.propwave.daotool.wallet.model.UserWallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UserWalletDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){this.jdbcTemplate = new JdbcTemplate(dataSource);}


    public List<UserWallet> getUserWallet(String walletAddress){
        String getUserWalletQuery = "select * from userWallet where walletAddress=?";
        String getWalletAddressParam = walletAddress;
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
                getWalletAddressParam
        );
    }

}
