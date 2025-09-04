package co.com.crediya.r2dbc.entity;


import co.com.crediya.model.loanapplication.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("loan_applications")
public class LoanApplicationEntity implements Persistable<UUID> {
    
    @Id
    private UUID id;
    
    @Column("user_id")
    private String userId;
    
    @Column("loan_type_id")
    private UUID loanTypeId;
    
    private BigDecimal amount;
    
    @Column("term_months")
    private Integer termMonths;
    
    private ApplicationStatus status;
    
    @Column("created_at")
    private LocalDateTime createdAt;

    @Transient
    private boolean isNew;

    @Override
    public boolean isNew() {
        return this.isNew || createdAt == null;
    }

    public LoanApplicationEntity setAsNew() {
        this.isNew = true;
        return this;
    }
}