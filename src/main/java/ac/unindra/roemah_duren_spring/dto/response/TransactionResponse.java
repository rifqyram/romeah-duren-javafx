package ac.unindra.roemah_duren_spring.dto.response;

import ac.unindra.roemah_duren_spring.model.Transaction;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {
    private String id;
    private AdminResponse admin;
    private CustomerResponse customer;
    private BranchResponse branch;
    private BranchResponse targetBranch;
    private List<TransactionDetailResponse> transactionDetails;
    private String transDate;
    private String transactionType;
    private Long totalPrice;

    public Transaction toTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setAdmin(admin.toAdmin());
        transaction.setCustomer(customer != null ? customer.toCustomer() : null);
        transaction.setBranch(branch.toBranch());
        transaction.setTargetBranch(targetBranch != null ? targetBranch.toBranch() : null);
        transaction.setTransDate(transDate);
        transaction.setTransactionType(transactionType);
        transaction.setTotalPrice(totalPrice);
        transaction.setTransactionDetails(transactionDetails.stream().map(TransactionDetailResponse::toTransactionDetail).collect(Collectors.toList()));
        return transaction;
    }
}
