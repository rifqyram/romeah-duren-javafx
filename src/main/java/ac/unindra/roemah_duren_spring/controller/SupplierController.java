package ac.unindra.roemah_duren_spring.controller;

import ac.unindra.roemah_duren_spring.JavaFxApplication;
import ac.unindra.roemah_duren_spring.constant.ConstantPage;
import ac.unindra.roemah_duren_spring.constant.ResponseMessage;
import ac.unindra.roemah_duren_spring.dto.request.QueryRequest;
import ac.unindra.roemah_duren_spring.model.Supplier;
import ac.unindra.roemah_duren_spring.service.JasperService;
import ac.unindra.roemah_duren_spring.service.SupplierService;
import ac.unindra.roemah_duren_spring.service.UserService;
import ac.unindra.roemah_duren_spring.util.TableUtil;
import ac.unindra.roemah_duren_spring.util.*;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class SupplierController implements Initializable {
    public AnchorPane main;
    public TextField searchField;
    public TableView<Supplier> tableSupplier;
    public TableColumn<Supplier, Integer> noCol;
    public TableColumn<Supplier, String> nameCol;
    public TableColumn<Supplier, String> addressCol;
    public TableColumn<Supplier, String> emailCol;
    public TableColumn<Supplier, String> mobilePhoneNoCol;
    public TableColumn<Supplier, Void> actionsCol;
    public Button searchBtn;
    public Pagination pagination;
    public Button buttonModalAdd;
    public Button printReport;

    private final SupplierService supplierService;
    private final JasperService jasperService;
    private final UserService userService;

    public SupplierController() {
        this.supplierService = JavaFxApplication.getBean(SupplierService.class);
        this.jasperService = JavaFxApplication.getBean(JasperService.class);
        this.userService = JavaFxApplication.getBean(UserService.class);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService.getUserInfoByToken(
                response -> {
                    if (response.getRoles().contains("Owner")) {
                        buttonModalAdd.setDisable(true);
                        tableSupplier.setDisable(true);
                    }
                },
                error -> handleErrorResponse(error.getMessage()));

        setupButtonIcons();
        initTableData();
        handlePagination();
        handleSearch();
    }

    private void setupButtonIcons() {
        buttonModalAdd.setGraphic(new FontIcon(Material2AL.ADD));
        printReport.setGraphic(new FontIcon(Material2MZ.PRINT));
    }

    private void initTableData() {
        TableUtil.setColumnResizePolicy(tableSupplier);
        TableUtil.setTableSequence(noCol);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        mobilePhoneNoCol.setCellValueFactory(new PropertyValueFactory<>("mobilePhoneNo"));
        actionsCol.setCellFactory(col -> TableUtil.setTableActions(processUpdate(), processDelete()));

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
            supplierService.getSuppliers(
                    QueryRequest.builder()
                            .page(number)
                            .size(10)
                            .query(searchField.getText())
                            .build(),
                    response -> FXMLUtil.updateUI(() -> {
                        tableSupplier.getItems().setAll(response.getData());
                        pagination.setPageCount(response.getPaging().getTotalPages());
                    }),
                    error -> FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, error.getMessage())));
            return new StackPane();
        });
    }

    public void doSearch() {
        supplierService.getSuppliers(
                QueryRequest.builder()
                        .page(0)
                        .size(10)
                        .query(searchField.getText())
                        .build(),
                response -> FXMLUtil.updateUI(() -> {
                    tableSupplier.getItems().setAll(response.getData());
                    pagination.setPageCount(response.getPaging().getTotalPages());
                }),
                error -> FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, error.getMessage())));
    }

    private void handleSuccessResponse(String message) {
        NotificationUtil.showNotificationSuccess(main, message);
        doSearch();
    }

    private void handleErrorResponse(String error) {
        NotificationUtil.showNotificationError(main, error);
    }

    public void openModalAdd() {
        FXMLUtil.openModal(main, ConstantPage.SUPPLIER_FORM, "Tambah Supplier", true, (SupplierFormController controller) -> {
            controller.setOnFormSubmit(this::doSearch);
            controller.setOwnerPane(main);
        });
    }

    private TableUtil.TableAction<TableView<Supplier>, Integer> processDelete() {
        return (table, index) -> {
            Supplier supplier = table.getItems().get(index);
            AlertUtil.confirmDelete(
                    () -> supplierService.deleteSupplier(supplier.getId(),
                            response -> FXMLUtil.updateUI(() -> handleSuccessResponse(ResponseMessage.SUCCESS_DELETE)),
                            error -> FXMLUtil.updateUI(() -> handleErrorResponse(error.getMessage()))
                    )
            );
        };
    }

    private TableUtil.TableAction<TableView<Supplier>, Integer> processUpdate() {
        return (table, index) -> {
            Supplier supplier = table.getItems().get(index);
            FXMLUtil.openModal(main, ConstantPage.SUPPLIER_FORM, "Tambah Supplier", true, (SupplierFormController controller) -> {
                controller.updateForm(supplier);
                controller.setOnFormSubmit(this::doSearch);
                controller.setOwnerPane(main);
            });
        };
    }

    public void doPrintReport() {
        List<Map<String, Object>> dataList = new ArrayList<>();

        supplierService.getSuppliers(
                response -> {
                    if (response.getData().isEmpty()) {
                        FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, "Data Kosong"));
                        return;
                    }

                    int no = 0;
                    for (Supplier supplier : response.getData()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("no", String.valueOf(++no));
                        map.put("supplierName", supplier.getName());
                        map.put("address", supplier.getAddress());
                        map.put("email", supplier.getEmail());
                        map.put("mobilePhoneNo", supplier.getMobilePhoneNo());
                        dataList.add(map);
                    }
                },
                error -> handleErrorResponse(error.getMessage())
        );

        Map<String, Object> params = new HashMap<>();

        params.put("tanggal", DateUtil.strDayDateFromLocalDateTime(LocalDateTime.now()));
        params.put("tanggalWaktu", DateUtil.strDateTimeFromLocalDateTime(LocalDateTime.now()));

        jasperService.createReport(main, "supplier", dataList, params);
    }
}
