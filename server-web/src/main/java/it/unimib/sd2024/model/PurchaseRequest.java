package it.unimib.sd2024.model;

public class PurchaseRequest {
    private User user;
    private Domain domain;
    // private Card card;

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
