import entity.Author;
import entity.Book;
import entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import util.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        List<User> users = new ArrayList<>(500);

        for (int i = 0; i < 500; i++) {
            User user = new User("entity.User " + i, String.format("user%s@example.com", i));
            users.add(user);
        }

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            int batchSize = 50;
            for (int i = 0; i < users.size(); i++) {
                session.persist(users.get(i));

                if (i > 0 && i % batchSize == 0) {
                    session.flush();
                    session.clear();
                }
            }

            transaction.commit();
        }

        Author author1 = new Author("Фёдор Достоевский");
        Author author2 = new Author("Лев Толстой");

        Book book1 = new Book("Идиот");
        Book book2 = new Book("Преступление и наказание");
        Book book3 = new Book("Братья Карамазовы");
        Book book4 = new Book("Анна Каренина");
        Book book5 = new Book("В чём моя вера?");

        author1.addBook(book1);
        author1.addBook(book2);
        author1.addBook(book3);
        author2.addBook(book4);
        author2.addBook(book5);
        author2.addBook(book1);

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(author1);
            session.persist(author2);
            session.flush();
            session.getTransaction().commit();
        }

        System.out.println("Всё добавлено");

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
//            session.persist(author1);
            session.remove(author1);
            session.flush();
            session.getTransaction().commit();
        }

        sessionFactory.close();
    }
}
