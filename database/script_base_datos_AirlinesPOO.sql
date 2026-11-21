-- 1. Crear la base de datos oficial
IF DB_ID('AeroFastTrack') IS NULL
BEGIN
    CREATE DATABASE AeroFastTrack;
END
GO

USE AeroFastTrack;
GO

CREATE TABLE dbo.tblAirlines (
                                 airlineId INT IDENTITY(100,10) PRIMARY KEY,
                                 name      VARCHAR(50)          NOT NULL,
                                 CONSTRAINT UQ_tblAirlines_name UNIQUE (name)
);
GO


CREATE TABLE dbo.tblCities (
                               CityId INT IDENTITY(1,1) PRIMARY KEY,
                               name   VARCHAR(20)       NOT NULL
);
GO

CREATE TABLE dbo.tblCountries (
                                  CountryId SMALLINT PRIMARY KEY,
                                  Name      VARCHAR(20) NULL
);
GO

CREATE TABLE dbo.tblDocumentsType (
                                      TypeId      CHAR(2) PRIMARY KEY,
                                      Description VARCHAR(20) NULL
);
GO

CREATE TABLE dbo.tblAirports (
                                 AirportId UNIQUEIDENTIFIER PRIMARY KEY,
                                 name      VARCHAR(50)      NOT NULL,
                                 CityId    INT              NULL
);
GO

CREATE TABLE dbo.tblCustomers (
                                  customerId      VARCHAR(20) PRIMARY KEY,
                                  FirtName        NVARCHAR(30) NULL,
                                  Lastname        NVARCHAR(30) NULL,
                                  TypeId          CHAR(2)      NULL,
                                  TelephoneNumber VARCHAR(15)  NOT NULL,
                                  Email           NVARCHAR(80) NULL,
                                  CountryId       SMALLINT     NULL,
                                  Address         NVARCHAR(40) NULL,
                                  CONSTRAINT UQ_tblCustomers_Email UNIQUE (Email)
);
GO

CREATE TABLE dbo.tblFlights (
                                FlightId      INT IDENTITY(1,1) PRIMARY KEY,
                                FlightName    VARCHAR(8)        NULL,
                                Departure     UNIQUEIDENTIFIER  NULL,
                                Arrival       UNIQUEIDENTIFIER  NULL,
                                DateDeparture DATE              NOT NULL,
                                HourDeparture TIME(7)           NOT NULL,
                                DateArrival   DATE              NOT NULL,
                                HourArrival   TIME(7)           NOT NULL,
                                AirlineId     INT               NULL,
                                CONSTRAINT UQ_tblFlights_FlightName UNIQUE (FlightName)
);
GO

CREATE TABLE dbo.tblReservations (
                                     ReservationId INT PRIMARY KEY,
                                     CustomerId    VARCHAR(20) NULL,
                                     FlightId      INT         NULL
);
GO

CREATE TABLE dbo.tblDetailsReservation (
                                           DetailId          INT PRIMARY KEY,
                                           ReservationId     INT          NULL,
                                           PassangerFullname NVARCHAR(50) NULL,
                                           PassagerDocument  VARCHAR(20)  NULL,
                                           SeatNumer         SMALLINT     NULL,
                                           RowNumber         CHAR(2)      NULL
);
GO

-- ------------------------------------------------------------
-- CLAVES FORANEAS
-- ------------------------------------------------------------

ALTER TABLE dbo.tblAirports
    ADD CONSTRAINT FK_Airports_Cities
        FOREIGN KEY (CityId) REFERENCES dbo.tblCities (CityId);
GO

ALTER TABLE dbo.tblCustomers
    ADD CONSTRAINT FK_Customers_Countries
        FOREIGN KEY (CountryId) REFERENCES dbo.tblCountries (CountryId);
GO

ALTER TABLE dbo.tblCustomers
    ADD CONSTRAINT FK_tblCustomers_tblDocumentsType
        FOREIGN KEY (TypeId) REFERENCES dbo.tblDocumentsType (TypeId);
GO

ALTER TABLE dbo.tblFlights
    ADD CONSTRAINT FK_Flights_Airlines
        FOREIGN KEY (AirlineId) REFERENCES dbo.tblAirlines (airlineId);
GO

ALTER TABLE dbo.tblFlights
    ADD CONSTRAINT FK_FlightsArrival_Airports
        FOREIGN KEY (Arrival) REFERENCES dbo.tblAirports (AirportId);
GO

ALTER TABLE dbo.tblFlights
    ADD CONSTRAINT FK_FlightsDeparture_Airports
        FOREIGN KEY (Departure) REFERENCES dbo.tblAirports (AirportId);
GO

ALTER TABLE dbo.tblReservations
    ADD CONSTRAINT FK_Reservations_Customers
        FOREIGN KEY (CustomerId) REFERENCES dbo.tblCustomers (customerId);
GO

ALTER TABLE dbo.tblReservations
    ADD CONSTRAINT FK_Reservations_Flights
        FOREIGN KEY (FlightId) REFERENCES dbo.tblFlights (FlightId);
GO

ALTER TABLE dbo.tblDetailsReservation
    ADD CONSTRAINT FK_tblDetailsReservation_Reservation
        FOREIGN KEY (ReservationId) REFERENCES dbo.tblReservations (ReservationId);
GO


-----------------------------------------------------
CREATE OR ALTER PROCEDURE dbo.uspCrearAerolineas
    @name VARCHAR(50)
    AS
BEGIN
    SET NOCOUNT ON;
INSERT INTO dbo.tblAirlines (name) VALUES (@name);
SELECT CAST(SCOPE_IDENTITY() AS INT) AS airlineId;
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.tblAirlines)
BEGIN
EXEC dbo.uspCrearAerolineas @name = 'Avianca';
EXEC dbo.uspCrearAerolineas @name = 'LATAM Airlines';
EXEC dbo.uspCrearAerolineas @name = 'Copa Airlines';
END
GO
