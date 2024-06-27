package it.unimib.sd2024.model;

import org.json.JSONObject;

public class Domain {
    private String domainName;
    private String userId;
    private long purchaseDate;
    private long lastRenewed;
    private long expirationDate;

    public Domain() {}

    public Domain(String domainName, String userId, long expirationDate, long purchaseDate, long lastRenewed) {
        this.domainName = domainName;
        this.purchaseDate = purchaseDate;
        this.userId = userId;
        this.expirationDate = expirationDate;
        this.lastRenewed = lastRenewed;
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

    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public long getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(long purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public long getLastRenewed() {
        return lastRenewed;
    }

    public void setLastRenewed(long lastRenewed) {
        this.lastRenewed = lastRenewed;
    }

    @Override
    public String toString() {
        return "Domain{" +
                ", name='" + domainName + '\'' +
                ", userId='" + userId + '\'' +
                ", lastRenewed='" + lastRenewed + '\'' +
                ", expirationDate=" + expirationDate +
                '}';
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("domainName", this.domainName);
        jsonObject.put("userId", this.userId);
        jsonObject.put("purchaseDate", this.purchaseDate);
        jsonObject.put("expirationDate", this.expirationDate);
        jsonObject.put("lastRenewed", this.lastRenewed);

        return jsonObject.toString();
    }
}
