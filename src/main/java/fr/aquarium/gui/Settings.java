package fr.aquarium.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Paramètres du superviseur d'aquarium
 */
public class Settings {   
    public String mysqlServer;
    public int mysqlPort;
    public String mysqlDb;
    public String mysqlUsername;
    public String mysqlPassword;
    
    public String arduinoPort;
    
    public String fishName;
    
    public String emailUsername;
    public String emailPassword;
    public List<String> recipients;
    
    public int lastMeasuresCount;
    public int interval;
    public boolean json;
    public boolean csv;
    
    /**
     * Construit des paramètres par défaut
     */
    public Settings() {
        //Configuration par défaut
        mysqlUsername = "";
        mysqlPassword = "";
        mysqlPort = 3306;
        mysqlDb = "";
    
        arduinoPort = "";

        fishName = "";

        emailUsername = "";
        emailPassword = "";
        recipients = new ArrayList<>();

        lastMeasuresCount = 10000;
        interval = 60;
        json = true;
        csv = true;
    }

    /**
     * Charge les paramètres depuis le fichier de configuration
     * @param filename Nom du fichier de configuration
     * @return Les paramètres
     * @throws FileNotFoundException Fichier non trouvé
     */
    public static Settings load(String filename) throws FileNotFoundException {
        Gson gson = new Gson();
        return gson.fromJson(new FileReader(new File(filename)), Settings.class);
    }

    /**
     * Enregistre les paramètres dans un fichier de configuration
     * @param filename Nom du fichier de configuration
     * @throws FileNotFoundException s'il y a une erreur lors de la sauvegarde
     * @throws UnsupportedEncodingException s'il y a une erreur lors de l'encodage de la sauvegarde
     */
    public void save(String filename) throws FileNotFoundException, UnsupportedEncodingException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (PrintWriter printWriter = new PrintWriter(filename, "UTF-8")) {
            printWriter.write(gson.toJson(this));
        }
    }
}
