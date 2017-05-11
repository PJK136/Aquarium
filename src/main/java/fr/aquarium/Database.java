package fr.aquarium;

import java.util.Calendar;
import java.util.List;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.SQLException;

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
    }

    public Sensor querySensor(int sensorId) {
        //TODO
        //return new Sensor(sensorId, name, unit);
        return null;
    }
    
    public Threshold queryThreshold(int fishId, int sensorId) {
        //TODO
        //return new Threshold(fishId, sensorId, fishId, fishId)
        return null;
    }
    
    public List<Measure> queryMeasures(Calendar start, Calendar stop) {
        //TODO
        return null;
    }
    
    public List<Measure> queryMeasures(int sensorId, Calendar start, Calendar stop) {
        //TODO
        return null;
    }
    
    public void insertMeasures(List<Measure> measures) {
        //TODO
    }
}
