package it.unimib.sd2024.model;

import org.json.JSONObject;

public class Order {
    private String domain;
    private String userId;
    private int price;
    private long date;
    private String cvv;
    private String cardNumber;
    private String operationType;

    public enum OperationType {
        RENEWAL, PURCHASE
    }
    
    public Order () { }

    public Order(String domain, String userId, int price, long date, String cvv, String cardNumber, String operationType) {
        this.domain = domain;
        this.userId = userId;
        this.price = price; 
        this.date = date;
        this.cvv = cvv;
        this.cardNumber = cardNumber;
        this.operationType = operationType;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("domain", this.domain);
        jsonObject.put("userId", this.userId);
        jsonObject.put("price", this.price);
        jsonObject.put("date", this.date);
        jsonObject.put("cvv", this.cvv);
        jsonObject.put("cardNumber", this.cardNumber);
        jsonObject.put("operationType", this.operationType);

        return jsonObject.toString();
    }
}
