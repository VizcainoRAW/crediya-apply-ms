package co.com.crediya.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("loan_types")
public class LoanTypeEntity {
    
    
    @Id
    private UUID id;
    
    private String name;

    private BigDecimal anualNominalRate;

    private boolean autoValidation;
}