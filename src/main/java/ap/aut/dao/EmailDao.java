package ap.aut.dao;

import ap.aut.model.Delivery;
import ap.aut.model.Email;
import ap.aut.model.User;
import ap.aut.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class EmailDao {

    private Session session() {
        return HibernateUtil.getSessionFactory().openSession();
    }

    public void save(Email email) {
        try (Session session = session()) {
            Transaction tx = session.beginTransaction();
            session.persist(email);
            tx.commit();
        }
    }

    public List<Email> findByRecipient(User user) {
        try (Session session = session()) {
            String hql = "select distinct e from Email e " +
                    "join fetch e.deliveries d " +
                    "where d.recipient = :user " +
                    "order by e.sentTime desc";
            return session.createQuery(hql, Email.class)
                    .setParameter("user", user)
                    .list();
        }
    }

    public List<Email> findBySender(User sender) {
        try (Session session = session()) {
            String hql = "select distinct e from Email e " +
                    "left join fetch e.deliveries " +
                    "where e.sender = :sender " +
                    "order by e.sentTime desc";
            return session.createQuery(hql, Email.class)
                    .setParameter("sender", sender)
                    .list();
        }
    }

    public List<Email> findUnreadByRecipient(User user) {
        try (Session session = session()) {
            String hql = "select distinct e from Email e " +
                    "join fetch e.deliveries d " +
                    "where d.recipient = :user and d.isRead = false " +
                    "order by e.sentTime desc";
            return session.createQuery(hql, Email.class)
                    .setParameter("user", user)
                    .list();
        }
    }

    public Optional<Email> findByCode(String code) {
        try (Session session = session()) {
            String hql = "from Email e where e.code = :code";
            return session.createQuery(hql, Email.class)
                    .setParameter("code", code)
                    .uniqueResultOptional();
        }
    }

    public void markDeliveryAsRead(User user, Email email) {
        try (Session session = session()) {
            Transaction tx = session.beginTransaction();
            String hql = "update Delivery d set d.readAt = current_timestamp " +
                    "where d.recipient = :user and d.email = :email and d.isRead = false";
            session.createQuery(hql)
                    .setParameter("user", user)
                    .setParameter("email", email)
                    .executeUpdate();
            tx.commit();
        }
    }
}