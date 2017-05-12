package fr.aquarium;

import java.util.Calendar;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            //TODO
            String query = "select * from Sensors where SensorId=?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1,sensorId);
            ResultSet rs=ps.executeQuery();
            return new Sensor(sensorId, rs.getString("name"),rs.getString("unit"));
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
            int min = rs.getInt("min");
            int max = rs.getInt("max");
            return new Threshold(fishId, sensorId, min, max);
        } catch (SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
            return null;
        }
    }
    
    private static Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
    
    public List<Measure> queryMeasures(Calendar start, Calendar stop) {
        try (Connection connection = dataSource.getConnection()) {
            List<Measure> measures = new LinkedList<Measure>();
            PreparedStatement ps = connection.prepareStatement("SELECT sensorId, MeasureDate, rawValue, value " + "FROM Measure " + "WHERE MeasureDate>? and MeasureDate<?;");
            ps.setDate(1, new Date(start.getTimeInMillis()));
            ps.setDate(2, new Date(stop.getTimeInMillis()));
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                measures.add(new Measure(rs.getInt("sensorId"), dateToCalendar(rs.getDate("MeasureDate")), rs.getInt("rawValue"), rs.getDouble("value")));
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
            ps.setDate(2, new Date(start.getTimeInMillis()));
            ps.setDate(3, new Date(stop.getTimeInMillis()));
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                measures.add(new Measure(sensorId, dateToCalendar(rs.getDate("MeasureDdate")), rs.getInt("rawValue"), rs.getDouble("value")));
            }
            connection.close();
            return measures;
        } catch (SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
            return null;
        }
    }
    
    public void insertMeasures(List<Measure> measures) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            //Ici, on fait 1 requête pour ajouter toutes les mesures d'un coup
            String query="insert into measure (SensorId, MeasureDate, RawValue, Value) values(?,?,?,?)";
            PreparedStatement ps = connection.prepareStatement(query);
            for (Measure measure : measures) {
                ps.setInt(1, measure.getSensorId());
                ps.setDate(2, new Date(measure.getDate().getTimeInMillis()));
                ps.setInt(3, measure.getRawValue());
                ps.setDouble(4, measure.getValue());
                ps.addBatch();
            }
            connection.commit();
        } catch (SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
        }
    }
}
