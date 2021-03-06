CREATE TABLE Sensor (
    SensorId tinyint unsigned not null auto_increment primary key,
    SensorName varchar(20),
    Unit varchar (10));
    
CREATE TABLE Fish (
    FishId tinyint unsigned not null auto_increment primary key,
    FishName varchar(20) not null);
    
CREATE TABLE Threshold (
    FishId tinyint unsigned not null,
    SensorId tinyint unsigned not null,
    Minimum double,
    Maximum double,
    PRIMARY KEY (FishId, SensorId),
    FOREIGN KEY (FishId) references Fish(FishId),
    FOREIGN KEY (SensorId) references Sensor(SensorId));
    
CREATE TABLE Measure (
    SensorId tinyint unsigned not null,
    MeasureDate datetime not null,
    RawValue smallint unsigned not null,
    Value double not null,
    primary key (SensorId, MeasureDate),
    foreign key (SensorId) references Sensor(SensorId));
    
CREATE TABLE EventType (
    EventId smallint unsigned not null auto_increment primary key,
    EventName varchar(20) not null);
    
CREATE TABLE Event (
    EventId smallint unsigned not null,
    EventDate datetime not null,
    primary key (EventId, EventDate),
    foreign key (EventId) references EventType(EventId));

CREATE TABLE PHCalibration (
    SensorId tinyint unsigned not null,
    CalibrationDate datetime not null,
    PH4 smallint unsigned not null,
    PH7 smallint unsigned not null,
    primary key (SensorId, CalibrationDate),
    foreign key (SensorId) references Sensor(SensorId));