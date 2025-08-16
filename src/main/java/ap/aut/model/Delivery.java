package ap.aut.model;

import jakarta.persistence.*;

@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "email_id", nullable = false)
    private Email email;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public Delivery() {}

    public Delivery(Email email, User recipient) {
        this.email = email;
        this.recipient = recipient;
        this.isRead = false;
    }

    public Long getId() { return id; }

    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = email; }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
