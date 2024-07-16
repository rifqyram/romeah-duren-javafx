package ac.unindra.roemah_duren_spring.controller;

import ac.unindra.roemah_duren_spring.JavaFxApplication;
import ac.unindra.roemah_duren_spring.constant.ConstantPage;
import ac.unindra.roemah_duren_spring.dto.request.QueryRequest;
import ac.unindra.roemah_duren_spring.model.Branch;
import ac.unindra.roemah_duren_spring.model.Supplier;
import ac.unindra.roemah_duren_spring.service.BranchService;
import ac.unindra.roemah_duren_spring.service.JasperService;
import ac.unindra.roemah_duren_spring.service.UserService;
import ac.unindra.roemah_duren_spring.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

public class BranchController implements Initializable {
    private final BranchService branchService;
    private final JasperService jasperService;
    public Button buttonModalAdd;

    public AnchorPane main;
    public TableView<Branch> tableBranch;
    public TableColumn<Branch, Integer> noCol;
    public TableColumn<Branch, String> codeCol;
    public TableColumn<Branch, String> nameCol;
    public TableColumn<Branch, String> addressCol;
    public TableColumn<Branch, String> mobilePhoneNoCol;
    public TableColumn<Branch, Void> actionsCol;
    public Pagination pagination;
    public Button searchBtn;
    public Button printReport;
    public TextField searchField;

    private final UserService userService;

    public BranchController() {
        this.jasperService = JavaFxApplication.getBean(JasperService.class);
        this.branchService = JavaFxApplication.getBean(BranchService.class);
        this.userService = JavaFxApplication.getBean(UserService.class);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService.getUserInfoByToken(
                response -> {
                    if (response.getRoles().contains("Owner")) {
                        buttonModalAdd.setDisable(true);
                        tableBranch.setDisable(true);
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
        printReport.setGraphic(new FontIcon(Material2MZ.PRINT));
    }

    private void initTableData() {
        TableUtil.setColumnResizePolicy(tableBranch);
        TableUtil.setTableSequence(noCol);
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
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
            branchService.getBranches(
                    QueryRequest.builder()
                            .page(number)
                            .size(10)
                            .query(searchField.getText())
                            .build(),
                    response -> FXMLUtil.updateUI(() -> {
                        tableBranch.getItems().setAll(response.getData());
                        pagination.setPageCount(response.getPaging().getTotalPages());
                    }),
                    error -> FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, error.getMessage())));
            return new StackPane();
        });
    }

    private void handleErrorResponse(String error) {
        NotificationUtil.showNotificationError(main, error);
    }

    @FXML
    public void doSearch() {
        branchService.getBranches(
                QueryRequest.builder()
                        .page(0)
                        .size(10)
                        .query(searchField.getText())
                        .build(),
                response -> tableBranch.getItems().setAll(response.getData()),
                error -> NotificationUtil.showNotificationError(main, error.getMessage())
        );
    }

    public void openModalAdd() {
        FXMLUtil.openModal(main, ConstantPage.BRANCH_FORM, "Form Branch", false, (BranchFormController controller) -> {
            controller.setOnFormSubmit(this::doSearch);
            controller.setOwnerPane(main);
        });
    }

    private TableUtil.TableAction<TableView<Branch>, Integer> processUpdate() {
        return (table, index) -> {
            Branch branch = table.getItems().get(index);
            FXMLUtil.openModal(main, ConstantPage.BRANCH_FORM, "Form Branch", false, (BranchFormController controller) -> {
                controller.updateForm(branch);
                controller.setOwnerPane(main);
                controller.setOnFormSubmit(this::doSearch);
            });
        };
    }

    private TableUtil.TableAction<TableView<Branch>, Integer> processDelete() {
        return (table, index) -> {
            Branch branch = table.getItems().get(index);
            AlertUtil.confirmDelete(() -> branchService.deleteBranch(
                    branch.getId(),
                    response -> FXMLUtil.updateUI(this::doSearch),
                    error -> handleErrorResponse(error.getMessage())
            ));

        };
    }

    public void doPrintReport() {
        List<Map<String, Object>> dataList = new ArrayList<>();

        branchService.getAllBranch(
                response -> {
                    if (response.getData().isEmpty()) {
                        FXMLUtil.updateUI(() -> NotificationUtil.showNotificationError(main, "Data Kosong"));
                        return;
                    }

                    int no = 0;
                    for (Branch branch : response.getData()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("no", String.valueOf(++no));
                        map.put("code", branch.getCode());
                        map.put("branchName", branch.getName());
                        map.put("address", branch.getAddress());
                        map.put("mobilePhoneNo", branch.getMobilePhoneNo());
                        dataList.add(map);
                    }
                },
                error -> handleErrorResponse(error.getMessage())
        );

        Map<String, Object> params = new HashMap<>();

        params.put("tanggal", DateUtil.strDayDateFromLocalDateTime(LocalDateTime.now()));
        params.put("tanggalWaktu", DateUtil.strDateTimeFromLocalDateTime(LocalDateTime.now()));

        jasperService.createReport(main, "branch", dataList, params);
    }
}
