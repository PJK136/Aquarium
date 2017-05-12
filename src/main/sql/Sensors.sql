use G221_B_BD1;

insert into Sensor values
(1, "Luminosité", NULL),
(2, "pH", NULL),
(3, "Débit", NULL),
(4, "Niveau d'eau", NULL),
(5, "Température", NULL);

select * from Sensor;

delete from Sensor where SensorId != 0;