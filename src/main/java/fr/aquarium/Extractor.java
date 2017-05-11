package fr.aquarium;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Extractor {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final Database database;

    public Extractor(Database database) {
        this.database = database;
    }
    
    public void dumpToJSON(Path path) {
        //TODO
    }
    
    public void dumpToCSV(Path path) {
        //TODO
    }
}
