package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PayrollMessage {

    @JsonProperty("source_name")
    private String sourceName;

    @JsonProperty("record_type")
    private String recordType;

    @JsonProperty("business_key")
    private String businessKey;

    @JsonProperty("hash_sha256")
    private String hashSha256;

    @JsonProperty("payload")
    private Object payload;

    // --- HAI TRƯỜNG MỚI THÊM ---
    @JsonProperty("status")
    private String status; // Giá trị sẽ là "VALID" hoặc "INVALID"

    @JsonProperty("error_msg")
    private String errorMsg; // Lý do lỗi (nếu có)

    // Constructor mặc định (Bắt buộc cho Jackson)
    public PayrollMessage() {}

    // Constructor đầy đủ mới
    public PayrollMessage(String sourceName, String recordType, String businessKey, String hashSha256, Object payload, String status, String errorMsg) {
        this.sourceName = sourceName;
        this.recordType = recordType;
        this.businessKey = businessKey;
        this.hashSha256 = hashSha256;
        this.payload = payload;
        this.status = status;
        this.errorMsg = errorMsg;
    }

    // --- GETTERS & SETTERS ---
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }

    public String getBusinessKey() { return businessKey; }
    public void setBusinessKey(String businessKey) { this.businessKey = businessKey; }

    public String getHashSha256() { return hashSha256; }
    public void setHashSha256(String hashSha256) { this.hashSha256 = hashSha256; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }

    // Getter/Setter cho 2 trường mới
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}