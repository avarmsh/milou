package ap.aut.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emails")
public class Email {

    @Id
    private String code;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 1000)
    private String body;

    @Column(nullable = false)
    private boolean isReply;

    @Column(nullable = false)
    private boolean isForward;

    @ManyToOne
    @JoinColumn(name = "original_email_code")
    private Email originalEmail;

    @Column(name = "sent_time", nullable = false)
    private LocalDateTime sentTime;

    @OneToMany(mappedBy = "email", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Delivery> deliveries = new ArrayList<>();

    public Email() {
        this.sentTime = LocalDateTime.now();
    }

    public Email(String code, User sender, String subject, String body,
                 boolean isReply, boolean isForward, Email originalEmail) {
        this.code = code;
        this.sender = sender;
        this.subject = subject;
        this.body = body;
        this.isReply = isReply;
        this.isForward = isForward;
        this.originalEmail = originalEmail;
        this.sentTime = LocalDateTime.now();
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public boolean isReply() { return isReply; }
    public void setReply(boolean reply) { isReply = reply; }

    public boolean isForward() { return isForward; }
    public void setForward(boolean forward) { isForward = forward; }

    public Email getOriginalEmail() { return originalEmail; }
    public void setOriginalEmail(Email originalEmail) { this.originalEmail = originalEmail; }

    public LocalDateTime getSentTime() { return sentTime; }
    public void setSentTime(LocalDateTime sentTime) { this.sentTime = sentTime; }

    public List<Delivery> getDeliveries() { return deliveries; }
    public void setDeliveries(List<Delivery> deliveries) { this.deliveries = deliveries; }
}
