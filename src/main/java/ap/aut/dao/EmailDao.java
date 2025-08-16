package ap.aut.dao;

import ap.aut.model.Delivery;
import ap.aut.model.Email;
import ap.aut.model.User;
import ap.aut.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class EmailDao {

    public void save(Email email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(email);
            tx.commit();
        }
    }

    public List<Email> getUnreadEmails(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                select d.email from Delivery d
                where d.recipient = :user and d.isRead = false
                order by d.email.sentTime desc
            """;
            return session.createQuery(hql, Email.class)
                    .setParameter("user", user)
                    .list();
        }
    }

    public List<Email> getAllEmails(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                select d.email from Delivery d
                where d.recipient = :user
                order by d.email.sentTime desc
            """;
            return session.createQuery(hql, Email.class)
                    .setParameter("user", user)
                    .list();
        }
    }

    public Optional<Delivery> findDeliveryByUserAndCode(User user, String code) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                from Delivery d
                where d.recipient = :user and d.email.code = :code
            """;
            return session.createQuery(hql, Delivery.class)
                    .setParameter("user", user)
                    .setParameter("code", code)
                    .uniqueResultOptional();
        }
    }

    public void updateDelivery(Delivery delivery) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(delivery);
            tx.commit();
        }
    }
}
