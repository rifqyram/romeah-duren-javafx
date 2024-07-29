package ac.unindra.roemah_duren_spring.controller;

import ac.unindra.roemah_duren_spring.JavaFxApplication;
import ac.unindra.roemah_duren_spring.constant.ConstantPage;
import ac.unindra.roemah_duren_spring.constant.ResponseMessage;
import ac.unindra.roemah_duren_spring.dto.request.QueryRequest;
import ac.unindra.roemah_duren_spring.model.Branch;
import ac.unindra.roemah_duren_spring.model.Product;
import ac.unindra.roemah_duren_spring.model.Stock;
import ac.unindra.roemah_duren_spring.model.Supplier;
import ac.unindra.roemah_duren_spring.service.JasperService;
import ac.unindra.roemah_duren_spring.service.ProductService;
import ac.unindra.roemah_duren_spring.service.StockService;
import ac.unindra.roemah_duren_spring.service.UserService;
import ac.unindra.roemah_duren_spring.util.TableUtil;
import ac.unindra.roemah_duren_spring.util.*;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ProductController implements Initializable {
    public AnchorPane main;
    public TextField searchField;
    public Button searchBtn;
    public TableView<Product> tableProduct;
    public TableColumn<Product, Integer> noCol;
    public TableColumn<Product, String> codeCol;
    public TableColumn<Product, String> nameCol;
    public TableColumn<Product, Long> priceCol;
    public TableColumn<Product, String> descriptionCol;
    public TableColumn<Product, Supplier> supplierCol;
    public TableColumn<Product, Void> actionsCol;
    public Pagination pagination;
    public Button btnOpenModal;
    public Button printReport;

    private final ProductService productService;
    private final StockService stockService;
    private final JasperService jasperService;
    private final UserService userService;

    public ProductController() {
        this.productService = JavaFxApplication.getBean(ProductService.class);
        this.stockService = JavaFxApplication.getBean(StockService.class);
        this.jasperService = JavaFxApplication.getBean(JasperService.class);
        this.userService = JavaFxApplication.getBean(UserService.class);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService.getUserInfoByToken(
                response -> {
                    if (response.getRoles().contains("Owner")) {
                        btnOpenModal.setDisable(true);
                        tableProduct.setDisable(true);
                    }
                },
                error -> handleErrorResponse(error.getMessage())
        );

        setupButtonIcons();
        initTableData();
        handleSearch();
        handlePagination();
    }

    private void handlePagination() {
        pagination.setPageFactory(pageIndex -> {
            productService.getProducts(QueryRequest.builder()
                            .page(pageIndex)
                            .size(10)
                            .query("")
                            .build(),
                    response -> FXMLUtil.updateUI(() -> {
                        tableProduct.getItems().setAll(response.getData());
                        pagination.setPageCount(response.getPaging().getTotalPages());
                    }),
                    error -> handleErrorResponse(error.getMessage())
            );
            return new AnchorPane();
        });
    }

    private void initTableData() {
        TableUtil.setColumnResizePolicy(tableProduct);
        TableUtil.setTableSequence(noCol);
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(col -> TableUtil.formatCurrencyIDR());
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        supplierCol.setCellFactory(col -> TableUtil.setTableObject(Supplier::getName));
        actionsCol.setCellFactory(col -> TableUtil.setTableActions(
                (table, index) -> FXMLUtil.openModal(main, ConstantPage.PRODUCT_FORM, "Ubah Barang", true, (ProductFormController controller) -> {
                    controller.updateForm(table.getItems().get(index));
                    controller.setOwnerPane(main);
                    controller.setOnSubmit(this::doSearch);
                }),
                (table, index) -> AlertUtil.confirmDelete(() -> {
                    Product product = table.getItems().get(index);
                    productService.deleteProduct(
                            product.getId(),
                            response -> handleSuccessResponse(),
                            error -> handleErrorResponse(error.getMessage())
                    );
                })
        ));
    }

    public void doSearch() {
        productService.getProducts(QueryRequest.builder()
                        .page(0)
                        .size(10)
                        .query(searchField.getText())
                        .build(),
                response -> {
                    FXMLUtil.updateUI(() -> {
                        tableProduct.getItems().clear();
                        tableProduct.getItems().addAll(response.getData());
                        pagination.setPageCount(response.getPaging().getTotalPages());
                        pagination.setCurrentPageIndex(0);
                    });
                },
                error -> handleErrorResponse(error.getMessage())
        );
    }

    private void setupButtonIcons() {
        btnOpenModal.setGraphic(new FontIcon(Material2AL.ADD));
        printReport.setGraphic(new FontIcon(Material2MZ.PRINT));
    }

    public void openModal() {
        FXMLUtil.openModal(main, ConstantPage.PRODUCT_FORM, "Tambah Barang", true, (ProductFormController controller) -> {
            controller.setOnSubmit(this::doSearch);
            controller.setOwnerPane(main);
        });
    }

    public void handleSearch() {
        searchField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                doSearch();
            }
        });
    }

    private void handleSuccessResponse() {
        NotificationUtil.showNotificationSuccess(main, ResponseMessage.SUCCESS_DELETE);
        doSearch();
    }

    private void handleErrorResponse(String error) {
        NotificationUtil.showNotificationError(main, error);
    }

    public void doPrintReport() {
        List<Map<String, Object>> dataList = new ArrayList<>();

        stockService.getAllStocks(
                response -> {
                    if (response.getData().isEmpty()) {
                        FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, "Data Kosong"));
                        return;
                    }

                    Map<Branch, List<Stock>> data = response.getData().stream().collect(Collectors.groupingBy(Stock::getBranch));

                    data.forEach((s, stocks) -> {
                        Map<String, Object> map = new HashMap<>();
                        List<Map<String, Object>> details = new ArrayList<>();

                        int no = 0;
                        for (Stock stock : stocks) {
                            Map<String, Object> detail = new HashMap<>();
                            detail.put("no", String.valueOf(++no));
                            detail.put("code", stock.getProduct().getCode());
                            detail.put("product", stock.getProduct().getName());
                            detail.put("harga", CurrencyUtil.formatCurrencyIDR(stock.getProduct().getPrice()));
                            detail.put("stock", String.valueOf(stock.getStock()));
                            detail.put("supplier", stock.getProduct().getSupplier().getName());
                            details.add(detail);
                        }

                        map.put("branch", s.getName());
                        map.put("details", details);
                        dataList.add(map);
                    });
                },
                error -> handleErrorResponse(error.getMessage())
        );

        Map<String, Object> params = new HashMap<>();

        params.put("tanggal", DateUtil.strDayDateFromLocalDateTime(LocalDateTime.now()));
        params.put("tanggalWaktu", DateUtil.strDateTimeFromLocalDateTime(LocalDateTime.now()));
        params.put("subReport", jasperService.loadReport("sub_product"));

        jasperService.createReport(main, "product", dataList, params);
    }
}
