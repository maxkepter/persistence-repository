package com.example.persistence_repository.common.model;

import com.example.persistence_repository.common.model.enums.RequestStatus;
import com.example.persistence_repository.common.model.enums.converter.RequestStatusConverter;
import com.example.persistence_repository.persistence.annotation.*;
import com.example.persistence_repository.persistence.entity.convert.Convert;
import com.example.persistence_repository.persistence.entity.load.LazyReference;
import com.example.persistence_repository.persistence.entity.relation.FetchMode;

import java.time.LocalDateTime;
import java.util.List;

@Entity(tableName = "Request")
public class Request {
    @Key
    @Column(name = "RequestID", type = "BIGINT")
    private Long requestID;

    @Column(name = "RequestDescription", length = 255)
    private String requestDescription;

    @Column(name = "RequestStatus")
    @Convert(converter = RequestStatusConverter.class)
    private RequestStatus requestStatus;

    @Column(name = "StartDate", type = "DATETIME", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "FinishedDate", type = "DATETIME")
    private LocalDateTime finishedDate;

    @Column(name = "Note", length = 255)
    private String note;

    @Column(name = "ContractID", type = "BIGINT", nullable = false)
    private Long contractID; // corrected relationship to ContractID

    @ManyToOne(joinColumn = "ContractID", fetch = FetchMode.EAGER)
    private LazyReference<Contract> contract;

    @OneToMany(mappedBy = "requestID", joinColumn = "RequestID", fetch = FetchMode.LAZY)
    private List<RequestLog> logs;

    public Request() {
    }

    public Request(Long requestID, String requestDescription, RequestStatus requestStatus, LocalDateTime startDate,
            Long contractID) {
        this.requestID = requestID;
        this.requestDescription = requestDescription;
        this.requestStatus = requestStatus;
        this.startDate = startDate;
        this.contractID = contractID;
    }

    public Long getRequestID() {
        return requestID;
    }

    public void setRequestID(Long requestID) {
        this.requestID = requestID;
    }

    public String getRequestDescription() {
        return requestDescription;
    }

    public void setRequestDescription(String requestDescription) {
        this.requestDescription = requestDescription;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(LocalDateTime finishedDate) {
        this.finishedDate = finishedDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getContractID() {
        return contractID;
    }

    public void setContractID(Long contractID) {
        this.contractID = contractID;
    }

    public Contract getContract() {
        return contract.get();
    }

    public void setContract(Contract contract) {
        this.contract.setValue(contract);
    }

    public List<RequestLog> getLogs() {
        return logs;
    }

    public void setLogs(List<RequestLog> logs) {
        this.logs = logs;
    }
}
