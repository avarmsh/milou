package ap.aut.service;

import ap.aut.dao.EmailDao;
import ap.aut.dao.UserDao;
import ap.aut.model.Delivery;
import ap.aut.model.Email;
import ap.aut.model.User;

import java.time.LocalDateTime;
import java.util.*;

public class MilouService {
    private final UserDao userDao;
    private final EmailDao emailDao;

    public MilouService(UserDao userDao, EmailDao emailDao) {
        this.userDao = userDao;
        this.emailDao = emailDao;
    }

    public boolean register(String name, String email, String password, List<String> errors) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            errors.add("All fields are required.");
            return false;
        }
        if (userDao.findByEmail(email).isPresent()) {
            errors.add("Email already registered.");
            return false;
        }
        User user = new User(name, email, password);
        userDao.save(user);
        return true;
    }

    public Optional<User> login(String email, String password) {
        return userDao.findByEmail(email)
                .filter(u -> u.getPassword().equals(password));
    }

    public String sendEmail(User sender, List<String> recipientEmails, String subject, String body) {
        Email email = new Email(UUID.randomUUID().toString().substring(0, 6), sender, subject, body, false, false, null);
        List<Delivery> deliveries = new ArrayList<>();
        for (String recEmail : recipientEmails) {
            Optional<User> recipient = userDao.findByEmail(recEmail.trim());
            recipient.ifPresent(r -> deliveries.add(new Delivery(email, r)));
        }
        email.getDeliveries().addAll(deliveries);
        emailDao.save(email);
        return email.getCode();
    }

    public List<Email> getUnreadEmails(User user) {
        return emailDao.getUnreadEmails(user);
    }

    public List<Email> getAllEmails(User user) {
        return emailDao.getAllEmails(user);
    }

    public Optional<Email> openEmail(User user, String code) {
        Optional<Delivery> deliveryOpt = emailDao.findDeliveryByUserAndCode(user, code);
        deliveryOpt.ifPresent(delivery -> {
            delivery.setRead(true);
            emailDao.updateDelivery(delivery);
        });
        return deliveryOpt.map(Delivery::getEmail);
    }

    public String replyEmail(User replier, String originalEmailCode, String body) {
        Optional<Delivery> originalDeliveryOpt = emailDao.findDeliveryByUserAndCode(replier, originalEmailCode);
        if (originalDeliveryOpt.isEmpty()) return null;

        Email original = originalDeliveryOpt.get().getEmail();
        String subject = "Re: " + original.getSubject();
        String code = UUID.randomUUID().toString().substring(0, 6);
        Email reply = new Email(code, replier, subject, body, true, false, original);
        Delivery delivery = new Delivery(reply, original.getSender());
        reply.getDeliveries().add(delivery);
        emailDao.save(reply);
        return code;
    }

    public String forwardEmail(User forwarder, String originalEmailCode, List<String> recipients) {
        Optional<Delivery> originalDeliveryOpt = emailDao.findDeliveryByUserAndCode(forwarder, originalEmailCode);
        if (originalDeliveryOpt.isEmpty()) return null;

        Email original = originalDeliveryOpt.get().getEmail();
        String subject = "Fwd: " + original.getSubject();
        String code = UUID.randomUUID().toString().substring(0, 6);
        Email forwarded = new Email(code, forwarder, subject, original.getBody(), false, true, original);
        List<Delivery> deliveryList = new ArrayList<>();

        for (String recEmail : recipients) {
            Optional<User> recipient = userDao.findByEmail(recEmail.trim());
            recipient.ifPresent(r -> deliveryList.add(new Delivery(forwarded, r)));
        }
        forwarded.getDeliveries().addAll(deliveryList);
        emailDao.save(forwarded);
        return code;
    }
}
