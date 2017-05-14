CREATE TABLE Sensor (
	SensorId tinyint not null auto_increment primary key,
    SensorName varchar(20),
    Unit varchar (10));
    
CREATE TABLE Fish (
	FishId tinyint not null auto_increment primary key,
	FishName varchar(20) not null);
    
CREATE TABLE Threshold (
	FishId tinyint not null,
    SensorId tinyint not null,
    Minimum double,
    Maximum double,
    FOREIGN KEY (FishId) references Fish(FishId),
    FOREIGN KEY (SensorId) references Sensor(SensorId));
    
CREATE TABLE Measure (
	SensorId tinyint not null,
    MeasureDate datetime not null,
    RawValue smallint not null,
    Value double not null,
    primary key (SensorId, MeasureDate),
    foreign key (SensorId) references Sensor(SensorId));
    
CREATE TABLE EventType (
	EventId smallint not null auto_increment primary key,
    EventName varchar(20) not null);
    
CREATE TABLE Event (
	EventId smallint not null,
    DateEvent datetime not null,
    primary key (EventId, DateEvent),
    foreign key (EventId) references EventType(EventId));CREATE TABLE PHCalibration (
    CalibrationDate datetime not null primary key,
    PH4 smallint unsigned not null,
    PH7 smallint unsigned not null);