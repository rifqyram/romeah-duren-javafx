package ac.unindra.roemah_duren_spring.controller;

import ac.unindra.roemah_duren_spring.JavaFxApplication;
import ac.unindra.roemah_duren_spring.constant.ConstantPage;
import ac.unindra.roemah_duren_spring.constant.ResponseMessage;
import ac.unindra.roemah_duren_spring.dto.request.QueryRequest;
import ac.unindra.roemah_duren_spring.model.Supplier;
import ac.unindra.roemah_duren_spring.service.SupplierService;
import ac.unindra.roemah_duren_spring.util.AlertUtil;
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
import org.kordamp.ikonli.material2.Material2AL;

import java.net.URL;
import java.util.ResourceBundle;

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
    private final SupplierService supplierService;

    public SupplierController() {
        this.supplierService = JavaFxApplication.getBean(SupplierService.class);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupButtonIcons();
        initTableData();
        handlePagination();
        handleSearch();
    }

    private void setupButtonIcons() {
        buttonModalAdd.setGraphic(new FontIcon(Material2AL.ADD));
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
        FXMLUtil.openModal(main, ConstantPage.SUPPLIER_FORM, "Tambah Supplier", false, (SupplierFormController controller) -> {
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
            FXMLUtil.openModal(main, ConstantPage.SUPPLIER_FORM, "Tambah Supplier", false, (SupplierFormController controller) -> {
                controller.updateForm(supplier);
                controller.setOnFormSubmit(this::doSearch);
                controller.setOwnerPane(main);
            });
        };
    }
}
