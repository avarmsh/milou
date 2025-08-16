package ap.aut.service;

import ap.aut.dao.EmailDao;
import ap.aut.dao.UserDao;
import ap.aut.model.Email;
import ap.aut.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        Email email = new Email(UUID.randomUUID().toString().substring(0, 6),
                sender, subject, body, false, false, null);

        recipientEmails.forEach(rec -> userDao.findByEmail(rec.trim())
                .ifPresent(r -> email.getDeliveries().add(new ap.aut.model.Delivery(email, r))));

        emailDao.save(email);
        return email.getCode();
    }

    public List<Email> getAllEmails(User user) {
        return emailDao.findByRecipient(user);
    }

    public List<Email> getUnreadEmails(User user) {
        return emailDao.findUnreadByRecipient(user);
    }

    public List<Email> getSentEmails(User user) {
        return emailDao.findBySender(user);
    }

    public Optional<Email> openEmail(User user, String code) {
        Optional<Email> emailOpt = emailDao.findByCode(code);

        if (emailOpt.isEmpty()) return Optional.empty();

        Email email = emailOpt.get();

        boolean isSender = email.getSender().getId() == user.getId();
        boolean isRecipient = email.getDeliveries().stream()
                .anyMatch(d -> d.getRecipient().getId() == user.getId());

        if (isSender || isRecipient) {
            emailDao.markDeliveryAsRead(user, email);
            return Optional.of(email);
        } else {
            return Optional.empty();
        }
    }

    public String replyEmail(User replier, String originalEmailCode, String body) {
        Optional<Email> originalOpt = openEmail(replier, originalEmailCode);
        if (originalOpt.isEmpty()) return null;

        Email original = originalOpt.get();
        String code = UUID.randomUUID().toString().substring(0, 6);
        Email reply = new Email(code, replier, "Re: " + original.getSubject(), body,
                true, false, original);

        reply.getDeliveries().add(new ap.aut.model.Delivery(reply, original.getSender()));
        emailDao.save(reply);
        return code;
    }

    public String forwardEmail(User forwarder, String originalEmailCode, List<String> recipients) {
        Optional<Email> originalOpt = openEmail(forwarder, originalEmailCode);
        if (originalOpt.isEmpty()) return null;

        Email original = originalOpt.get();
        String code = UUID.randomUUID().toString().substring(0, 6);
        Email fwd = new Email(code, forwarder, "Fwd: " + original.getSubject(),
                original.getBody(), false, true, original);

        recipients.forEach(rec -> userDao.findByEmail(rec.trim())
                .ifPresent(r -> fwd.getDeliveries().add(new ap.aut.model.Delivery(fwd, r))));

        emailDao.save(fwd);
        return code;
    }
}