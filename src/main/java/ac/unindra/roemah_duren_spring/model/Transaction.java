package ac.unindra.roemah_duren_spring.model;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private StringProperty id = new SimpleStringProperty();

    @Getter
    @Setter
    private Admin admin;

    @Setter
    @Getter
    private Customer customer;

    @Setter
    @Getter
    private Branch branch;

    @Setter
    @Getter
    private Branch targetBranch;

    private StringProperty transDate = new SimpleStringProperty();
    private StringProperty transactionType = new SimpleStringProperty();
    private LongProperty totalPrice = new SimpleLongProperty();

    @Getter
    @Setter
    private List<TransactionDetail> transactionDetails = new ArrayList<>();

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getTransDate() {
        return transDate.get();
    }

    public void setTransDate(String transDate) {
        this.transDate.set(transDate);
    }

    public String getTransactionType() {
        return transactionType.get();
    }

    public void setTransactionType(String transactionType) {
        this.transactionType.set(transactionType);
    }

    public long getTotalPrice() {
        return totalPrice.get();
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice.set(totalPrice);
    }
}
