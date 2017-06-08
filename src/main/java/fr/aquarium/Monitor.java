package fr.aquarium;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Monitor implements MeasureListener {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final int DELAY_BETWEEN_EMAILS = 3600; //secondes
    
    private final int fishId;
    private final Database database;
    private final String username;
    private final String password;
    private final String recipients;
    
    private Date lastEmailSent;

    public Monitor(Database database, int fishId, String username, String password, String recipients) {
        this.database = database;
        this.fishId = fishId;
        this.username = username;
        this.password = password;
        this.recipients = recipients;
    }
    
    @Override
    public void measureReceived(MeasureEvent event) {
        List<Measure> measures = event.getMeasures();
        Threshold threshold;
        Sensor sensor;
        String inf = "Mesures : ";
        for (Measure measure : measures){
            sensor = database.querySensor(measure.getSensorId());
            inf = inf + sensor.getName()+ " : "+ measure.getValue() + sensor.getUnitSuffix() + ",\t";
        }
        logger.info(inf);
        
        List<String> problems = new ArrayList<String>();
        
        for (Measure measure : measures) {
            threshold = database.queryThreshold(this.fishId,measure.getSensorId());
            sensor = database.querySensor(measure.getSensorId());
            if (threshold == null || sensor == null)
                continue;
            
            double value = measure.getValue();
            String unit = sensor.getUnitSuffix();
            if (value>threshold.getMax()){
                String problem = "La mesure du capteur " + sensor.getName() + " est trop haute : "
                        + value + unit + " (Max : " + threshold.getMax() + unit + ").";
                logger.error(problem);
                problems.add(problem);
            } else if (value<threshold.getMin()){
                String problem = "La mesure du capteur " + sensor.getName() + " est trop basse : "
                        + value + unit + " (Min : " + threshold.getMin() + unit + ").";
                logger.error(problem);
                problems.add(problem);
            }
        }
        
        if (!problems.isEmpty() &&
                (lastEmailSent == null || (new Date().getTime() - lastEmailSent.getTime())/1000 > DELAY_BETWEEN_EMAILS)) {
            StringBuilder builder = new StringBuilder();
            builder.append("Bonjour,\n\n");
            builder.append("Quelques paramètres de votre aquarium ne semblent pas bons :\n");
            for (String problem : problems) {
                builder.append("- ");
                builder.append(problem);
                builder.append("\n");
            }
            builder.append("\nVeuillez vérifier votre installation au plus vite !");
            
            try {
                sendEmail(username, password, recipients,
                        "[Aquarium] Valeurs limites atteintes", builder.toString());
            } catch (MessagingException ex) {
                logger.info("Impossible d'envoyer un email à {}", recipients, ex);
            }
            lastEmailSent = new Date();
        }
        
    }

    @Override
    public void pHCalibrationReceived(PHCalibrationEvent event) {
        //Nop
    }
    
    public static void sendEmail(final String username, final String password, String recipients, String subject, String text) throws AddressException, MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO,
            InternetAddress.parse(recipients));
        message.setSubject(subject);
        message.setText(text);

        Transport.send(message);

        logger.info("Email envoyé à {}", recipients);
    }
}
