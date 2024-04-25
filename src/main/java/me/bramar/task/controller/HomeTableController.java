package me.bramar.task.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import lombok.extern.slf4j.Slf4j;
import me.bramar.task.entity.CreditCardInfo;
import me.bramar.task.service.ICreditCardInfoService;
import me.bramar.task.utils.LfgUtils;
import me.bramar.task.utils.NameParser;
import me.bramar.task.utils.PhoneNumberUtils;
import net.datafaker.Faker;
import net.datafaker.providers.base.Address;
import net.datafaker.providers.base.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class HomeTableController implements Initializable {

    @FXML
    private Tab homeTab; // 使用 fx:id 绑定的 Tab

    @FXML
    private TableView<CreditCardInfo> cvvView;

    @FXML
    private TableColumn<CreditCardInfo, Boolean> selectColumn;

    @FXML
    private TableColumn<CreditCardInfo, String> cardNumber;

    @FXML
    private TableColumn<CreditCardInfo, String> month;

    @FXML
    private TableColumn<CreditCardInfo, String> year;
    @FXML
    private TableColumn<CreditCardInfo, String> securityCode;
    @FXML
    private TableColumn<CreditCardInfo, String> fullName;
    @FXML
    private TableColumn<CreditCardInfo, String> address;
    @FXML
    private TableColumn<CreditCardInfo, String> city;
    @FXML
    private TableColumn<CreditCardInfo, String> phoneNumber;
    @FXML
    private TableColumn<CreditCardInfo, String> state;
    @FXML
    private TableColumn<CreditCardInfo, String> zipCode;
    @FXML
    private TableColumn<CreditCardInfo, String> email;
    @FXML
    private TableColumn<CreditCardInfo, String> password;
    @FXML
    private TableColumn<CreditCardInfo, String> isActive;

    @FXML
    private Button selectAllButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button saveButton;
    @FXML
    public Button restoreButton;

    @FXML
    public Button executeButton;

    @Autowired
    private ICreditCardInfoService creditCardInfoService;
    private boolean selectStatus = false;

    //作为备份数据
    private List<CreditCardInfo> originalDataList = new ArrayList<>();

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(true);
    private ExecutorService executorService;

    private boolean isEditMode = false;

    public void parseAndLoadData(String content) {
        if (StrUtil.isNotBlank(content)) {
            String[] lines = content.split(System.lineSeparator());
            ObservableList<CreditCardInfo> dataList = FXCollections.observableArrayList();
            for (String line : lines) {
                Faker faker = new Faker(Locale.US);
                String[] parts = line.split("\\|"); // 使用 分隔数据
                if (parts.length >= 4) {
                    CreditCardInfo creditCardInfo = new CreditCardInfo();
                    creditCardInfo.setCardNumber(parts[0].trim());
                    creditCardInfo.setMonthNum(parts[1].trim());
                    String year = parts[2].trim();
                    if (year.length() == 4) {
                        year = StrUtil.removePrefix(year, "20");
                        creditCardInfo.setYearNum(year);
                    } else if (year.length() == 2) {
                        creditCardInfo.setYearNum(year);
                    }
                    creditCardInfo.setSecurityCode(parts[3].trim());
                    if (parts.length > 5 && StrUtil.isNotBlank(parts[4])) {
                        String fullName = parts[4].trim();
                        creditCardInfo.setFullName(fullName);
                        List<String> name = NameParser.getName(fullName);
                        if (CollectionUtil.isNotEmpty(name)) {
                            creditCardInfo.setFirstName(name.get(0));
                            creditCardInfo.setLastName(name.get(1));
                        }
                    } else {
                        Name name = faker.name();
                        String fullName = name.fullName().intern();
                        creditCardInfo.setFullName(fullName);
                        creditCardInfo.setFirstName(name.firstName());
                        creditCardInfo.setLastName(name.lastName());
                    }
                    Address address = faker.address();
                    if (parts.length > 6 && StrUtil.isNotBlank(parts[5])) {
                        creditCardInfo.setAddress(parts[5].trim());
                    } else {
                        creditCardInfo.setAddress(address.streetAddress());
                    }
                    if (parts.length > 7 && StrUtil.isNotBlank(parts[6])) {
                        creditCardInfo.setCity(parts[6].trim());
                    } else {
                        creditCardInfo.setCity(address.city().trim());
                    }
                    if (parts.length > 8 && StrUtil.isNotBlank(parts[7])) {
                        String number = parts[7].trim();
                        String phoneNum = PhoneNumberUtils.removeCountryCode(number, "US");
                        Integer countryCode = PhoneNumberUtils.getCountryCode(number, "US");
                        creditCardInfo.setPhoneNumber(phoneNum);
                        creditCardInfo.setPhoneNumberCountry(countryCode);
                    } else {
                        String phoneNumber = faker.phoneNumber().phoneNumber();
                        String phoneNum = PhoneNumberUtils.removeCountryCode(phoneNumber, "US");
                        Integer countryCode = PhoneNumberUtils.getCountryCode(phoneNumber, "US");
                        creditCardInfo.setPhoneNumberCountry(countryCode);
                        creditCardInfo.setPhoneNumber(phoneNum);
                    }

                    if (parts.length > 9 && StrUtil.isNotBlank(parts[8])) {
                        creditCardInfo.setState(parts[8]);
                    } else {
                        creditCardInfo.setState(address.state().trim());
                    }

                    if (parts.length > 10 && StrUtil.isNotBlank(parts[9])) {
                        creditCardInfo.setZipCode(parts[9]);
                    } else {
                        creditCardInfo.setZipCode(address.zipCode().trim());
                    }

                    if (parts.length > 11 && StrUtil.isNotBlank(parts[10])) {
                        creditCardInfo.setEmail(parts[10]);
                    } else {
                        creditCardInfo.setEmail(faker.internet().emailAddress().intern());
                    }

                    if (parts.length > 12 && StrUtil.isNotBlank(parts[11])) {
                        creditCardInfo.setPassword(parts[11]);
                    } else {
                        creditCardInfo.setPassword(faker.internet().password(8, 13, true, true).intern());
                    }

                    dataList.add(creditCardInfo);
                }
            }
            if (CollectionUtil.isNotEmpty(dataList)) {
                cvvView.setItems(dataList);
                creditCardInfoService.saveOrUpdateBatch(dataList);
            }

        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<CreditCardInfo> creditCardInfoList = creditCardInfoService.list();
        if (CollectionUtil.isNotEmpty(creditCardInfoList)) {
            ObservableList<CreditCardInfo> dataList = FXCollections.observableArrayList();
            dataList.addAll(creditCardInfoList);
            cvvView.setItems(dataList);
        }

        //备份一次数据
        List<CreditCardInfo> creditCardInfos = BeanUtil.copyToList(creditCardInfoList, CreditCardInfo.class);
        originalDataList.clear();
        originalDataList.addAll(creditCardInfos); // 假设 dataList 是你当前的数据

        //全局可编辑
        cvvView.setEditable(Boolean.TRUE);

        //设置行号
        // 创建一个新列来显示行号
        TableColumn<CreditCardInfo, Number> indexColumn = new TableColumn<>("ID");
        indexColumn.setSortable(false); // 行号列通常不需要排序功能
        indexColumn.setMinWidth(40); // 设置适当的宽度

        // 设置单元格工厂来显示行号
        indexColumn.setCellFactory(column -> new TableCell<CreditCardInfo, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        cvvView.getColumns().add(1, indexColumn);

        //设置单选框
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        //设置单选框为可编辑
        selectColumn.setEditable(Boolean.TRUE);
        selectColumn.setSortable(false);

        setupEditableColumn(cardNumber, "cardNumber", new DefaultStringConverter(), CreditCardInfo::setCardNumber);
        setupEditableColumn(month, "monthNum", new DefaultStringConverter(), CreditCardInfo::setMonthNum);
        setupEditableColumn(year, "yearNum", new DefaultStringConverter(), CreditCardInfo::setYearNum);
        setupEditableColumn(securityCode, "securityCode", new DefaultStringConverter(), CreditCardInfo::setSecurityCode);
        setupEditableColumn(fullName, "fullName", new DefaultStringConverter(), CreditCardInfo::setFullName);
        setupEditableColumn(address, "address", new DefaultStringConverter(), CreditCardInfo::setAddress);
        setupEditableColumn(city, "city", new DefaultStringConverter(), CreditCardInfo::setCity);
        setupEditableColumn(phoneNumber, "phoneNumber", new DefaultStringConverter(), CreditCardInfo::setPhoneNumber);
        setupEditableColumn(state, "state", new DefaultStringConverter(), CreditCardInfo::setState);
        setupEditableColumn(zipCode, "zipCode", new DefaultStringConverter(), CreditCardInfo::setZipCode);
        setupEditableColumn(email, "email", new DefaultStringConverter(), CreditCardInfo::setEmail);
        setupEditableColumn(password, "password", new DefaultStringConverter(), CreditCardInfo::setPassword);
        setupEditableColumn(isActive, "isActive", new DefaultStringConverter(), CreditCardInfo::setIsActive);

        selectAllButton.setOnAction(event -> selectAll());
        deleteButton.setOnAction(event -> deleteSelected());

        // 初始化时创建线程池
        executorService = Executors.newFixedThreadPool(5);
        updateButtonLabel();
        initializeTable();
    }

    private void initializeTable() {
        setupEditableColumns();
        updateButtonVisibility(false); // 初始时不显示保存和取消按钮
    }

    private void setupEditableColumns() {
        // 初始设置所有列为不可编辑
        setColumnsEditable(false);
    }

    /**
     * 设置表格列为可编辑，并定义编辑行为。
     *
     * @param <T>          列的数据类型。
     * @param column       要设置的表格列。
     * @param propertyName 属性名称，用于绑定到CreditCardInfo对象的相应属性。
     * @param converter    字符串转换器，用于在编辑单元格时转换数据类型。
     * @param updateAction 在列编辑提交时执行的操作，更新CreditCardInfo对象。
     */
    private <T> void setupEditableColumn(TableColumn<CreditCardInfo, T> column, String propertyName, StringConverter<T> converter, BiConsumer<CreditCardInfo, T> updateAction) {

        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setCellFactory(col -> {
            TableCell<CreditCardInfo, T> cell = new TextFieldTableCell<>(converter);
            return cell;
        });
        //设置为不可编辑
        column.setEditable(Boolean.FALSE);
        column.setOnEditCommit(event -> {
            try {
                CreditCardInfo creditCardInfo = event.getRowValue();
                if ("fullName".equalsIgnoreCase(propertyName) && StrUtil.isNotBlank((String) event.getNewValue())) {
                    String newValue = Convert.toStr(event.getNewValue());
                    List<String> name = NameParser.getName(newValue);
                    if (CollectionUtil.isNotEmpty(name)) {
                        creditCardInfo.setFirstName(name.get(0));
                        creditCardInfo.setLastName(name.get(1));
                    }
                }
                updateAction.accept(creditCardInfo, event.getNewValue());
                //出现数据修改，还未保存到数据库中，将恢复按钮置为可见
                restoreButton.setVisible(Boolean.TRUE);
                saveButton.setVisible(Boolean.TRUE);
            } catch (Exception e) {
                log.error("修改失败:", e);
                showErrorDialog("修改失败"); // 在JavaFX线程中显示错误消息
                throw new RuntimeException(e);
            }

        });

    }

    public void showErrorDialog(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);

        alert.showAndWait();
    }

    //全选按钮
    private void selectAll() {
        selectStatus = !selectStatus;
        for (CreditCardInfo data : cvvView.getItems()) {
            data.setSelected(selectStatus);
        }
    }

    //删除按钮，删除数据
    private void deleteSelected() {

        boolean removeIf = cvvView.getItems().removeIf(CreditCardInfo::isSelected);
        if (removeIf) {
            //删除了数据，此时还未保存到数据库中，将取消按钮置为可见
            restoreButton.setVisible(Boolean.TRUE);
            saveButton.setVisible(Boolean.TRUE);
        }
    }

    //保存按钮，保存数据
    public void saveCvvData() {
        // 先删除以前的数据
        creditCardInfoService.remove(new QueryWrapper<>());
        // 保存或修改CCV数据
        if (CollectionUtil.isNotEmpty(cvvView.getItems())) {
            creditCardInfoService.saveOrUpdateBatch(cvvView.getItems());
        }

        // 将新数据放入originalData，作为备份
        originalDataList.clear();
        originalDataList.addAll(creditCardInfoService.list());
        // 保存后，恢复按钮和保存按钮置为不可见
        saveButton.setVisible(Boolean.FALSE);
        restoreButton.setVisible(Boolean.FALSE);
        // 取消可以编辑的状态
        //this.updateColumnEditable(Boolean.FALSE);
        setColumnsEditable(Boolean.FALSE);
        // 更新 isEditMode 状态
        isEditMode = false;
        updateButtonVisibility(isEditMode); // 根据当前的编辑模式，更新按钮的可见性
        cvvView.edit(-1, null); // 结束任何激活的编辑
    }

    //编辑按钮，将每一行都变为可编辑
    public void modifyCvvData(ActionEvent actionEvent) {
        isEditMode = !isEditMode; // 切换编辑模式状态
        setColumnsEditable(isEditMode); // 设置列的可编辑状态
        updateButtonVisibility(isEditMode); // 根据是否是编辑模式显示或隐藏按钮
    }

    private void setColumnsEditable(boolean editable) {
        // 遍历所有列，设置为可编辑或不可编辑
        for (TableColumn column : cvvView.getColumns()) {
            column.setEditable(editable);
        }
    }
    private void updateButtonVisibility(boolean visible) {
        saveButton.setVisible(visible);
        restoreButton.setVisible(visible);
    }

    private void updateColumnEditable(Boolean isEdit) {
        // 设置表格的每一列为可编辑
        setColumnEditable(cardNumber, isEdit);
        setColumnEditable(month, isEdit);
        setColumnEditable(year, isEdit);
        setColumnEditable(securityCode, isEdit);
        setColumnEditable(fullName, isEdit);
        setColumnEditable(address, isEdit);
        setColumnEditable(city, isEdit);
        setColumnEditable(phoneNumber, isEdit);
        setColumnEditable(state, isEdit);
        setColumnEditable(zipCode, isEdit);
        setColumnEditable(email, isEdit);
        setColumnEditable(password, isEdit);
        setColumnEditable(isActive, isEdit);
    }

    private <T> void setColumnEditable(TableColumn<CreditCardInfo, T> column, boolean editable) {
        column.setEditable(editable);
    }

    public void restoreCvvData(ActionEvent actionEvent) {
        if (CollectionUtil.isNotEmpty(originalDataList)) {
            List<CreditCardInfo> creditCardInfos = BeanUtil.copyToList(originalDataList, CreditCardInfo.class);
            cvvView.setItems(FXCollections.observableArrayList(creditCardInfos));
            cvvView.refresh();
        }
        // 明确设置为非编辑模式
        isEditMode = false;
        // 禁用表格的编辑功能
        setColumnsEditable(false);
        // 更新按钮可见性
        updateButtonVisibility(false);
        // 结束任何激活的编辑
        cvvView.edit(-1, null);
    }

    @FXML
    private void executeTask(ActionEvent event) {
        // 切换任务状态
        if (paused.getAndSet(false)) { // 如果当前是暂停状态，点击后开始执行
            if (!running.getAndSet(true)) { // 如果任务未在运行，启动它
                startTasks();
                Platform.runLater(() -> executeButton.setText("停止")); // 更新按钮文本
            }
        } else {
            // 暂停任务
            stopTasks();
            paused.set(true);
            running.set(false);
            Platform.runLater(() -> executeButton.setText("执行")); // 更新按钮文本
        }
    }

    private void startTasks() {
        ensureExecutorService();  // 确保线程池是活跃的
        List<CreditCardInfo> list = creditCardInfoService.list();
        if (CollectionUtil.isNotEmpty(list)) {
            for (CreditCardInfo info : list) {

                executorService.submit(() -> {
                    try {
                        LfgUtils.start(info); // 执行具体任务
                    } catch (Exception e) {
                        log.error("执行任务失败", e);
                    }
                });
            }
        }
    }

    private void stopTasks() {
        // 停止正在执行的任务
        if (!executorService.isShutdown() && !executorService.isShutdown()) {
            executorService.shutdownNow(); // 尝试立即停止所有正在执行的任务
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow(); // 如果任务仍未结束，再次尝试停止
                }
            } catch (InterruptedException ex) {
                executorService.shutdownNow(); // 在中断时尝试停止
                Thread.currentThread().interrupt();
            }
        }
        // 关闭所有活跃的浏览器实例
        LfgUtils.closeAllBrowsers();
    }

    private void updateButtonLabel() {
        if (paused.get()) {
            executeButton.setText("执行");
        } else {
            executeButton.setText("暂停");
        }
    }

    private void ensureExecutorService() {
        // 检查并重新初始化线程池
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(5);
        }
    }
}
