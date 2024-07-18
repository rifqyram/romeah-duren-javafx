package ac.unindra.roemah_duren_spring.controller;

import ac.unindra.roemah_duren_spring.JavaFxApplication;
import ac.unindra.roemah_duren_spring.dto.request.ReportQueryRequest;
import ac.unindra.roemah_duren_spring.model.Branch;
import ac.unindra.roemah_duren_spring.model.Transaction;
import ac.unindra.roemah_duren_spring.model.TransactionDetail;
import ac.unindra.roemah_duren_spring.service.BranchService;
import ac.unindra.roemah_duren_spring.service.JasperService;
import ac.unindra.roemah_duren_spring.service.TransactionService;
import ac.unindra.roemah_duren_spring.util.*;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.Setter;
import net.sf.jasperreports.engine.JasperReport;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;

public class ReportController implements Initializable {
    public AnchorPane main;

    public ComboBox<String> reportTypeComboBox;

    public DatePicker startDatePicker;
    public DatePicker endDatePicker;
    public ComboBox<Branch> branchComboBox;
    public Button printReportButton;
    public Button cancelBtn;
    public Label labelErrorType;
    public Label labelErrorStartDate;
    public Label errorLabelEndDate;
    public Label errorLabelBranch;
    public VBox branchPane;

    @Setter
    private AnchorPane ownerPane;

    private final BranchService branchService;
    private final TransactionService transactionService;
    private final JasperService jasperService;

    public ReportController() {
        this.jasperService = JavaFxApplication.getBean(JasperService.class);
        this.branchService = JavaFxApplication.getBean(BranchService.class);
        this.transactionService = JavaFxApplication.getBean(TransactionService.class);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupButton();
        initComboBoxBranch();
        initTransTypeComboBox();

        branchComboBox.setButtonCell(ComboBoxUtil.getComboBoxListCell(branch -> {
            if (branch.getCode() == null) return "-- Pilih Cabang --";
            return branch.getCode() + " | " + branch.getName();
        }));
        branchComboBox.setCellFactory(param -> ComboBoxUtil.getComboBoxListCell(branch -> {
            if (branch.getCode() == null) return "-- Pilih Cabang --";
            return branch.getCode() + " | " + branch.getName();
        }));
    }

    private void initComboBoxBranch() {
        branchService.getAllBranch(
                response -> {
                    Branch branch = new Branch();
                    branch.setName("-- Pilih Cabang --");
                    branchComboBox.getItems().clear();
                    branchComboBox.getItems().add(branch);
                    branchComboBox.getItems().addAll(response.getData());
                    branchComboBox.getSelectionModel().selectFirst();
                },
                error -> handleResponseError(error.getMessage())
        );
    }

    private void handleResponseError(String message) {
        FXMLUtil.updateUI(() -> {
            NotificationUtil.showNotificationError(ownerPane, message);
            setPrintButtonDisableLoading();
        });
    }

    public void doPrint() {
        if (!ValidationUtil.isFormValidComboBox(getValidationComboBox())) return;

        getTransactions(transactions -> {
            if (transactions.isEmpty()) {
                NotificationUtil.showNotificationError(ownerPane, "Tidak ada data transaksi");
                return;
            }

            switch (reportTypeComboBox.getValue()) {
                case "Pembelian" -> doPrintTransactionPurchase(transactions);
                case "Penjualan" -> doPrintTransactionSales(transactions);
                case "Transfer" -> doPrintTransactionTransfer(transactions);
            }
        });
    }

    private void doPrintTransactionTransfer(List<Transaction> transactions) {
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (Transaction transaction : transactions) {
            Map<String, Object> data = new HashMap<>();
            data.put("kodeTransaksi", "#" + transaction.getId().split("-")[0]);
            data.put("branch", transaction.getBranch().getName());
            data.put("targetBranchName", transaction.getTargetBranch().getName());

            int no = 0;
            List<Map<String, Object>> detailList = new ArrayList<>();
            for (TransactionDetail transactionDetail : transaction.getTransactionDetails()) {
                Map<String, Object> dataDetail = new HashMap<>();
                dataDetail.put("no", String.valueOf(++no));
                dataDetail.put("product", transactionDetail.getStock().getProduct().getName());
                dataDetail.put("qty", String.valueOf(transactionDetail.getQty()));
                detailList.add(dataDetail);
            }
            data.put("details", detailList);
            dataList.add(data);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("rangeTanggal", DateUtil.strDateFromLocalDateTime(startDatePicker.getValue().atTime(LocalTime.MIN)) + " - " + DateUtil.strDateFromLocalDateTime(endDatePicker.getValue().atTime(LocalTime.MAX)));
        params.put("tanggal", DateUtil.strDayDateFromLocalDateTime(LocalDateTime.now()));
        params.put("tanggalWaktu", DateUtil.strDateTimeFromLocalDateTime(LocalDateTime.now()));

        JasperReport subSales = jasperService.loadReport("sub_transfer");
        params.put("subReport", subSales);

        jasperService.createReport(ownerPane, "transfer", dataList, params);
        setPrintButtonDisableLoading();
    }

    private void doPrintTransactionSales(List<Transaction> transactions) {
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (Transaction transaction : transactions) {
            Map<String, Object> data = new HashMap<>();
            long sum = transaction.getTransactionDetails().stream().mapToLong(transactionDetail -> transactionDetail.getQty() * transactionDetail.getPrice()).sum();
            data.put("kodeTransaksi", "#" + transaction.getId().split("-")[0]);
            data.put("branch", transaction.getBranch().getName());
            data.put("customerName", transaction.getCustomer() != null ? transaction.getCustomer().getName() : "Guest");
            data.put("totalPerBranch", CurrencyUtil.formatCurrencyIDR(sum));

            int no = 0;
            List<Map<String, Object>> detailList = new ArrayList<>();
            for (TransactionDetail transactionDetail : transaction.getTransactionDetails()) {
                Map<String, Object> dataDetail = new HashMap<>();
                dataDetail.put("no", String.valueOf(++no));
                dataDetail.put("product", transactionDetail.getStock().getProduct().getName());
                dataDetail.put("qty", String.valueOf(transactionDetail.getQty()));
                dataDetail.put("price", CurrencyUtil.formatCurrencyIDR(transactionDetail.getPrice()));
                dataDetail.put("subTotal", CurrencyUtil.formatCurrencyIDR(transactionDetail.getQty() * transactionDetail.getPrice()));
                detailList.add(dataDetail);
            }
            data.put("details", detailList);
            dataList.add(data);
        }

        Map<String, Object> params = new HashMap<>();
        long sum = transactions.stream().mapToLong(transaction -> transaction.getTransactionDetails().stream().mapToLong(transactionDetail -> transactionDetail.getQty() * transactionDetail.getPrice()).sum()).sum();
        params.put("rangeTanggal", DateUtil.strDateFromLocalDateTime(startDatePicker.getValue().atTime(LocalTime.MIN)) + " - " + DateUtil.strDateFromLocalDateTime(endDatePicker.getValue().atTime(LocalTime.MAX)));
        params.put("tanggal", DateUtil.strDayDateFromLocalDateTime(LocalDateTime.now()));
        params.put("tanggalWaktu", DateUtil.strDateTimeFromLocalDateTime(LocalDateTime.now()));
        params.put("grandTotal", CurrencyUtil.formatCurrencyIDR(sum));

        JasperReport subSales = jasperService.loadReport("sub_sales");
        params.put("subReport", subSales);

        jasperService.createReport(ownerPane, "sales", dataList, params);
        setPrintButtonDisableLoading();
    }

    private void doPrintTransactionPurchase(List<Transaction> transactions) {
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (Transaction transaction : transactions) {
            Map<String, Object> data = new HashMap<>();
            long sum = transaction.getTransactionDetails().stream().mapToLong(transactionDetail -> transactionDetail.getQty() * transactionDetail.getPrice()).sum();
            data.put("kodeTransaksi", "#" + transaction.getId().split("-")[0]);
            data.put("branch", transaction.getBranch().getName());
            data.put("totalPerBranch", CurrencyUtil.formatCurrencyIDR(sum));

            int no = 0;
            List<Map<String, Object>> detailList = new ArrayList<>();
            for (TransactionDetail transactionDetail : transaction.getTransactionDetails()) {
                Map<String, Object> dataDetail = new HashMap<>();
                dataDetail.put("no", String.valueOf(++no));
                dataDetail.put("product", transactionDetail.getStock().getProduct().getName());
                dataDetail.put("qty", String.valueOf(transactionDetail.getQty()));
                dataDetail.put("supplier", transactionDetail.getStock().getProduct().getSupplier().getName());
                dataDetail.put("price", CurrencyUtil.formatCurrencyIDR(transactionDetail.getPrice()));
                dataDetail.put("subTotal", CurrencyUtil.formatCurrencyIDR(transactionDetail.getQty() * transactionDetail.getPrice()));
                detailList.add(dataDetail);
            }
            data.put("details", detailList);
            dataList.add(data);
        }

        Map<String, Object> params = new HashMap<>();
        long sum = transactions.stream().mapToLong(transaction -> transaction.getTransactionDetails().stream().mapToLong(transactionDetail -> transactionDetail.getQty() * transactionDetail.getPrice()).sum()).sum();
        params.put("rangeTanggal", DateUtil.strDateFromLocalDateTime(startDatePicker.getValue().atTime(LocalTime.MIN)) + " - " + DateUtil.strDateFromLocalDateTime(endDatePicker.getValue().atTime(LocalTime.MAX)));
        params.put("tanggal", DateUtil.strDayDateFromLocalDateTime(LocalDateTime.now()));
        params.put("tanggalWaktu", DateUtil.strDateTimeFromLocalDateTime(LocalDateTime.now()));
        params.put("grandTotal", CurrencyUtil.formatCurrencyIDR(sum));

        JasperReport subSales = jasperService.loadReport("purchasing_detail");
        params.put("subReport", subSales);

        jasperService.createReport(ownerPane, "purchasing", dataList, params);
        setPrintButtonDisableLoading();
    }

    private void getTransactions(Consumer<List<Transaction>> consumer) {
        setPrintButtonEnableLoading();
        String reportType = reportTypeComboBox.getValue();

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        LocalDateTime startDateTime = startDate.atTime(LocalTime.MIN);
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        Branch branch = branchComboBox.getValue();

        transactionService.getTransactions(
                ReportQueryRequest.builder()
                        .branchId(branch.getId() != null ? branch.getId() : null)
                        .startDate(startDateTime)
                        .endDate(endDateTime)
                        .transactionType(reportType)
                        .build(),
                response -> FXMLUtil.updateUI(() -> consumer.accept(response.getData())),
                error -> handleResponseError(error.getMessage())
        );
    }

    private void setupButton() {
        printReportButton.setGraphic(new FontIcon(Material2OutlinedMZ.PRINT));
        cancelBtn.setGraphic(new FontIcon(Material2OutlinedAL.CANCEL));
    }

    public void closeModal() {
        Stage stage = (Stage) main.getScene().getWindow();
        stage.close();
    }

    private void initTransTypeComboBox() {
        reportTypeComboBox.getItems().setAll(List.of("Pembelian", "Penjualan", "Transfer"));
    }

    public Map<ComboBoxBase<?>, Pair<Label, ValidationUtil.ValidationStrategyComboBox>> getValidationComboBox() {
        return Map.of(
                reportTypeComboBox, new Pair<>(labelErrorType, input -> input.getValue() == null ? "Pilih salah satu jenis laporan" : ""),
                startDatePicker, new Pair<>(labelErrorStartDate, input -> input.getValue() == null ? "Pilih tanggal awal" : ""),
                endDatePicker, new Pair<>(errorLabelEndDate, input -> input.getValue() == null ? "Pilih tanggal akhir" : "")
        );
    }

    private void setPrintButtonDisableLoading() {
        FXMLUtil.updateUI(() -> {
            printReportButton.setText("Print");
            printReportButton.setDisable(false);
        });
    }

    private void setPrintButtonEnableLoading() {
        FXMLUtil.updateUI(() -> {
            printReportButton.setText("Loading...");
            printReportButton.setDisable(true);
        });
    }
}
