package ac.unindra.roemah_duren_spring.controller;

import ac.unindra.roemah_duren_spring.JavaFxApplication;
import ac.unindra.roemah_duren_spring.model.Stock;
import ac.unindra.roemah_duren_spring.model.Transaction;
import ac.unindra.roemah_duren_spring.model.TransactionDetail;
import ac.unindra.roemah_duren_spring.service.JasperService;
import ac.unindra.roemah_duren_spring.util.*;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class TransactionDetailController implements Initializable {
    public TableView<TransactionDetail> tableDetail;
    public TableColumn<TransactionDetail, Integer> noCol;
    public TableColumn<TransactionDetail, Stock> productCol;
    public TableColumn<TransactionDetail, Integer> qtyCol;
    public TableColumn<TransactionDetail, Stock> supplierCol;
    public TableColumn<TransactionDetail, Long> priceCol;
    public TableColumn<TransactionDetail, Long> subTotalCol;

    public Button closeBtn;
    public AnchorPane main;
    public Button printBtn;
    public Label branchLabelKey;
    public Label targetBranchOrCustomerLabelKey;
    public Label transTypeLabelKey;
    public Label transDateLabelKey;
    public Label branchLabelValue;
    public Label targetBranchOrCustomerLabelValue;
    public Label transTypeLabelValue;
    public Label transDateLabelValue;
    public Label transactionIdLabelKey;
    public Label transactionIdLabelValue;

    private Transaction selectedTransaction;

    private final JasperService jasperService;

    public TransactionDetailController() {
        this.jasperService = JavaFxApplication.getBean(JasperService.class);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupButtonIcons();
        if (selectedTransaction != null) {
            initLabels();
            initTables();
        }
    }

    public void setSelectedTransaction(Transaction selectedTransaction) {
        this.selectedTransaction = selectedTransaction;
        if (selectedTransaction != null) {
            initLabels();
            initTables();
        }
    }

    private void initLabels() {
        transactionIdLabelValue.setText(selectedTransaction.getId().split("-")[0]);
        branchLabelValue.setText(selectedTransaction.getBranch().getName());

        if (selectedTransaction.getTransactionType().equalsIgnoreCase("Transfer")) {
            targetBranchOrCustomerLabelKey.setText("Cabang Tujuan");
            targetBranchOrCustomerLabelValue.setText(selectedTransaction.getTargetBranch().getName());
        } else if (selectedTransaction.getTransactionType().equalsIgnoreCase("Pembelian")) {
            targetBranchOrCustomerLabelKey.setVisible(false);
            targetBranchOrCustomerLabelValue.setVisible(false);
        } else {
            targetBranchOrCustomerLabelKey.setText("Pelanggan");
            targetBranchOrCustomerLabelValue.setText(selectedTransaction.getCustomer() != null ? selectedTransaction.getCustomer().getName() : "Guest");
        }

        transTypeLabelValue.setText(selectedTransaction.getTransactionType());
        transDateLabelValue.setText(DateUtil.strDateTimeFromString(selectedTransaction.getTransDate()));
    }

    public void initTables() {
        TableUtil.setColumnResizePolicy(tableDetail);
        TableUtil.setTableSequence(noCol);

        productCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        productCol.setCellFactory(col -> TableUtil.setTableObject(stock -> stock.getProduct().getName()));

        qtyCol.setCellValueFactory(new PropertyValueFactory<>("qty"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(col -> TableUtil.setTableObject(CurrencyUtil::formatCurrencyIDR));

        if (selectedTransaction.getTransactionType().equalsIgnoreCase("Pembelian")) {
            supplierCol.setVisible(true);
            supplierCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
            supplierCol.setCellFactory(col -> TableUtil.setTableObject(stock -> stock.getProduct().getSupplier().getName()));
        }

        subTotalCol.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        subTotalCol.setCellFactory(col -> TableUtil.setTableObject(CurrencyUtil::formatCurrencyIDR));

        if (selectedTransaction != null) {
            tableDetail.getItems().setAll(selectedTransaction.getTransactionDetails());
            tableDetail.refresh();
        }
    }

    public void handleClose() {
        Stage stage = (Stage) main.getScene().getWindow();
        stage.close();
    }

    private void setupButtonIcons() {
        closeBtn.setGraphic(new FontIcon(Material2OutlinedAL.CLOSE));
        printBtn.setGraphic(new FontIcon(Material2OutlinedMZ.PRINT));
    }

    public void doPrint() {
        if (selectedTransaction == null) {
            FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, "Transaksi Tidak Ditemukan"));
        }

        switch (selectedTransaction.getTransactionType()) {
            case "Penjualan" -> printInvoiceSales();
            case "Pembelian" -> printInvoiceInbound();
            case "Transfer" -> printTransfer();
            default -> FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, "Tidak Dapat Mencetak Transaksi"));
        }
    }

    private void printTransfer() {
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (TransactionDetail transactionDetail : selectedTransaction.getTransactionDetails()) {
            Map<String, Object> map = new HashMap<>();
            map.put("BARANG", transactionDetail.getStock().getProduct().getName());
            map.put("KUANTITAS", String.valueOf(transactionDetail.getQty()));
            dataList.add(map);
        }

        String from = selectedTransaction.getBranch().getName();
        String to = selectedTransaction.getTargetBranch().getName();
        Map<String, Object> map = new HashMap<>();
        map.put("DARI", from);
        map.put("KIRIM_KE", to);
        map.put("TANGGAL_TTD", DateUtil.strDayDateFromLocalDateTime(LocalDateTime.now()));
        map.put("KODE_TRANSAKSI", selectedTransaction.getId().split("-")[0]);
        map.put("TIPE_TRANSAKSI", selectedTransaction.getTransactionType());

        String date = DateUtil.strDateTimeFromString(selectedTransaction.getTransDate());
        map.put("TANGGAL", date);

        jasperService.createReport(main, "detail_transfer", dataList, map);
    }

    private void printInvoiceInbound() {
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (TransactionDetail transactionDetail : selectedTransaction.getTransactionDetails()) {
            Map<String, Object> map = new HashMap<>();
            map.put("BARANG", transactionDetail.getStock().getProduct().getName());
            map.put("KUANTITAS", String.valueOf(transactionDetail.getQty()));
            map.put("SUPPLIER", transactionDetail.getStock().getProduct().getSupplier().getName());
            map.put("HARGA", CurrencyUtil.formatCurrencyIDR(transactionDetail.getPrice()));
            map.put("TOTAL", CurrencyUtil.formatCurrencyIDR(transactionDetail.getPrice() * transactionDetail.getQty()));
            dataList.add(map);
        }

        String to = selectedTransaction.getBranch().getName();
        Map<String, Object> map = new HashMap<>();
        map.put("TANGGAL_TTD", DateUtil.strDayDateFromLocalDateTime(LocalDateTime.now()));
        map.put("KIRIM_KE", to);
        map.put("KODE_TRANSAKSI", selectedTransaction.getId().split("-")[0]);
        map.put("TIPE_TRANSAKSI", selectedTransaction.getTransactionType());

        String date = DateUtil.strDateTimeFromString(selectedTransaction.getTransDate());
        long totalPrice = selectedTransaction.getTransactionDetails().stream().mapToLong(detail -> detail.getPrice() * detail.getQty()).sum();

        map.put("TANGGAL", date);
        map.put("TOTAL_PRICE", CurrencyUtil.formatCurrencyIDR(totalPrice));

        jasperService.createReport(main, "invoice_inbound", dataList, map);
    }

    private void printInvoiceSales() {
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (TransactionDetail transactionDetail : selectedTransaction.getTransactionDetails()) {
            Map<String, Object> map = new HashMap<>();
            map.put("BARANG", transactionDetail.getStock().getProduct().getName());
            map.put("KUANTITAS", String.valueOf(transactionDetail.getQty()));
            map.put("HARGA", CurrencyUtil.formatCurrencyIDR(transactionDetail.getPrice()));
            map.put("TOTAL", CurrencyUtil.formatCurrencyIDR(transactionDetail.getPrice() * transactionDetail.getQty()));
            dataList.add(map);
        }

        String to = selectedTransaction.getCustomer() != null ? selectedTransaction.getCustomer().getName() : "Guest";
        Map<String, Object> map = new HashMap<>();
        map.put("TANGGAL_TTD", DateUtil.strDayDateFromLocalDateTime(LocalDateTime.now()));
        map.put("KIRIM_KE", to);
        map.put("KODE_TRANSAKSI", selectedTransaction.getId().split("-")[0]);
        map.put("TIPE_TRANSAKSI", selectedTransaction.getTransactionType());

        String date = DateUtil.strDateTimeFromString(selectedTransaction.getTransDate());
        long totalPrice = selectedTransaction.getTransactionDetails().stream().mapToLong(detail -> detail.getPrice() * detail.getQty()).sum();

        map.put("TANGGAL", date);
        map.put("TOTAL_PRICE", CurrencyUtil.formatCurrencyIDR(totalPrice));

        jasperService.createReport(main, "invoice", dataList, map);
    }
}
