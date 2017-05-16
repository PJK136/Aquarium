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

    /**
     * Récupère le capteur spécifié
     * @param sensorId Identifiant du capteur
     * @return Données à propos du capteur
     */
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

    /**
     * Récupère tous les capteurs de la base de données
     * @return Liste de tous les capteurs
     */
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

    /**
     * Récupère les limites pour un poisson et un capteur donné
     * @param fishId Identifiant du poisson
     * @param sensorId Identifiant du capteur
     * @return Limites associées
     */
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
    
    /**
     * Récupère les mesures entre deux moments
     * @param start À partir de ce moment (inclus)
     * @param stop Jusqu'à ce moment (exclu)
     * @return Liste de mesures
     */
    public List<Measure> queryMeasures(Calendar start, Calendar stop) {
        try (Connection connection = dataSource.getConnection()) {
            List<Measure> measures = new LinkedList<Measure>();
            PreparedStatement ps = connection.prepareStatement("SELECT sensorId, MeasureDate, rawValue, value " + "FROM Measure " + "WHERE MeasureDate>=? and MeasureDate<?;");
            ps.setTimestamp(1, new Timestamp(start.getTimeInMillis()));
            ps.setTimestamp(2, new Timestamp(stop.getTimeInMillis()));
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                measures.add(new Measure(rs.getInt("SensorId"), timestampToCalendar(rs.getTimestamp("MeasureDate")), rs.getInt("rawValue"), rs.getDouble("value")));
            }
            return measures;
        } catch (SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
            return null;
        }
    }

    /**
     * Récupère les mesures pour un capteur donné entre deux moments
     * @param sensorId Identifiant du capteur
     * @param start À partir de ce moment (inclus)
     * @param stop Jusqu'à ce moment (exclu)
     * @return Liste de mesures pour le capteur spécifié
     */
    public List<Measure> queryMeasures(int sensorId, Calendar start, Calendar stop) {
        try (Connection connection = dataSource.getConnection()) {
            List<Measure> measures = new LinkedList<Measure>();
            PreparedStatement ps = connection.prepareStatement("SELECT MeasureDate, rawValue, value " + "FROM Measure " + "WHERE SensorId=? and MeasureDate>=? and MeasureDate<?;");
            ps.setInt(1, sensorId);
            ps.setTimestamp(2, new Timestamp(start.getTimeInMillis()));
            ps.setTimestamp(3, new Timestamp(stop.getTimeInMillis()));
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                measures.add(new Measure(sensorId, timestampToCalendar(rs.getTimestamp("MeasureDate")), rs.getInt("rawValue"), rs.getDouble("value")));
            }
            return measures;
        } catch (SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
            return null;
        }
    }
    
     /**
     * Récupère les n-dernières mesures
     * @param count Nombre de mesures à récupérer
     * @return Liste de mesures
     */
    public List<Measure> queryLastMeasures(int count) {
        try (Connection connection = dataSource.getConnection()) {
            List<Measure> measures = new LinkedList<Measure>();
            PreparedStatement ps = connection.prepareStatement("SELECT SensorId, MeasureDate, rawValue, value "
                    + "FROM Measure ORDER BY MeasureDate DESC LIMIT ?");
            ps.setInt(1, count);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                measures.add(new Measure(rs.getInt("SensorId"), timestampToCalendar(rs.getTimestamp("MeasureDate")), rs.getInt("rawValue"), rs.getDouble("value")));
            }
            return measures;
        } catch (SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
            return null;
        }
    }

    /**
     * Récupère les n-dernières mesures pour un capteur donné
     * @param sensorId Identifiant du capteur
     * @param count Nombre de mesures à récupérer
     * @return Liste de mesures pour le capteur spécifié
     */
    public List<Measure> queryLastMeasures(int sensorId, int count) {
        try (Connection connection = dataSource.getConnection()) {
            List<Measure> measures = new LinkedList<Measure>();
            PreparedStatement ps = connection.prepareStatement("SELECT SensorId, MeasureDate, rawValue, value "
                    + "FROM Measure WHERE SensorId=? ORDER BY MeasureDate DESC LIMIT ?");
            ps.setInt(1, sensorId);
            ps.setInt(2, count);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                measures.add(new Measure(rs.getInt("SensorId"), timestampToCalendar(rs.getTimestamp("MeasureDate")), rs.getInt("rawValue"), rs.getDouble("value")));
            }
            return measures;
        } catch (SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
            return null;
        }
    }
    
    /**
     * Insère les mesures dans la base de donnée
     * @param measures Liste des mesures à insérer
     * @throws SQLException Erreur SQL
     */
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
        connection.close();
    }
    
    /**
     * Récupère la dernière calibration du capteur de pH
     * @return Données de calibration du capteur de pH
     */
    public PHCalibration queryLastPHCalibration() {
        try (Connection connection = dataSource.getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("select * from PHCalibration order by CalibrationDate desc limit 1");
            if (rs.next()) {
                return new PHCalibration(timestampToCalendar(rs.getTimestamp("CalibrationDate")), rs.getInt("PH4"), rs.getInt("PH7"));
            }
            return null;
        } catch (SQLException ex) {
            logger.error(ex.getClass().getName(), ex);
            return null;
        }
    }
    
    /**
     * Insère une calibration du capteur de pH dans la base de donnée
     * @param calibration Données de calibration
     * @throws SQLException Erreur SQL
     */
    public void insertPHCalibration(PHCalibration calibration) throws SQLException {
        Connection connection = dataSource.getConnection();
        String query = "insert into PHCalibration (CalibrationDate, PH4, PH7) values(?,?,?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setTimestamp(1, new Timestamp(calibration.getDate().getTimeInMillis()));
        ps.setInt(2, calibration.getpH4());
        ps.setInt(3, calibration.getpH7());
        ps.execute();
        connection.close();
    }
}
