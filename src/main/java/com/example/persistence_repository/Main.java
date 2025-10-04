package com.example.persistence_repository;

import java.sql.SQLException;

import com.example.persistence_repository.common.model.Account;
import com.example.persistence_repository.common.model.AccountRequest;
import com.example.persistence_repository.common.model.Category;
import com.example.persistence_repository.common.model.Contract;
import com.example.persistence_repository.common.model.Customer;
import com.example.persistence_repository.common.model.Feature;
import com.example.persistence_repository.common.model.Feedback;
import com.example.persistence_repository.common.model.InventoryItem;
import com.example.persistence_repository.common.model.Product;
import com.example.persistence_repository.common.model.ProductContract;
import com.example.persistence_repository.common.model.ProductExported;
import com.example.persistence_repository.common.model.ProductRequest;
import com.example.persistence_repository.common.model.ProductSpecification;
import com.example.persistence_repository.common.model.ProductTransaction;
import com.example.persistence_repository.common.model.ProductWarehouse;
import com.example.persistence_repository.common.model.Request;
import com.example.persistence_repository.common.model.RequestLog;
import com.example.persistence_repository.common.model.Role;
import com.example.persistence_repository.common.model.RoleFeature;
import com.example.persistence_repository.common.model.Specification;
import com.example.persistence_repository.common.model.SpecificationType;
import com.example.persistence_repository.common.model.Staff;
import com.example.persistence_repository.common.model.Type;
import com.example.persistence_repository.common.model.Warehouse;
import com.example.persistence_repository.common.model.WarehouseLog;
import com.example.persistence_repository.common.model.enums.AccountStatus;
import com.example.persistence_repository.common.repository.AccountRepository;
import com.example.persistence_repository.persistence.config.DBcontext;
import com.example.persistence_repository.persistence.config.TransactionManager;
import com.example.persistence_repository.persistence.entity.EntityRegistry;
import com.example.persistence_repository.persistence.entity.SchemaGenerator;
import com.example.persistence_repository.persistence.query.common.Page;
import com.example.persistence_repository.persistence.repository.CrudRepository;
import com.example.persistence_repository.persistence.repository.SimpleRepository;

public class Main {
    public static void main(String[] args) {
        AccountRepository accountRepository = new AccountRepository();
        try {
            TransactionManager.beginTransaction();
            Account account = new Account();
            Role role = new Role();
            role.setRoleID(1L);
            role.setRoleName("Administrator");
            account.setRole(role);
            System.out.println(account.getRole().getRoleName());
            TransactionManager.commit();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            try {
                TransactionManager.rollback();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

    }

    public static void generateAll() {
        try {
            TransactionManager.beginTransaction();
            // Ensure schema exists then load sample data
            System.out.println("Generating all database schemas...");
            testSchemaGeneration();
            System.out.println("Loading sample data into the database...");
            SampleDataLoader.loadAll();
            TransactionManager.commit();
            System.out.println("Sample data loaded successfully.");
        } catch (SQLException e) {
            try {
                TransactionManager.rollback();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            System.err.println("Failed to load sample data: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public static void testSchemaGeneration() {
        try {
            // Initialize DB connection (side-effect: ensure driver loaded)
            DBcontext.getConnection();

            // 1. Register all entity classes (add new ones here when created)
            Class<?>[] entities = new Class<?>[] {
                    Account.class,
                    Role.class,
                    Feature.class,
                    RoleFeature.class,
                    AccountRequest.class,
                    Category.class,
                    Type.class,
                    SpecificationType.class,
                    Specification.class,
                    Product.class,
                    ProductSpecification.class,
                    ProductWarehouse.class,
                    Warehouse.class,
                    WarehouseLog.class,
                    InventoryItem.class,
                    ProductTransaction.class,
                    ProductRequest.class,
                    ProductExported.class,
                    Staff.class,
                    Customer.class,
                    Contract.class,
                    Request.class,
                    RequestLog.class,
                    Feedback.class,
                    ProductContract.class
            };
            for (Class<?> c : entities) {
                @SuppressWarnings("unchecked")
                Class<Object> cls = (Class<Object>) c;
                EntityRegistry.register(cls);
            }

            // 2. Generate schema (DDL) for all registered entities
            SchemaGenerator.withDefault().generateAll();

            System.out.println("Schema generation completed successfully.");
        } catch (Exception e) {
            System.err.println("Schema generation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
