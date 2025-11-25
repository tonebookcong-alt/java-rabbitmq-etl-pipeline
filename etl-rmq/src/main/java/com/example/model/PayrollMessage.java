package com.example.model;

// Đây cũng là một record, nó sẽ được thư viện Jackson tự động chuyển thành JSON
// Tên các trường (ví dụ: sourceName) sẽ được chuyển thành (source_name)
// Nhưng để chắc chắn, chúng ta nên dùng lớp (class)

import com.fasterxml.jackson.annotation.JsonProperty;

// Dùng class thay vì record để Jackson có thể set/get
public class PayrollMessage {

    @JsonProperty("source_name")
    private String sourceName;

    @JsonProperty("record_type")
    private String recordType;

    @JsonProperty("business_key")
    private String businessKey;

    @JsonProperty("hash_sha256")
    private String hashSha256;

    @JsonProperty("payload") // Dữ liệu payload có thể là Employee, Attendance...
    private Object payload;

    // Constructor, Getters, Setters
    // Jackson cần constructor rỗng
    public PayrollMessage() {}

    public PayrollMessage(String sourceName, String recordType, String businessKey, String hashSha256, Object payload) {
        this.sourceName = sourceName;
        this.recordType = recordType;
        this.businessKey = businessKey;
        this.hashSha256 = hashSha256;
        this.payload = payload;
    }

    // Getters và Setters (Jackson cần)
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
}