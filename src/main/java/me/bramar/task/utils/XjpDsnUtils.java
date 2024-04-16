package me.bramar.task.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestBody;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.dtflys.forest.http.body.NameValueRequestBody;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.mmg.ddddocr4j.utils.DDDDOcrUtil;
import me.bramar.undetectedselenium.SeleniumStealthOptions;
import me.bramar.undetectedselenium.Test2;
import me.bramar.undetectedselenium.UndetectedChromeDriver;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriverLogLevel;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

/**
 * @author qtq
 * @since 2024-04-02 17:32
 */
public class XjpDsnUtils {

    private static final String testUrl = "https://www.rwsentosa.com/en/attractions/universal-studios-singapore";
    public static int waitTime = 60;

    public static void main(String[] args) throws IOException, ReflectiveOperationException, URISyntaxException, InterruptedException {
        executeMethod();
    }

    public static void executeMethod() throws IOException, ReflectiveOperationException, URISyntaxException {

        ChromeOptions chromeOptions = new ChromeOptions();

        String chromeBinaryPath = "E:\\Chrome\\App\\chrome.exe"; // Update this with the actual Chrome path
        chromeOptions.setBinary(chromeBinaryPath);

        // 加载两个扩展目录
        // 获取扩展目录的绝对路径
        URL proExtensionDirectoryURL = Test2.class.getClassLoader().getResource("pro_1.1.30");
        if (proExtensionDirectoryURL == null) {
            System.err.println("Cannot find 'pro_1.1.30' extension directory in resources.");
            return;
        }
        String proExtensionDirectoryPath = Paths.get(proExtensionDirectoryURL.toURI()).toFile().getAbsolutePath();

        URL canvasBlockerExtensionDirectoryURL = Test2.class.getClassLoader().getResource("canvasblocker");
        if (canvasBlockerExtensionDirectoryURL == null) {
            System.err.println("Cannot find 'canvasblocker' extension directory in resources.");
            return;
        }
        String canvasBlockerExtensionDirectoryPath = Paths.get(canvasBlockerExtensionDirectoryURL.toURI()).toFile().getAbsolutePath();

        chromeOptions.addArguments("--load-extension=" + proExtensionDirectoryPath + "," + canvasBlockerExtensionDirectoryPath);
        // 添加额外的隐匿模式和性能优化选项
        chromeOptions.addArguments("--disable-blink-features=AutomationControlled");

        UndetectedChromeDriver driver = UndetectedChromeDriver.builder()
            .pageLoadStrategy(PageLoadStrategy.NONE)
            .headless(false)
            .driverFromCFT(true)
            .versionMain(117)
            .options(chromeOptions)
            .serviceBuilder(new ChromeDriverService.Builder().withSilent(true).withLogLevel(ChromiumDriverLogLevel.OFF))
            .seleniumStealth(SeleniumStealthOptions.getDefault()).build();

        driver.cloudflareGet(testUrl);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(waitTime));
        //点击预定
        click(wait, driver, By.xpath("//div[@id='root']/header/nav/div/div[2]/section/button"));
        //点击选择日期
        clickDate((JavascriptExecutor) driver);
        //点击立即预定
        click(wait, driver, By.xpath("//div[@id='root']/header/nav/div/div[2]/section/div/div/section/button"));
        //选择第一个项目
        click(wait, driver, By.xpath("//div[@id='container']/section/div[2]/div/div[2]/div/div[2]/div[2]/div[2]/button"));
        //点击+1
        click(wait,driver,By.xpath("//span[4]"));
        //点击添加到购物车
        click(wait, driver, By.xpath("//*/text()[normalize-space(.)='Add To Cart']/parent::*"));
        //点击查看购物车
        click(wait, driver, By.xpath("//div[@id='root']/header/div[2]/div/div/div[2]/div/div[2]/div/a/div"));
        //点击结算
        click(wait, driver, By.xpath("//*/text()[normalize-space(.)='Check out']/parent::*"));

        //选择国家
        click(wait, driver, By.xpath("//div[@id='fxb_75afc65d-8d35-403d-bd73-c48867e5eb18_Fields_26997082-04fe-42fa-aad7-54b764039648__Value']/div/div"));
        //选择美国
        click(wait, driver, By.xpath("//*/text()[normalize-space(.)='United States of America']/parent::*"));


        /*// 找到需要右键点击的元素
        WebElement element = driver.findElement(By.xpath("//img[@alt='captcha']"));

        // 创建 Actions 对象
        Actions actions = new Actions(driver);

        // 右键点击指定的元素
        actions.contextClick(element).perform();

        // 找到右键菜单中的所有项
        List<WebElement> menuItems = driver.findElements(By.xpath("//img[@alt='captcha']"));

        // 打印菜单中的所有项
        System.out.println("右键菜单中的所有项:");
        for (WebElement menuItem : menuItems) {
            System.out.println(menuItem.getText());
        }*/

        // 通过XPath找到图片
        WebElement captchaElement = driver.findElement(By.xpath("//img[@alt='captcha']"));

        // 截图该元素
        File screenshot = captchaElement.getScreenshotAs(OutputType.FILE);
        DDDDOcrUtil.getCode(Base64.encode(screenshot));




        w();
        driver.quit();
    }

    public static void test4() {
        String url = "/BotDetectCaptcha.ashx?get=image&c=FormCaptcha&t=5a1232fa6c7b46ae8cecc918ed1bb888";

    }

    private static void clickDate(JavascriptExecutor driver) {
        String jsCode = "// 找到日期输入框元素\n" +
                "const dateInput = document.querySelector('.styled-input.sg-input.styled-input--date');\n\n" +
                "// 如果找到了日期输入框元素\n" +
                "if (dateInput) {\n" +
                "  // 模拟鼠标点击来激活日期输入框\n" +
                "  dateInput.click();\n\n" +
                "  // 找到所有可选择的日期元素\n" +
                "  const selectableDates = document.querySelectorAll('.DayPicker-Day:not(.DayPicker-Day--disabled)');\n\n" +
                "  // 如果找到了可选择的日期元素\n" +
                "  if (selectableDates.length > 0) {\n" +
                "    // 选择第三个可选择的日期元素\n" +
                "    const thirdSelectableDate = selectableDates[2];\n    \n" +
                "    // 模拟鼠标点击来选择日期\n" +
                "    thirdSelectableDate.click();\n" +
                "  } else {\n" +
                "    // 如果没有找到可选择的日期元素，则输出一条消息\n" +
                "    console.log('没有找到可选择的日期元素');\n" +
                "  }\n" +
                "} else {\n" +
                "  // 如果没有找到日期输入框元素，则输出一条消息\n" +
                "  console.log('没有找到日期输入框元素');\n" +
                "}";
        driver.executeScript(jsCode);
    }

    public static void w() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press [ENTER] to quit");
        scanner.nextLine();
        scanner.close();
        System.out.println("Quitting");
    }

    private static void fillInput(WebDriverWait wait, WebDriver driver, String xpath, String value) throws InterruptedException {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        Random random = new Random();
        element.clear();
        for (int i = 0; i < value.length(); i++) {
            //pauseIfNeeded();
            char ch = value.charAt(i);
            // 模拟按键输入
            element.sendKeys(String.valueOf(ch));

            // 增加随机延时，范围从100到500毫秒，使打字速度更慢、更不规则
            try {
                Thread.sleep(100 + random.nextInt(400)); // 随机延时100到500毫秒之间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted during simulated typing.");
            }

            // 增加输错的概率到10%
            if (random.nextFloat() < 0.1) { // 假设有10%的概率输入错误
                element.sendKeys("\b"); // 使用退格键删除错误字符
                // 等待一段时间后再次输入，模拟用户发现并更正错误的过程
                try {
                    Thread.sleep(100 + random.nextInt(400));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Thread was interrupted during error correction.");
                }
                element.sendKeys(String.valueOf(ch)); // 重新输入正确字符
            }
        }
        //randomSleep(500, 3000);
    }

    private static void selectOption(WebDriverWait wait, WebDriver driver, String xpath)  {
       // pauseIfNeeded();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath))).click();
    }

    private static void click(WebDriverWait wait, WebDriver driver, By byPath)  {
       // pauseIfNeeded();
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(byPath));
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
        randomSleep(500, 3000);
    }

    private boolean checkVerificationFailure(WebDriver driver) {
       // pauseIfNeeded();
        try {
            driver.findElement(By.xpath("//*[contains(text(), 'Verification Failure')]"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 随机等待方法
    private static void randomSleep(int min, int max) {
        Random random = new Random();
        try {
            Thread.sleep(min + random.nextInt(max - min));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Interrupted during random sleep.");
        }
    }
}
