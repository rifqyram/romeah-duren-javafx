package ac.unindra.roemah_duren_spring.controller;

import ac.unindra.roemah_duren_spring.JavaFxApplication;
import ac.unindra.roemah_duren_spring.constant.ConstantPage;
import ac.unindra.roemah_duren_spring.dto.request.QueryRequest;
import ac.unindra.roemah_duren_spring.model.Admin;
import ac.unindra.roemah_duren_spring.model.Branch;
import ac.unindra.roemah_duren_spring.model.Customer;
import ac.unindra.roemah_duren_spring.model.Transaction;
import ac.unindra.roemah_duren_spring.service.TransactionService;
import ac.unindra.roemah_duren_spring.util.CurrencyUtil;
import ac.unindra.roemah_duren_spring.util.FXMLUtil;
import ac.unindra.roemah_duren_spring.util.NotificationUtil;
import ac.unindra.roemah_duren_spring.util.TableUtil;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionController implements Initializable {

    public AnchorPane main;
    public Button buttonModalAdd;
    public TextField searchField;
    public Button searchBtn;
    public Pagination pagination;
    public TableView<Transaction> tableTransaction;
    public TableColumn<Transaction, Integer> noCol;
    public TableColumn<Transaction, Admin> adminCol;
    public TableColumn<Transaction, Branch> branchCol;
    public TableColumn<Transaction, String> transDateCol;
    public TableColumn<Transaction, String> transTypeCol;
    public TableColumn<Transaction, Long> totalPriceCol;
    public TableColumn<Transaction, Void> actionsCol;

    private final TransactionService transactionService;
    public Button printModalBtn;

    public TransactionController() {
        this.transactionService = JavaFxApplication.getBean(TransactionService.class);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupButtonIcons();
        initTables();
        handlePagination();
        handleSearch();
    }

    private void initTables() {
        TableUtil.setColumnResizePolicy(tableTransaction);
        TableUtil.setTableSequence(noCol);

        adminCol.setCellValueFactory(new PropertyValueFactory<>("admin"));
        adminCol.setCellFactory(col -> TableUtil.setTableObject(Admin::getEmail));

        branchCol.setCellValueFactory(new PropertyValueFactory<>("branch"));
        branchCol.setCellFactory(col -> TableUtil.setTableObject(Branch::getName));

        transDateCol.setCellValueFactory(new PropertyValueFactory<>("transDate"));
        transTypeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalPriceCol.setCellFactory(col -> TableUtil.setTableObject(CurrencyUtil::formatCurrencyIDR));

        actionsCol.setCellFactory(col -> TableUtil.createDetailButtonCell((table, index) ->
                        FXMLUtil.openModal(
                                main,
                                ConstantPage.TRANSACTION_DETAIL,
                                "Detail Transaksi",
                                false,
                                (TransactionDetailController controller) -> controller.setSelectedTransaction(table.getItems().get(index))
                        )
                )
        );
    }

    public void openModalAdd() {
        FXMLUtil.openModal(main, ConstantPage.TRANSACTION_FORM, "Tambah Transaksi", false, (TransactionFormController controller) -> {
            controller.setOwnerPane(main);
            controller.setOnSubmitForm(this::doSearch);
        });
    }

    private void handleSearch() {
        searchField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                doSearch();
            }
        });
    }

    private void handlePagination() {
        pagination.setPageFactory(number -> {
            transactionService.getTransactions(
                    QueryRequest.builder()
                            .page(number)
                            .size(10)
                            .query(searchField.getText())
                            .build(),
                    response -> FXMLUtil.updateUI(() -> {
                        tableTransaction.getItems().setAll(response.getData());
                        pagination.setPageCount(response.getPaging().getTotalPages());
                    }),
                    error -> FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, error.getMessage())));
            return new StackPane();
        });
    }

    public void doSearch() {
        transactionService.getTransactions(
                QueryRequest.builder()
                        .page(0)
                        .size(10)
                        .query(searchField.getText())
                        .build(),
                response -> FXMLUtil.updateUI(() -> {
                    tableTransaction.getItems().setAll(response.getData());
                    pagination.setPageCount(response.getPaging().getTotalPages());
                }),
                error -> FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, error.getMessage())));
    }

    private void setupButtonIcons() {
        buttonModalAdd.setGraphic(new FontIcon(Material2OutlinedAL.ADD));
        printModalBtn.setGraphic(new FontIcon(Material2OutlinedMZ.PRINT));
    }

    public void openModalPrint() {
        FXMLUtil.openModal(main, ConstantPage.TRANSACTION_REPORT, "Laporan Transaksi", false, (ReportController controller) -> controller.setOwnerPane(main));
    }
}
