package me.bramar.task.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import me.bramar.task.utils.NameParser;
import me.bramar.task.utils.PhoneNumberUtils;
import me.bramar.task.utils.XjpDsnUtils;
import net.datafaker.Faker;
import net.datafaker.providers.base.Address;
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
                       /* Name name = faker.name();
                        String fullName = name.fullName().intern();
                        creditCardInfo.setFullName(fullName);
                        creditCardInfo.setFirstName(name.firstName());
                        creditCardInfo.setLastName(name.lastName());*/
                    }
                    Address address = faker.address();
                    if (parts.length > 6 && StrUtil.isNotBlank(parts[5])) {
                        creditCardInfo.setAddress(parts[5].trim());
                    } else {
                        //creditCardInfo.setAddress(address.streetAddress());
                    }
                    if (parts.length > 7 && StrUtil.isNotBlank(parts[6])) {
                        creditCardInfo.setCity(parts[6].trim());
                    } else {
                        // creditCardInfo.setCity(address.city().trim());
                    }
                    if (parts.length > 8 && StrUtil.isNotBlank(parts[7])) {
                        String number = parts[7].trim();
                        String phoneNum = PhoneNumberUtils.removeCountryCode(number, "US");
                        creditCardInfo.setPhoneNumber(phoneNum);
                    } else {
                        /*String phoneNumber = faker.phoneNumber().phoneNumber();
                        String phoneNum = PhoneNumberUtils.removeCountryCode(phoneNumber, "US");
                        creditCardInfo.setPhoneNumber(phoneNum);*/
                    }

                    if (parts.length > 9 && StrUtil.isNotBlank(parts[8])) {
                        creditCardInfo.setState(parts[8]);
                    } else {
                        // creditCardInfo.setState(address.state().trim());
                    }

                    if (parts.length > 10 && StrUtil.isNotBlank(parts[9])) {
                        creditCardInfo.setZipCode(parts[9]);
                    } else {
                        //creditCardInfo.setZipCode(address.zipCode().trim());
                    }

                    if (parts.length > 11 && StrUtil.isNotBlank(parts[10])) {
                        creditCardInfo.setEmail(parts[10]);
                    } else {
                        //creditCardInfo.setEmail(faker.internet().emailAddress().intern());
                    }

                    if (parts.length > 12 && StrUtil.isNotBlank(parts[11])) {
                        creditCardInfo.setPassword(parts[11]);
                    } else {
                        //creditCardInfo.setPassword(faker.internet().password(8, 13, true, true).intern());
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

        selectAllButton.setOnAction(event -> selectAll());
        deleteButton.setOnAction(event -> deleteSelected());

        // 初始化时创建线程池
        executorService = Executors.newFixedThreadPool(5);
        updateButtonLabel();
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
        //先删除以前的数据
        creditCardInfoService.remove(new QueryWrapper<>());
        //保存或修改CCV数据
        if (CollectionUtil.isNotEmpty(cvvView.getItems())) {
            creditCardInfoService.saveOrUpdateBatch(cvvView.getItems());
        }

        //将新数据放入originalData，作为备份
        originalDataList.clear();
        originalDataList.addAll(creditCardInfoService.list());
        //保存后，恢复置为不可见
        saveButton.setVisible(Boolean.FALSE);
        restoreButton.setVisible(Boolean.FALSE);
        //取消可以编辑的状态
        this.updateColumnEditable(Boolean.FALSE);
    }

    //编辑按钮，将每一行都变为可编辑
    public void modifyCvvData(ActionEvent actionEvent) {
        this.updateColumnEditable(Boolean.TRUE);
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
        restoreButton.setVisible(Boolean.FALSE);
        saveButton.setVisible(Boolean.FALSE);

    }

    public void refreshTableView() {
        List<CreditCardInfo> creditCardInfos = creditCardInfoService.list();
        ObservableList<CreditCardInfo> observableList = FXCollections.observableArrayList(creditCardInfos);
        originalDataList.clear();
        originalDataList.addAll(creditCardInfoService.list()); //防止添加的是引用
        cvvView.setItems(observableList);
        cvvView.refresh();
    }

    public void executeTask(ActionEvent actionEvent) {
        if (paused.getAndSet(false)) {
            if (!running.getAndSet(true)) {
                startTasks();
            } else {
                // 任务已经在运行，此时点击表示暂停
                paused.set(true);
                updateButtonLabel();
            }
        } else {
            // 暂停任务
            paused.set(true);
            updateButtonLabel();
        }
    }

    private void startTasks() {
        // 示例任务：在这里启动您的具体任务
        // 这里仅为示例，您需要替换为实际任务逻辑
        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                while (running.get()) {
                    if (paused.get()) {
                        try {
                            Thread.sleep(100); // 暂停时简单休眠
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                    // 执行具体的任务逻辑
                    try {
                        XjpDsnUtils.executeMethod();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }

                }
            });
        }
        updateButtonLabel();
    }

    private void updateButtonLabel() {
        if (paused.get()) {
            executeButton.setText("执行");
        } else {
            executeButton.setText("暂停");
        }
    }
}
