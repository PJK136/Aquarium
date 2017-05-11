use Aquarium;

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
    Min double,
    Max double,
    FOREIGN KEY (FishId) references Fish(FishId),
    FOREIGN KEY (SensorId) references Sensor(SensorId));
    
CREATE TABLE Measure (
	SensorId tinyint not null,
    MeasureDate date not null,
    RawValue smallint not null,
    Value double not null,
    primary key (SensorId, MeasureDate),
    foreign key (SensorId) references Sensor(SensorId));
    
CREATE TABLE EventType (
	EventId smallint not null auto_increment primary key,
    EventName varchar(20) not null);
    
CREATE TABLE Event (
	EventId smallint not null,
    DateEvent date not null,
    primary key (EventId, DateEvent),
    foreign key (EventId) references EventType(EventId));