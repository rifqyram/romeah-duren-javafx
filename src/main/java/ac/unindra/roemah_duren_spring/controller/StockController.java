package ac.unindra.roemah_duren_spring.controller;

import ac.unindra.roemah_duren_spring.JavaFxApplication;
import ac.unindra.roemah_duren_spring.constant.ConstantPage;
import ac.unindra.roemah_duren_spring.dto.request.QueryRequest;
import ac.unindra.roemah_duren_spring.model.Branch;
import ac.unindra.roemah_duren_spring.model.Product;
import ac.unindra.roemah_duren_spring.model.Stock;
import ac.unindra.roemah_duren_spring.service.StockService;
import ac.unindra.roemah_duren_spring.service.UserService;
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

public class StockController implements Initializable {
    public AnchorPane main;
    public TextField searchField;
    public Button buttonModalAdd;
    public Button searchBtn;
    public TableView<Stock> tableStock;
    public TableColumn<Stock, Integer> noCol;
    public TableColumn<Stock, Product> productNameCol;
    public TableColumn<Stock, Branch> branchCol;
    public TableColumn<Stock, Integer> stockCol;
    public TableColumn<Stock, String> updatedDateCol;
    public TableColumn<Stock, Void> actionsCol;
    public Pagination pagination;

    private final StockService stockService;
    private final UserService userService;

    public StockController() {
        this.stockService = JavaFxApplication.getBean(StockService.class);
        this.userService = JavaFxApplication.getBean(UserService.class);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService.getUserInfoByToken(
                response -> {
                    if (response.getRoles().contains("Owner")) {
                        buttonModalAdd.setDisable(true);
                        tableStock.setDisable(true);
                    }
                },
                error -> handleErrorResponse(error.getMessage())
        );

        setupButtonIcons();
        initTableData();
        handlePagination();
        handleSearch();
    }

    private void setupButtonIcons() {
        buttonModalAdd.setGraphic(new FontIcon(Material2AL.ADD));
    }

    private void initTableData() {
        TableUtil.setColumnResizePolicy(tableStock);
        TableUtil.setTableSequence(noCol);
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        productNameCol.setCellFactory(col -> TableUtil.setTableObject(Product::getName));

        branchCol.setCellValueFactory(new PropertyValueFactory<>("branch"));
        branchCol.setCellFactory(col -> TableUtil.setTableObject(Branch::getName));

        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        updatedDateCol.setCellValueFactory(new PropertyValueFactory<>("updatedDate"));

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
            stockService.getStocks(
                    QueryRequest.builder()
                            .page(number)
                            .size(10)
                            .query(searchField.getText())
                            .build(),
                    response -> FXMLUtil.updateUI(() -> {
                        tableStock.getItems().setAll(response.getData());
                        pagination.setPageCount(response.getPaging().getTotalPages());
                    }),
                    error -> FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, error.getMessage())));
            return new StackPane();
        });
    }

    public void doSearch() {
        stockService.getStocks(
                QueryRequest.builder()
                        .page(0)
                        .size(10)
                        .query(searchField.getText())
                        .build(),
                response -> {
                    tableStock.getItems().setAll(response.getData());
                },
                error -> handleErrorResponse(error.getMessage())
        );
    }

    public void openModalAdd() {
        FXMLUtil.openModal(main, ConstantPage.STOCK_FORM, "Form Tambah Stock", true, (StockFormController controller) -> {
            controller.setOwnerPane(main);
            controller.setOnFormSubmit(this::doSearch);
        });
    }

    private void handleErrorResponse(String error) {
        NotificationUtil.showNotificationError(main, error);
    }

    private TableUtil.TableAction<TableView<Stock>, Integer> processUpdate() {
        return (table, index) -> {
            Stock stock = table.getItems().get(index);
            FXMLUtil.openModal(main, ConstantPage.STOCK_FORM, "Form Ubah Stock", true, (StockFormController controller) -> {
                controller.updateForm(stock);
                controller.setOwnerPane(main);
                controller.setOnFormSubmit(this::doSearch);
            });
        };
    }

    private TableUtil.TableAction<TableView<Stock>, Integer> processDelete() {
        return (table, index) -> {
            Stock stock = table.getItems().get(index);
            AlertUtil.confirmDelete(() -> stockService.deleteStock(
                    stock.getId(),
                    response -> FXMLUtil.updateUI(this::doSearch),
                    error -> handleErrorResponse(error.getMessage())
            ));

        };
    }
}
