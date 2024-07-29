package ac.unindra.roemah_duren_spring.controller;

import ac.unindra.roemah_duren_spring.JavaFxApplication;
import ac.unindra.roemah_duren_spring.constant.ConstantPage;
import ac.unindra.roemah_duren_spring.dto.request.QueryRequest;
import ac.unindra.roemah_duren_spring.model.Admin;
import ac.unindra.roemah_duren_spring.service.AdminService;
import ac.unindra.roemah_duren_spring.util.AlertUtil;
import ac.unindra.roemah_duren_spring.util.FXMLUtil;
import ac.unindra.roemah_duren_spring.util.NotificationUtil;
import ac.unindra.roemah_duren_spring.util.TableUtil;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements Initializable {
    public AnchorPane main;
    public Button buttonModalAdd;
    public TextField searchField;
    public Button searchBtn;
    public TableView<Admin> tableAdmin;
    public TableColumn<Admin, Integer> noCol;
    public TableColumn<Admin, String> nipCol;
    public TableColumn<Admin, String> nameCol;
    public TableColumn<Admin, String> emailCol;
    public TableColumn<Admin, String> mobilePhoneNoCol;
    public TableColumn<Admin, Boolean> statusCol;
    public TableColumn<Admin, Void> actionCol;
    public Pagination pagination;

    private final AdminService adminService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupButtonIcons();
        handlePagination();
        initTables();
        handleSearching();
    }

    private void handleSearching() {
        searchField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                doSearch();
            }
        });
    }

    private void handlePagination() {
        pagination.setPageFactory(pageIndex -> {
            adminService.getAdmins(QueryRequest.builder()
                            .page(pageIndex)
                            .size(10)
                            .query("")
                            .build(),
                    response -> FXMLUtil.updateUI(() -> {
                        tableAdmin.getItems().setAll(response.getData());
                        pagination.setPageCount(response.getPaging().getTotalPages());
                    }),
                    error -> handleErrorResponse(error.getMessage())
            );
            return new AnchorPane();
        });
    }

    private void handleErrorResponse(String message) {
        FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, message));
    }

    public AdminController() {
        this.adminService = JavaFxApplication.getBean(AdminService.class);
    }

    private void setupButtonIcons() {
        buttonModalAdd.setGraphic(new FontIcon(Material2MZ.PLUS));
    }

    private void initTables() {
        TableUtil.setColumnResizePolicy(tableAdmin);
        TableUtil.setTableSequence(noCol);

        nipCol.setCellValueFactory(new PropertyValueFactory<>("nip"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        mobilePhoneNoCol.setCellValueFactory(new PropertyValueFactory<>("mobilePhoneNo"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(col -> TableUtil.setTableObject(aBoolean -> aBoolean ? "Aktif" : "Tidak Aktif"));
        actionCol.setCellFactory(col -> TableUtil.setTableActions(processUpdate(), processDelete()));
    }

    public void openModalAdd() {
        FXMLUtil.openModal(main, ConstantPage.ADMIN_FORM, "Tambah Admin", true, (AdminFormController controller) -> {
            controller.setOwnerPane(main);
            controller.setOnSubmitForm(this::doSearch);
        });
    }

    public void doSearch() {
        adminService.getAdmins(QueryRequest.builder()
                        .page(0)
                        .size(10)
                        .query(searchField.getText())
                        .build(),
                response -> FXMLUtil.updateUI(() -> {
                    tableAdmin.getItems().clear();
                    tableAdmin.getItems().addAll(response.getData());
                    pagination.setPageCount(response.getPaging().getTotalPages());
                    pagination.setCurrentPageIndex(0);
                }),
                error -> handleErrorResponse(error.getMessage())
        );
    }

    private TableUtil.TableAction<TableView<Admin>, Integer> processDelete() {
        return (table, index) -> {
            Admin admin = table.getItems().get(index);
            AlertUtil.confirmDelete(() -> {
                adminService.deleteAdmin(
                        admin.getId(),
                        response -> FXMLUtil.updateUI(() -> {
                            NotificationUtil.showNotificationSuccess(main, "Berhasil menghapus data admin");
                            doSearch();
                        }),
                        error -> handleErrorResponse(error.getMessage())
                );
            });
        };
    }

    private TableUtil.TableAction<TableView<Admin>, Integer> processUpdate() {
        return (table, index) -> FXMLUtil.openModal(main, ConstantPage.ADMIN_FORM, "Ubah Admin", true, (AdminFormController controller) -> {
            controller.updateForm(table.getItems().get(index));
            controller.setOwnerPane(main);
            controller.setOnSubmitForm(this::doSearch);
        });
    }
}
