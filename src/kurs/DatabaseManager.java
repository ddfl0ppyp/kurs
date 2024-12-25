package kurs;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;

public class DatabaseManager {
    public static Connection conn;
    public static Statement statmt;
    public static ResultSet resSet;
    public static String last;
    
    public static void Conn() throws SQLException, ClassNotFoundException
    {
        conn = null;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:mydb_utf8.s3db");
        statmt = conn.createStatement();
    }
    public void GetAll() throws SQLException, NoSuchMethodException, SecurityException
    {
        last =          "SELECT\r\n" + //
                        "teams.name AS Team,\r\n" + //
                        "drivers.name AS Driver,\r\n" + //
                        "tracks.name AS Track,\r\n" + //
                        "date AS Date,\r\n" + //
                        "place AS Place,\r\n" + //
                        "points AS Points,\r\n" + //
                        "races.id, teams.id, drivers.id\r\n" + //
                        "FROM races\r\n" + //
                        "JOIN teams\r\n" + //
                        "ON teams.id = races.team\r\n" + //
                        "JOIN drivers\r\n" + //
                        "ON drivers.id = races.driver\r\n" + //
                        "JOIN tracks\r\n" + //
                        "ON tracks.id = races.track;";
        resSet = statmt.executeQuery(last);
    } 

    public void pedestal(String trackName, String date) throws SQLException
    {
        resSet = statmt.executeQuery("SELECT * FROM tracks WHERE name = \"" + trackName + "\";");
        int trackId = resSet.getInt(1);
        resSet = statmt.executeQuery("SELECT teams.name AS Team,drivers.name AS Driver,tracks.name AS Track,date AS Date,place AS Place,points AS Points,races.id, teams.id, drivers.id FROM races JOIN teams ON teams.id = races.team JOIN drivers ON drivers.id = races.driver JOIN tracks ON tracks.id = races.track WHERE track = "+trackId+" AND date = \""+date+"\" ORDER BY place LIMIT 0,3;");
    }

    public static void DeleteRowById(int id) throws SQLException
    {
        statmt.executeUpdate("DELETE FROM races WHERE id = " + id);
    }

    public void FilterByField(String field, String name) throws SQLException, NoSuchMethodException, SecurityException
    {
        last =          "SELECT\r\n" + //
                        "teams.name AS Team,\r\n" + //
                        "drivers.name AS Driver,\r\n" + //
                        "tracks.name AS Track,\r\n" + //
                        "date AS Date,\r\n" + //
                        "place AS Place,\r\n" + //
                        "points AS Points,\r\n" + //
                        "races.id, teams.id, drivers.id\r\n" + //
                        "FROM races\r\n" + //
                        "JOIN teams\r\n" + //
                        "ON teams.id = races.team\r\n" + //
                        "JOIN drivers\r\n" + //
                        "ON drivers.id = races.driver\r\n" + //
                        "JOIN tracks\r\n" + //
                        "ON tracks.id = races.track\r\n" + //
                        "WHERE " + field + ".id = " + "\""+name+"\";";
        resSet = statmt.executeQuery(last);
    }

    public void AddRow(String teamName, String driverName, String trackName, String date, String place, String points, boolean edit, int id) throws SQLException
    {
        resSet = statmt.executeQuery("SELECT * FROM teams WHERE name = \"" + teamName + "\";");
        if(!resSet.next()) statmt.executeUpdate("INSERT INTO teams(name) VALUES (\""+ teamName +"\");");
        resSet = statmt.executeQuery("SELECT * FROM teams WHERE name = \"" + teamName + "\";");
        int teamId = resSet.getInt(1);
        resSet = statmt.executeQuery("SELECT * FROM drivers WHERE name = \"" + driverName + "\";");
        if(!resSet.next()) statmt.executeUpdate("INSERT INTO drivers(name) VALUES (\""+ driverName +"\");");
        resSet = statmt.executeQuery("SELECT * FROM drivers WHERE name = \"" + driverName + "\";");
        int driverId = resSet.getInt(1);
        resSet = statmt.executeQuery("SELECT * FROM tracks WHERE name = \"" + trackName + "\";");
        if(!resSet.next()) statmt.executeUpdate("INSERT INTO tracks(name) VALUES (\""+ trackName +"\");");
        resSet = statmt.executeQuery("SELECT * FROM tracks WHERE name = \"" + trackName + "\";");
        int trackId = resSet.getInt(1);
        System.out.println(trackId);
        if(edit) statmt.executeUpdate("UPDATE races SET team = "+teamId+", driver = "+driverId+", track = "+trackId+", date = \""+date+"\", place = "+place+", points = "+points+" WHERE id = "+id+";");
        else statmt.executeUpdate("INSERT INTO races(team, driver, track, date, place, points) VALUES ("+teamId+","+driverId+","+trackId+",\""+date+"\","+place+","+points+");");
    }

    public void Repeat() throws IllegalAccessException, InvocationTargetException, SQLException
    {
        resSet = statmt.executeQuery(last);
    }
}
