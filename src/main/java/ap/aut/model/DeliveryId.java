package ap.aut.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DeliveryId implements Serializable {
    @Column(name = "email_id")
    private int emailId;

    @Column(name = "recipient_id")
    private int recipientId;

    public DeliveryId() { }

    public DeliveryId(int emailId, int recipientId) {
        this.emailId = emailId;
        this.recipientId = recipientId;
    }

    public int getEmailId() { return emailId; }
    public void setEmailId(int emailId) { this.emailId = emailId; }

    public int getRecipientId() { return recipientId; }
    public void setRecipientId(int recipientId) { this.recipientId = recipientId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeliveryId)) return false;
        DeliveryId that = (DeliveryId) o;
        return emailId == that.emailId && recipientId == that.recipientId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailId, recipientId);
    }
}
