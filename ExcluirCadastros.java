package aps;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ExcluirCadastros {

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/helpdesk", "root", "DBadm")) {
            Statement statement = connection.createStatement();
            String sql = "DELETE FROM pessoa";
            int rowsDeleted = statement.executeUpdate(sql);
            System.out.println("Registros deletados: " + rowsDeleted);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
