package fr.aquarium;

import java.util.Calendar;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.LinkedList;

public class Database {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private MysqlDataSource dataSource;
    
    public Database(String serverName, int port, String database, String user, String password) throws SQLException {
        this.dataSource = new MysqlDataSource();
        this.dataSource.setServerName(serverName);
        this.dataSource.setPort(port);
        this.dataSource.setDatabaseName(database);
        this.dataSource.setUser(user);
        this.dataSource.setPassword(password);
        
        this.dataSource.getConnection(); //Test de connection
        logger.info("Connexion réussie à la BDD {}", database);
    }

    public Sensor querySensor(int sensorId) {
        try (Connection connection = dataSource.getConnection()) {
            String query = "select * from Sensor where SensorId=?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1,sensorId);
            ResultSet rs=ps.executeQuery();
            if (rs.next())
                return new Sensor(sensorId, rs.getString("SensorName"),rs.getString("Unit"));
            else
                return null;
        } catch (SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
            return null;
        }
    }
    
    public List<Sensor> querySensors() {
        try (Connection connection = dataSource.getConnection()) {
            List<Sensor> sensors = new LinkedList<Sensor>();
            String query = "select * from Sensor";
            ResultSet rs = connection.createStatement().executeQuery(query);
            while (rs.next()) {
                sensors.add(new Sensor(rs.getInt("SensorId"), rs.getString("SensorName"),rs.getString("Unit")));
            }
            return sensors;
        } catch (SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
            return null;
        }
    }
    
    public Threshold queryThreshold(int fishId, int sensorId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("select Minimum, Maximum from Threshold where FishId=? and SensorId=?;");
            ps.setInt(1, fishId);
            ps.setInt(2, sensorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int min = rs.getInt("Minimum");
                int max = rs.getInt("Maximum");
                return new Threshold(fishId, sensorId, min, max);
            } else
                return null;
        } catch (SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
            return null;
        }
    }
    
    private static Calendar timestampToCalendar(Timestamp timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        return calendar;
    }
    
    public List<Measure> queryMeasures(Calendar start, Calendar stop) {
        try (Connection connection = dataSource.getConnection()) {
            List<Measure> measures = new LinkedList<Measure>();
            PreparedStatement ps = connection.prepareStatement("SELECT sensorId, MeasureDate, rawValue, value " + "FROM Measure " + "WHERE MeasureDate>? and MeasureDate<?;");
            ps.setTimestamp(1, new Timestamp(start.getTimeInMillis()));
            ps.setTimestamp(2, new Timestamp(stop.getTimeInMillis()));
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                measures.add(new Measure(rs.getInt("sensorId"), timestampToCalendar(rs.getTimestamp("MeasureDate")), rs.getInt("rawValue"), rs.getDouble("value")));
            }
            connection.close();
            return measures;
        } catch (SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
            return null;
        }
    }
    
    public List<Measure> queryMeasures(int sensorId, Calendar start, Calendar stop) {
        try (Connection connection = dataSource.getConnection()) {
            List<Measure> measures = new LinkedList<Measure>();
            PreparedStatement ps = connection.prepareStatement("SELECT MeasureDate, rawValue, value " + "FROM Measure " + "WHERE sensorId=? and MeasureDate>? and MeasureDate<?;");
            ps.setInt(1, sensorId);
            ps.setTimestamp(2, new Timestamp(start.getTimeInMillis()));
            ps.setTimestamp(3, new Timestamp(stop.getTimeInMillis()));
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                measures.add(new Measure(sensorId, timestampToCalendar(rs.getTimestamp("MeasureDate")), rs.getInt("rawValue"), rs.getDouble("value")));
            }
            connection.close();
            return measures;
        } catch (SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
            return null;
        }
    }
    
    public void insertMeasures(List<Measure> measures) throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        //Ici, on fait 1 requête pour ajouter toutes les mesures d'un coup
        String query="insert into Measure (SensorId, MeasureDate, RawValue, Value) values(?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(query);
        for (Measure measure : measures) {
            ps.setInt(1, measure.getSensorId());
            ps.setTimestamp(2, new Timestamp(measure.getDate().getTimeInMillis()));
            ps.setInt(3, measure.getRawValue());
            ps.setDouble(4, measure.getValue());
            ps.addBatch();
        }
        
        ps.executeBatch();
        connection.commit();
    }
}
