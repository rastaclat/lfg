import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;

@SpringBootApplication
@ComponentScan(basePackages = {"me.bramar.*"})
@MapperScan("me.bramar.task.mapper")
public class AppApplication extends Application {

    private static Stage mainStage;
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() throws Exception {
        // 初始化Spring应用程序上下文
        applicationContext = new SpringApplicationBuilder(AppApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        mainStage = primaryStage;
        // 展示窗口
        changeView("/view/main.fxml"); // 确保第一次视图加载能够处理Spring依赖注入
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // 关闭Spring应用程序上下文
        applicationContext.close();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void changeView(String fxml) throws IOException {
        // 使用Spring应用程序上下文加载FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
        fxmlLoader.setControllerFactory(applicationContext::getBean);

        Parent root = fxmlLoader.load();
        mainStage.setScene(new Scene(root));
    }

    public void showErrorDialog(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);

        alert.showAndWait();
    }

}