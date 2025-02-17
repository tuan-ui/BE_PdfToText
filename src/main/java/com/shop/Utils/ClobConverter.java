package com.shop.Utils;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ClobConverter {
    public static Clob createClob(String text) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@125.212.216.115:2531/pdb19c", "VOFFICEEXIMBANK", "VOFFICEEXIMBANK#123");

        Clob clob = connection.createClob();
        clob.setString(1, text);

        return clob;
    }
}
