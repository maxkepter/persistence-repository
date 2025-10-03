package com.example.persistence_repository;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDateTime;

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
import com.example.persistence_repository.common.model.enums.ProductRequestStatus;
import com.example.persistence_repository.common.model.enums.ProductStatus;
import com.example.persistence_repository.common.model.enums.RequestStatus;
import com.example.persistence_repository.common.model.enums.TransactionStatus;
import com.example.persistence_repository.common.repository.AccountRepository;
import com.example.persistence_repository.common.repository.AccountRequestRepository;
import com.example.persistence_repository.common.repository.CategoryRepository;
import com.example.persistence_repository.common.repository.ContractRepository;
import com.example.persistence_repository.common.repository.CustomerRepository;
import com.example.persistence_repository.common.repository.FeatureRepository;
import com.example.persistence_repository.common.repository.FeedbackRepository;
import com.example.persistence_repository.common.repository.InventoryItemRepository;
import com.example.persistence_repository.common.repository.ProductContractRepository;
import com.example.persistence_repository.common.repository.ProductExportedRepository;
import com.example.persistence_repository.common.repository.ProductRepository;
import com.example.persistence_repository.common.repository.ProductRequestRepository;
import com.example.persistence_repository.common.repository.ProductSpecificationRepository;
import com.example.persistence_repository.common.repository.ProductTransactionRepository;
import com.example.persistence_repository.common.repository.ProductWarehouseRepository;
import com.example.persistence_repository.common.repository.RequestLogRepository;
import com.example.persistence_repository.common.repository.RequestRepository;
import com.example.persistence_repository.common.repository.RoleFeatureRepository;
import com.example.persistence_repository.common.repository.RoleRepository;
import com.example.persistence_repository.common.repository.SpecificationRepository;
import com.example.persistence_repository.common.repository.SpecificationTypeRepository;
import com.example.persistence_repository.common.repository.StaffRepository;
import com.example.persistence_repository.common.repository.TypeRepository;
import com.example.persistence_repository.common.repository.WarehouseLogRepository;
import com.example.persistence_repository.common.repository.WarehouseRepository;
import com.example.persistence_repository.persistence.repository.CrudRepository;

import java.time.LocalDate;

/**
 * Utility class to populate the database with sample data for demo/testing.
 * Simple existence checks are performed to avoid duplicate inserts when
 * invoked multiple times in a dev environment.
 */
public final class SampleDataLoader {

    private SampleDataLoader() {
    }

    public static void loadAll() {
        try {
            // ROLES
            CrudRepository<Role, Long> roleRepo = new RoleRepository();
            if (!roleRepo.isExist(1L)) {
                Role admin = new Role();
                admin.setRoleID(1L);
                admin.setRoleName("ADMIN");
                roleRepo.save(admin);
            }
            if (!roleRepo.isExist(2L)) {
                Role user = new Role();
                user.setRoleID(2L);
                user.setRoleName("USER");
                roleRepo.save(user);
            }

            // FEATURES
            CrudRepository<Feature, Long> featureRepo = new FeatureRepository();
            if (!featureRepo.isExist(1L)) {
                Feature f = new Feature();
                f.setFeatureID(1L);
                f.setFeatureURL("/dashboard");
                f.setDescription("Dashboard access");
                featureRepo.save(f);
            }

            // ACCOUNTS
            CrudRepository<Account, String> accountRepo = new AccountRepository();
            if (!accountRepo.isExist("admin")) {
                Account acc = new Account();
                acc.setUsername("admin");
                acc.setPasswordHash("hashed_admin");
                acc.setAccountStatus(AccountStatus.Active);
                acc.setRoleID(1L);
                accountRepo.save(acc);
            }
            if (!accountRepo.isExist("user1")) {
                Account acc = new Account();
                acc.setUsername("user1");
                acc.setPasswordHash("hashed_user1");
                acc.setAccountStatus(AccountStatus.Active);
                acc.setRoleID(2L);
                accountRepo.save(acc);
            }

            // CATEGORY & TYPE & SPECIFICATION TYPE & SPECIFICATION
            CrudRepository<Category, Long> categoryRepo = new CategoryRepository();
            if (!categoryRepo.isExist(1L)) {
                Category c = new Category();
                c.setCategoryID(1L);
                c.setCategoryName("Electronics");
                c.setCategoryImage("electronics.png");
                categoryRepo.save(c);
            }

            CrudRepository<Type, Long> typeRepo = new TypeRepository();
            if (!typeRepo.isExist(1L)) {
                Type t = new Type();
                t.setTypeID(1L);
                t.setTypeName("Laptop");
                t.setTypeImage("laptop.png");
                t.setCategoryID(1L);
                typeRepo.save(t);
            }

            CrudRepository<SpecificationType, Long> specTypeRepo = new SpecificationTypeRepository();
            if (!specTypeRepo.isExist(1L)) {
                SpecificationType st = new SpecificationType();
                st.setSpecificationTypeID(1L);
                st.setSpecificationTypeName("Hardware");
                st.setTypeID(1L);
                specTypeRepo.save(st);
            }

            CrudRepository<Specification, Long> specRepo = new SpecificationRepository();
            if (!specRepo.isExist(1L)) {
                Specification s = new Specification();
                s.setSpecificationID(1L);
                s.setSpecificationName("RAM");
                s.setSpecificationValue("16GB");
                s.setSpecificationTypeID(1L);
                specRepo.save(s);
            }

            // PRODUCT
            CrudRepository<Product, Long> productRepo = new ProductRepository();
            if (!productRepo.isExist(1L)) {
                Product p = new Product();
                p.setProductID(1L);
                p.setProductName("Pro Laptop 15");
                p.setProductDescription("High-end developer laptop");
                p.setProductImage("pro15.png");
                p.setTypeID(1L);
                productRepo.save(p);
            }

            CrudRepository<ProductSpecification, Long> prodSpecRepo = new ProductSpecificationRepository();
            if (!prodSpecRepo.isExist(1L)) {
                ProductSpecification ps = new ProductSpecification();
                ps.setProductSpecificationID(1L);
                ps.setProductID(1L);
                ps.setSpecificationID(1L);
                prodSpecRepo.save(ps);
            }

            // WAREHOUSE
            CrudRepository<Warehouse, Long> warehouseRepo = new WarehouseRepository();
            if (!warehouseRepo.isExist(1L)) {
                Warehouse w = new Warehouse();
                w.setWarehouseID(1L);
                w.setWarehouseName("Main DC");
                w.setLocation("City A");
                w.setWarehouseManager("admin");
                warehouseRepo.save(w);
            }

            // INVENTORY ITEM
            CrudRepository<InventoryItem, Long> invRepo = new InventoryItemRepository();
            if (!invRepo.isExist(1L)) {
                InventoryItem item = new InventoryItem();
                item.setItemId(1L);
                item.setSerialNumber("SN-001");
                item.setProductID(1L);
                invRepo.save(item);
            }

            // PRODUCT WAREHOUSE
            CrudRepository<ProductWarehouse, Long> productWarehouseRepo = new ProductWarehouseRepository();
            if (!productWarehouseRepo.isExist(1L)) {
                ProductWarehouse pw = new ProductWarehouse();
                pw.setProductWarehouseID(1L);
                pw.setItemID(1L);
                pw.setWarehouseID(1L);
                pw.setProductStatus(ProductStatus.In_stock);
                productWarehouseRepo.save(pw);
            }

            // CUSTOMER
            CrudRepository<Customer, Long> customerRepo = new CustomerRepository();
            if (!customerRepo.isExist(1L)) {
                Customer cust = new Customer();
                cust.setCustomerID(1L);
                cust.setCustomerName("Alice");
                cust.setAddress("123 Main St");
                cust.setPhone("1234567890");
                cust.setEmail("alice@example.com");
                cust.setUsername("user1");
                customerRepo.save(cust);
            }

            // CONTRACT
            CrudRepository<Contract, Long> contractRepo = new ContractRepository();
            if (!contractRepo.isExist(1L)) {
                Contract contract = new Contract();
                contract.setContractID(1L);
                contract.setCustomerID(1L);
                contract.setContractImage("contract1.png");
                contract.setStartDate(Date.valueOf(LocalDate.now()));
                contract.setExpiredDate(Date.valueOf(LocalDate.now().plusMonths(12)));
                contractRepo.save(contract);
            }

            // REQUEST
            CrudRepository<Request, Long> requestRepo = new RequestRepository();
            if (!requestRepo.isExist(1L)) {
                Request r = new Request();
                r.setRequestID(1L);
                r.setContractID(1L);
                r.setRequestDescription("Initial setup");
                r.setRequestStatus(RequestStatus.Pending);
                r.setStartDate(LocalDateTime.now());
                requestRepo.save(r);
            }

            // ACCOUNT REQUEST
            CrudRepository<AccountRequest, Long> accReqRepo = new AccountRequestRepository();
            if (!accReqRepo.isExist(1L)) {
                AccountRequest ar = new AccountRequest();
                ar.setAccountRequestID(1L);
                ar.setUsername("user1");
                ar.setRequestID(1L);
                accReqRepo.save(ar);
            }

            // REQUEST LOG
            CrudRepository<RequestLog, Long> requestLogRepo = new RequestLogRepository();
            if (!requestLogRepo.isExist(1L)) {
                RequestLog rl = new RequestLog();
                rl.setRequestLogID(1L);
                rl.setRequestID(1L);
                rl.setUsername("admin");
                rl.setNewStatus(RequestStatus.Pending);
                rl.setActionDate(Date.valueOf(LocalDate.now()));
                requestLogRepo.save(rl);
            }

            // PRODUCT REQUEST
            CrudRepository<ProductRequest, Long> productRequestRepo = new ProductRequestRepository();
            if (!productRequestRepo.isExist(1L)) {
                ProductRequest pr = new ProductRequest();
                pr.setProductRequestID(1L);
                pr.setProductID(1L);
                pr.setWarehouseID(1L);
                pr.setRequestID(1L);
                pr.setQuantity(5);
                pr.setRequestDate(Date.valueOf(LocalDate.now()));
                pr.setStatus(ProductRequestStatus.Pending);
                productRequestRepo.save(pr);
            }

            // WAREHOUSE LOG (after product request)
            CrudRepository<WarehouseLog, Long> warehouseLogRepo = new WarehouseLogRepository();
            if (!warehouseLogRepo.isExist(1L)) {
                WarehouseLog wl = new WarehouseLog();
                wl.setWarehouseLogID(1L);
                wl.setWarehouseID(1L);
                wl.setProductRequestID(1L);
                wl.setLogDate(Date.valueOf(LocalDate.now()));
                wl.setDescription("Initial stock move");
                warehouseLogRepo.save(wl);
            }

            // PRODUCT EXPORTED (depends on existing WarehouseLog)
            CrudRepository<ProductExported, Long> exportedRepo = new ProductExportedRepository();
            if (!exportedRepo.isExist(1L)) {
                ProductExported pe = new ProductExported();
                pe.setProductWarehouseID(1L);
                pe.setWarehouseLogID(1L); // now guaranteed to exist
                exportedRepo.save(pe);
            }

            // PRODUCT TRANSACTION
            CrudRepository<ProductTransaction, Long> txnRepo = new ProductTransactionRepository();
            if (!txnRepo.isExist(1L)) {
                ProductTransaction tx = new ProductTransaction();
                tx.setTransactionID(1L);
                tx.setItemID(1L);
                tx.setTransactionDate(LocalDateTime.now());
                tx.setTransactionStatus(TransactionStatus.Export);
                tx.setSourceWarehouseID(1L);
                txnRepo.save(tx);
            }

            // ROLE FEATURE mapping
            CrudRepository<RoleFeature, Long> rfRepo = new RoleFeatureRepository();
            if (!rfRepo.isExist(1L)) {
                RoleFeature rf = new RoleFeature();
                rf.setRoleFeatureID(1L);
                rf.setRoleID(1L);
                rf.setFeatureID(1L);
                rfRepo.save(rf);
            }

            // STAFF
            CrudRepository<Staff, Long> staffRepo = new StaffRepository();
            if (!staffRepo.isExist(1L)) {
                Staff s = new Staff();
                s.setStaffID(1L);
                s.setStaffName("Bob Admin");
                s.setEmail("bob.admin@example.com");
                s.setPhone("5550001");
                s.setUsername("admin");
                staffRepo.save(s);
            }

            // FEEDBACK
            CrudRepository<Feedback, Long> feedbackRepo = new FeedbackRepository();
            if (!feedbackRepo.isExist(1L)) {
                Feedback fb = new Feedback();
                fb.setFeedbackID(1L);
                fb.setCustomerID("user1");
                fb.setContent("Great service so far");
                fb.setRating(5);
                fb.setFeedbackDate(LocalDateTime.now());
                feedbackRepo.save(fb);
            }

            // PRODUCT CONTRACT (link contract to item)
            CrudRepository<ProductContract, Long> productContractRepo = new ProductContractRepository();
            if (!productContractRepo.isExist(1L)) {
                ProductContract pc = new ProductContract();
                pc.setContractID(1L);
                pc.setItemID(1L);
                productContractRepo.save(pc);
            }

            System.out.println("Sample data load completed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
