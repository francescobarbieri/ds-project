package it.unimib.sd2024.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Domain {
    private String domainName;
    private String userId;
    private long purchaseDate;
    private long expiryDate;
    private boolean isAvailable;

    public Domain() {}

    public Domain(String domainName, String userId, boolean isAvailable) {
        this.domainName = domainName;
        this.userId = userId;
        this.isAvailable = isAvailable;
    }

    public Domain(String domainName, String userId, long expiryDate, long purchaseDate, boolean isAvailable) {
        this.domainName = domainName;
        this.purchaseDate = purchaseDate;
        this.userId = userId;
        this.expiryDate = expiryDate;
        this.isAvailable = isAvailable;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(long expiryDate) {
        this.expiryDate = expiryDate;
    }

    public long getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(long purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    @Override
    public String toString() {
        return "Domain{" +
                ", name='" + domainName + '\'' +
                ", userId='" + userId + '\'' +
                ", available='" + isAvailable + '\'' +
                ", expiryDate=" + expiryDate +
                '}';
    }

    public String toJSON() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }
}
