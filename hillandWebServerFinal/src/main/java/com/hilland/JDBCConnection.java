package com.hilland;

import com.hilland.domain.State;
import com.hilland.domain.Temperature;
import com.hilland.domain.Thermostat;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jhilland
 */
public final class JDBCConnection {

    private static final String DB_CONNECTION = "jdbc:mysql://127.0.0.1:3306/thermostat";
    private static final String ROOT = "root";
    private static final String PASSWORD = "Brady#2019";

    private JDBCConnection() {
    }

    // get request based on ID
    public static final Temperature getTemp(String id) {

        String select = "select * from temps where id = " + id;
        try ( Connection conn = setupConnection()) {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(select);
            Temperature temp = new Temperature();
            while (resultSet.next()) {
                temp.setId(resultSet.getLong("ID"));
                temp.setTemp(resultSet.getInt("TEMP"));
            }
            return temp;
        } catch (SQLException ex) {
            System.err.format("SQL State: %s\n%s", ex.getSQLState(), ex.getMessage());
        }
        return null;
    }

    public static final List<Temperature> getAllTemps() {
        List<Temperature> temps = new ArrayList<>();
        String select = "select * from temps";

        try ( Connection conn = setupConnection()) {

            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(select);
            while (resultSet.next()) {

                Temperature obj = new Temperature();
                obj.setId(resultSet.getLong("ID"));
                obj.setTemp(resultSet.getInt("TEMP"));
                temps.add(obj);
            }

        } catch (SQLException ex) {
            System.err.format("SQL State: %s\n%s", ex.getSQLState(), ex.getMessage());
        }
        return temps;
    }

    public static final String handleType(Thermostat thermostat) {
        if (thermostat instanceof Temperature) {
            return addTemp((Temperature) thermostat);
        } else if (thermostat instanceof State) {
            return addState((State) thermostat);
        }
        return "data type is not supported";
    }

    public static final String addState(State state) {
        String insert = null;
        if (state.isOn()) {
            insert = "insert into state (state, time) values ('', '"
                    + state.getTime()
                    + "')";
        } else {
            insert = "insert into state (state, time) values (NULL, '"
                    + state.getTime()
                    + "')";
        }

        try ( Connection conn = setupConnection()) {
            Statement statement = (Statement) conn.createStatement();
            statement.execute(insert);
        } catch (SQLException ex) {
            System.err.format("SQL State: %s\n%s", ex.getSQLState(), ex.getMessage());
            return "Post state Failed\n";
        }

        return "Post state Successful\n";

    }

    // add a temp to the database
    public static final String addTemp(Temperature temp) {
        
        // check to see if this time already exists, so we dont contradict ourselves
        for (Temperature dbTemp : getAllTemps()) {
            if (dbTemp.getTime() == temp.getTime()) {
                // we should do an update, but this is easier for now
                deleteTemp(Long.toString(dbTemp.getId()));
            }
        }
        
        String insert = "insert into temps (temp, time) values ('"
                + temp.getTemp()
                + "', '"
                + temp.getTime()
                + "')";

        try ( Connection conn = setupConnection()) {
            Statement statement = (Statement) conn.createStatement();
            statement.execute(insert);
        } catch (SQLException ex) {
            System.err.format("SQL State: %s\n%s", ex.getSQLState(), ex.getMessage());
            return "Post temp Failed\n";
        }
        return "Post temp Successful\n";
    }

    // delete temp from database
    public static final String deleteTemp(String id) {
        String insert = "delete from temps where id = " + id;
        try ( Connection conn = setupConnection()) {
            Statement statement = (Statement) conn.createStatement();
            statement.execute(insert);
        } catch (SQLException ex) {
            System.err.format("SQL State: %s\n%s", ex.getSQLState(), ex.getMessage());
            return "Delete Failed\n";
        }
        return "Delete Successful\n";
    }

    private static final Connection setupConnection() throws SQLException {
        return DriverManager.getConnection(DB_CONNECTION, ROOT, PASSWORD);
    }

}
