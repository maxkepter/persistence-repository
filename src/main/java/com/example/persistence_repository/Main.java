package com.example.persistence_repository;

import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;

import com.example.persistence_repository.dao.AuthorRepository;
import com.example.persistence_repository.dao.BookRepository;
import com.example.persistence_repository.dao.LoanRepository;
import com.example.persistence_repository.dao.MemberRepository;
import com.example.persistence_repository.entity.Author;
import com.example.persistence_repository.entity.Book;
import com.example.persistence_repository.entity.Loan;
import com.example.persistence_repository.entity.Member;
import com.example.persistence_repository.persistence.config.TransactionManager;
import com.example.persistence_repository.persistence.entity.EntityRegistry;
import com.example.persistence_repository.persistence.entity.SchemaGenerator;
import com.example.persistence_repository.persistence.entity.SchemaGenerator.Options;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== DEMO CRUD & LAZY/EAGER === (Chu y: can MySQL chay theo RepositoryConfig)");
        try {
            initSchema();
            runDemo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initSchema() throws Exception {
        // Đăng ký entity 1 lần
        EntityRegistry.register(Author.class);
        EntityRegistry.register(Member.class);
        EntityRegistry.register(Book.class);
        EntityRegistry.register(Loan.class);

        TransactionManager.beginTransaction();
        try {
            // Sinh DDL tự động
            Options opt = new Options();
            opt.dropIfExists = true;
            opt.printDdl = true; // có thể tắt nếu không muốn in
            SchemaGenerator generator = new SchemaGenerator(opt);
            generator.generateAll();
            TransactionManager.commit();
            System.out.println("Schema ready (auto-generated with cascade).");
        } catch (Exception ex) {
            TransactionManager.rollback();
            throw ex;
        }
    }

    private static void runDemo() throws Exception {
        AuthorRepository authorRepo = new AuthorRepository();
        BookRepository bookRepo = new BookRepository();
        MemberRepository memberRepo = new MemberRepository();
        LoanRepository loanRepo = new LoanRepository();

        // CLEAN dữ liệu cũ (đã có drop schema ở init, nên phần này chỉ phòng trường hợp
        // run lại không drop)
        TransactionManager.beginTransaction();
        try (Statement st = TransactionManager.getConnection().createStatement()) {
            st.executeUpdate("DELETE FROM Loan");
            st.executeUpdate("DELETE FROM Book");
            st.executeUpdate("DELETE FROM Author");
            st.executeUpdate("DELETE FROM Member");
            TransactionManager.commit();
        } catch (Exception e) {
            TransactionManager.rollback();
            throw e;
        }

        // INSERT (tự cấp ID) + tự gán ngày nếu muốn
        TransactionManager.beginTransaction();
        Author a1;
        Author a2;
        Member m1;
        Member m2;
        Book b1;
        Book b2;
        Book b3;
        Loan l1;
        Loan l2;
        try {
            a1 = new Author("J. K. Rowling", Date.valueOf(LocalDate.of(1965, 7, 31)), "UK");
            a1.setAuthorID(1);
            authorRepo.save(a1);
            a2 = new Author("George R. R. Martin", Date.valueOf(LocalDate.of(1948, 9, 20)), "USA");
            a2.setAuthorID(2);
            authorRepo.save(a2);

            m1 = new Member("Alice", "alice@example.com");
            m1.setMemberID(1);
            memberRepo.save(m1);
            m2 = new Member("Bob", "bob@example.com");
            m2.setMemberID(2);
            memberRepo.save(m2);

            b1 = new Book("Harry Potter 1", "ISBN-HP1", 1997, a1.getAuthorID());
            b1.setBookID(1);
            bookRepo.save(b1);
            b2 = new Book("Harry Potter 2", "ISBN-HP2", 1998, a1.getAuthorID());
            b2.setBookID(2);
            bookRepo.save(b2);
            b3 = new Book("Game of Thrones", "ISBN-GOT", 1996, a2.getAuthorID());
            b3.setBookID(3);
            bookRepo.save(b3);

            l1 = new Loan(b1.getBookID(), m1.getMemberID());
            l1.setLoanID(1);
            l1.setLoanDate(Date.valueOf(LocalDate.now()));
            loanRepo.save(l1);
            l2 = new Loan(b3.getBookID(), m2.getMemberID());
            l2.setLoanID(2);
            l2.setLoanDate(Date.valueOf(LocalDate.now()));
            loanRepo.save(l2);

            TransactionManager.commit();
            System.out.println("Insert done (IDs tự cấp)");
        } catch (Exception e) {
            TransactionManager.rollback();
            throw e;
        }

        // SELECT & EAGER/LAZY TEST
        TransactionManager.beginTransaction();
        try {
            Book book = bookRepo.findById(b1.getBookID());
            System.out.println("Book fetched: " + book);
            System.out
                    .println("Book.author (EAGER) = " + (book.getAuthor() != null ? book.getAuthor().getName() : null));

            Loan loan = loanRepo.findById(l1.getLoanID());
            System.out.println("Loan fetched: " + loan);
            System.out.println(
                    "Loan.member (EAGER) = " + (loan.getMember() != null ? loan.getMember().getFullName() : null));
            System.out.println("Loan.book loaded before access? " + (loan != null && loan.isBookLoaded()));
            System.out.println("Loan.book after access: " + (loan != null ? loan.getBook() : null));
            System.out.println("Loan.book loaded now? " + (loan != null && loan.isBookLoaded()));
            TransactionManager.commit();
        } catch (Exception e) {
            TransactionManager.rollback();
            throw e;
        }

        // UPDATE
        TransactionManager.beginTransaction();
        try {
            Author aUpdate = authorRepo.findById(a1.getAuthorID());
            if (aUpdate != null) {
                aUpdate.setNationality("United Kingdom");
                authorRepo.update(aUpdate);
            }
            TransactionManager.commit();
            System.out.println("Update done");
        } catch (Exception e) {
            TransactionManager.rollback();
            throw e;
        }

        // DELETE (sẽ cascade xóa Book và Loan liên quan)
        TransactionManager.beginTransaction();
        try {
            if (a2.getAuthorID() != null) {
                authorRepo.deleteById(a2.getAuthorID());
                System.out.println("Deleted author id=" + a2.getAuthorID() + " (cascade to books/loans)");
            }
            TransactionManager.commit();
        } catch (Exception e) {
            TransactionManager.rollback();
            throw e;
        }

        int totalAuthor = authorRepo.count();
        System.out.println("Total Author left = " + totalAuthor);
        System.out.println("=== DEMO DONE ===");
    }
}