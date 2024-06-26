package ac.unindra.roemah_duren_spring.controller;

import ac.unindra.roemah_duren_spring.JavaFxApplication;
import ac.unindra.roemah_duren_spring.constant.ConstantPage;
import ac.unindra.roemah_duren_spring.dto.request.QueryRequest;
import ac.unindra.roemah_duren_spring.model.Customer;
import ac.unindra.roemah_duren_spring.service.CustomerService;
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

public class CustomerController implements Initializable {
    public AnchorPane main;
    public TextField searchField;
    public Button searchBtn;
    public TableView<Customer> tableCustomer;
    public TableColumn<Customer, Integer> noCol;
    public TableColumn<Customer, String> nameCol;
    public TableColumn<Customer, String> addressCol;
    public TableColumn<Customer, String> emailCol;
    public TableColumn<Customer, String> mobilePhoneNoCol;
    public TableColumn<Customer, Void> actionCol;
    public Pagination pagination;
    public Button buttonModalAdd;
    private final CustomerService customerService;

    public CustomerController() {
        this.customerService = JavaFxApplication.getBean(CustomerService.class);
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
        tableCustomer.setSelectionModel(null);
        TableUtil.setColumnResizePolicy(tableCustomer);
        TableUtil.setTableSequence(noCol);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        mobilePhoneNoCol.setCellValueFactory(new PropertyValueFactory<>("mobilePhoneNo"));
        actionCol.setCellFactory(col -> TableUtil.setTableActions(processUpdate(), processDelete()));
    }

    public void openModalAdd() {
        FXMLUtil.openModal(main, ConstantPage.CUSTOMER_FORM, "Form Customer", false, (CustomerFormController controller) -> {
            controller.setOnFormSubmit(this::doSearch);
            controller.setOwnerPane(main);
        });
    }

    private void handlePagination() {
        pagination.setPageFactory(number -> {
            customerService.getCustomers(
                    QueryRequest.builder()
                            .page(number)
                            .size(10)
                            .query(searchField.getText())
                            .build(),
                    response -> FXMLUtil.updateUI(() -> {
                        tableCustomer.getItems().setAll(response.getData());
                        pagination.setPageCount(response.getPaging().getTotalPages());
                    }),
                    error -> FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, error.getMessage())));
            return new StackPane();
        });
    }

    private void handleSearch() {
        searchField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                doSearch();
            }
        });
    }

    public void doSearch() {
        customerService.getCustomers(
                QueryRequest.builder()
                        .page(0)
                        .size(10)
                        .query(searchField.getText())
                        .build(),
                response -> FXMLUtil.updateUI(() -> {
                    tableCustomer.getItems().setAll(response.getData());
                    pagination.setPageCount(response.getPaging().getTotalPages());
                }),
                error -> FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, error.getMessage())));
    }

    private TableUtil.TableAction<TableView<Customer>, Integer> processDelete() {
        return (table, index) -> {
            Customer customer = table.getItems().get(index);
            AlertUtil.confirmDelete(
                    () -> customerService.deleteCustomer(
                            customer.getId(),
                            response -> FXMLUtil.updateUI(this::doSearch),
                            error -> {
                            }
                    )
            );
        };
    }

    private TableUtil.TableAction<TableView<Customer>, Integer> processUpdate() {
        return (table, index) -> {
            Customer customer = table.getItems().get(index);
            FXMLUtil.openModal(main, ConstantPage.CUSTOMER_FORM, "Form Customer", false, (CustomerFormController controller) -> {
                controller.updateForm(customer);
                controller.setOwnerPane(main);
                controller.setOnFormSubmit(this::doSearch);
            });
        };
    }
}
