CREATE TABLE Category (
    CategoryID NVARCHAR(50) PRIMARY KEY,
    CategoryName NVARCHAR(255) NOT NULL,
    Image NVARCHAR(255) NULL
);

CREATE TABLE Specification (
    SpecID NVARCHAR(50) PRIMARY KEY,
    SpecName NVARCHAR(255) NOT NULL,
    SpecValue NVARCHAR(255) NOT NULL
);

CREATE TABLE Component (
    ComponentID NVARCHAR(50) PRIMARY KEY,
    ComponentName NVARCHAR(255) NOT NULL,
    Description NVARCHAR(500) NULL,
    Price DECIMAL(18,2) NOT NULL,
    CategoryID NVARCHAR(50) NOT NULL,
    FOREIGN KEY (CategoryID) REFERENCES Category(CategoryID)
);

CREATE TABLE SpecificationComponent (
    SpecComponentID NVARCHAR(50) PRIMARY KEY,
    SpecificationID NVARCHAR(50) NOT NULL,
    ComponentID NVARCHAR(50) NOT NULL,
    FOREIGN KEY (SpecificationID) REFERENCES Specification(SpecID),
    FOREIGN KEY (ComponentID) REFERENCES Component(ComponentID)
);

CREATE TABLE Device (
    SerialNumber NVARCHAR(50) PRIMARY KEY,
    DeviceName NVARCHAR(255) NOT NULL
);

CREATE TABLE DeviceComponent (
    DeviceComponentID NVARCHAR(50) PRIMARY KEY,
    SerialNumber NVARCHAR(50) NOT NULL,
    ComponentID NVARCHAR(50) NOT NULL,
    ComponentName NVARCHAR(255) NOT NULL,
    FOREIGN KEY (SerialNumber) REFERENCES Device(SerialNumber),
    FOREIGN KEY (ComponentID) REFERENCES Component(ComponentID)
);

CREATE TABLE Warehouse (
    WarehouseID NVARCHAR(50) PRIMARY KEY,
    WarehouseName NVARCHAR(255) NOT NULL,
    Location NVARCHAR(255) NOT NULL,
    WarehouseStatus NVARCHAR(50) NOT NULL,
    WarehouseOwnerID NVARCHAR(50) NOT NULL
);

CREATE TABLE ComponentsWarehouse (
    CmpWID NVARCHAR(50) PRIMARY KEY,
    ComponentID NVARCHAR(50) NOT NULL,
    WarehouseID NVARCHAR(50) NOT NULL,
    FOREIGN KEY (ComponentID) REFERENCES Component(ComponentID),
    FOREIGN KEY (WarehouseID) REFERENCES Warehouse(WarehouseID)
);

CREATE TABLE DevicesWarehouse (
    DevWID NVARCHAR(50) PRIMARY KEY,
    SerialNumber NVARCHAR(50) NOT NULL,
    WarehouseID NVARCHAR(50) NOT NULL,
    FOREIGN KEY (SerialNumber) REFERENCES Device(SerialNumber),
    FOREIGN KEY (WarehouseID) REFERENCES Warehouse(WarehouseID)
);

CREATE TABLE ComponentUpdated (
    ComponentUpdatedID NVARCHAR(50) PRIMARY KEY,
    UpdateDate DATETIME NOT NULL,
    NewValue NVARCHAR(255) NOT NULL,
    OldValue NVARCHAR(255) NOT NULL,
    ComponentID NVARCHAR(50) NOT NULL,
    FOREIGN KEY (ComponentID) REFERENCES Component(ComponentID)
);

CREATE TABLE UpdateLog (
    UpdateLogID NVARCHAR(50) PRIMARY KEY,
    UpdateDate DATETIME NOT NULL,
    NewValue NVARCHAR(255) NOT NULL,
    OldValue NVARCHAR(255) NOT NULL,
    ComponentID NVARCHAR(50) NOT NULL,
    FOREIGN KEY (ComponentID) REFERENCES Component(ComponentID)
);

CREATE TABLE StockCheck (
    StockCheckID NVARCHAR(50) PRIMARY KEY,
    CheckDate DATETIME NOT NULL,
    Quantity INT NOT NULL,
    Result NVARCHAR(255) NULL,
    WarehouseID NVARCHAR(50) NOT NULL,
    FOREIGN KEY (WarehouseID) REFERENCES Warehouse(WarehouseID)
);

CREATE TABLE WarehouseLog (
    WarehouseLogID NVARCHAR(50) PRIMARY KEY,
    Date DATETIME NOT NULL,
    Status NVARCHAR(50) NOT NULL,
    Report NVARCHAR(255) NULL,
    WarehouseID NVARCHAR(50) NOT NULL,
    FOREIGN KEY (WarehouseID) REFERENCES Warehouse(WarehouseID)
);

CREATE TABLE ComponentImportExport (
    ComponentImportExportID NVARCHAR(50) PRIMARY KEY,
    ComponentID NVARCHAR(50) NOT NULL,
    Quantity INT NOT NULL,
    WarehouseLogID NVARCHAR(50) NOT NULL,
    FOREIGN KEY (ComponentID) REFERENCES Component(ComponentID),
    FOREIGN KEY (WarehouseLogID) REFERENCES WarehouseLog(WarehouseLogID)
);

CREATE TABLE Account (
    Username NVARCHAR(50) PRIMARY KEY,
    Address NVARCHAR(255) NULL,
    Phone NVARCHAR(50) NULL,
    Role NVARCHAR(50) NOT NULL
);

CREATE TABLE Customer (
    CustomerID NVARCHAR(50) PRIMARY KEY,
    CustomerAddress NVARCHAR(255) NULL,
    CustomerPhone NVARCHAR(50) NULL,
    CustomerName NVARCHAR(255) NOT NULL,
    Username NVARCHAR(50) NOT NULL,
    FOREIGN KEY (Username) REFERENCES Account(Username)
);

CREATE TABLE Staff (
    StaffID NVARCHAR(50) PRIMARY KEY,
    StaffAddress NVARCHAR(255) NULL,
    StaffPhone NVARCHAR(50) NULL,
    StaffName NVARCHAR(255) NOT NULL,
    Age INT NOT NULL,
    Username NVARCHAR(50) NOT NULL,
    FOREIGN KEY (Username) REFERENCES Account(Username)
);

CREATE TABLE AccountRequest (
    AccountRequestID NVARCHAR(50) PRIMARY KEY,
    RequestDate DATETIME NOT NULL,
    Username NVARCHAR(50) NOT NULL,
    FOREIGN KEY (Username) REFERENCES Account(Username)
);

CREATE TABLE Contract (
    ContractID NVARCHAR(50) PRIMARY KEY,
    Date DATETIME NOT NULL,
    Total DECIMAL(18,2) NOT NULL,
    CustomerID NVARCHAR(50) NOT NULL,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
);

CREATE TABLE Request (
    RequestID NVARCHAR(50) PRIMARY KEY,
    RequestDescription NVARCHAR(500) NULL,
    RequestDate DATETIME NOT NULL,
    Status NVARCHAR(50) NOT NULL,
    StaffID NVARCHAR(50) NOT NULL,
    ContractID NVARCHAR(50) NOT NULL,
    FOREIGN KEY (StaffID) REFERENCES Staff(StaffID),
    FOREIGN KEY (ContractID) REFERENCES Contract(ContractID)
);
