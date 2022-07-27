package com.hilland;

import com.hilland.domain.Report;
import com.hilland.domain.State;
import com.hilland.domain.Temperature;
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

    public static final State getState() {
        String select = "select * from state";
        try ( Connection conn = setupConnection()) {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(select);
            State state = new State();
            while (resultSet.next()) {
                String currentState = resultSet.getString("STATE");
                if (currentState != null) {
                    state.setOn(true);
                } else {
                    state.setOn(false);
                }
            }
            return state;
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

    public static final List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String select = "select * from report";

        try ( Connection conn = setupConnection()) {

            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(select);
            while (resultSet.next()) {

                Report obj = new Report();
                obj.setId(resultSet.getLong("ID"));
                obj.setTemp(resultSet.getInt("TEMP"));
                obj.setDate(resultSet.getTimestamp("DATE"));
                reports.add(obj);
            }

        } catch (SQLException ex) {
            System.err.format("SQL State: %s\n%s", ex.getSQLState(), ex.getMessage());
        }
        return reports;
    }

    public static final String addReport(Report report) {
        String insert = "insert into report (temp, date) values ('"
                + report.getTemp()
                + "', '"
                + report.getDate()
                + "')";

        try ( Connection conn = setupConnection()) {
            Statement statement = (Statement) conn.createStatement();
            statement.execute(insert);
        } catch (SQLException ex) {
            System.err.format("SQL State: %s\n%s", ex.getSQLState(), ex.getMessage());
            return "Post state Failed\n";
        }

        return "Post Report successful\n";
    }

    public static final String updateState(boolean value) {
        String update = null;

        if (value) {
            update = "update state set state = ''";
        } else {
            update = "update state set state = NULL";
        }
        
        try ( Connection conn = setupConnection()) {
            Statement statement = (Statement) conn.createStatement();
            statement.execute(update);
        } catch (SQLException ex) {
            System.err.format("SQL State: %s\n%s", ex.getSQLState(), ex.getMessage());
            return "Update state Failed\n";
        }
        
        return "Post state Failed\n";
    }

    public static final String addState(State state) {
        String insert = null;
        if (state.isOn()) {
            insert = "insert into state (state, date) values ('', '"
                    + state.getDate()
                    + "')";
        } else {
            insert = "insert into state (state, date) values (NULL, '"
                    + state.getDate()
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
            if (dbTemp.getDate() == temp.getDate()) {
                // we should do an update, but this is easier for now
                deleteTemp(Long.toString(dbTemp.getId()));
            }
        }

        String insert = "insert into temps (temp, date) values ('"
                + temp.getTemp()
                + "', '"
                + temp.getDate()
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
