package me.bramar.task.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import com.mmg.ddddocr4j.utils.DDDDOcrUtil;
import me.bramar.task.entity.CreditCardInfo;
import me.bramar.undetectedselenium.SeleniumStealthOptions;
import me.bramar.undetectedselenium.Test2;
import me.bramar.undetectedselenium.UndetectedChromeDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriverLogLevel;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * @author qtq
 * @since 2024-04-02 17:32
 */
public class XjpDsnUtils {

    private static final String testUrl = "https://www.rwsentosa.com/en/attractions/universal-studios-singapore";
    public static int waitTime = 60;

    public static void main(String[] args) throws IOException, ReflectiveOperationException, URISyntaxException {
        executeMethod();
    }

    public static void executeMethod(CreditCardInfo creditCardInfo) throws IOException, ReflectiveOperationException, URISyntaxException {

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

        //公共方法
        commonExecuteMethod(wait, driver);

        //输入姓名
        fillInputName(creditCardInfo, wait);

        //输入email
        fillInputEmail(creditCardInfo, wait);

        //选择国籍
        fillInputNationality(wait, driver);

        //选择国际区号1和输入电话号码
        fillInputPhone(creditCardInfo, wait, driver);

        //同意条款
        agreeTerms(wait, driver);

        // 尝试提交表单
        click(wait, driver, By.id("fxb_75afc65d-8d35-403d-bd73-c48867e5eb18_b7bbe0b3-b58f-4e40-9947-60b17e7a3136"));

        submitFormAndHandleErrors(driver, wait, creditCardInfo);
        w();
        driver.quit();
    }

    private static void agreeTerms(WebDriverWait wait, WebDriver driver) {
        //点击同意
        click(wait, driver, By.xpath("//label[@for='fxb_75afc65d-8d35-403d-bd73-c48867e5eb18_Fields_c4da8c29-4028-4f6a-90a9-6d5469db9186__Value']//span[@class='custom-checkbox']"));
        click(wait, driver, By.xpath("//label[contains(text(),'I consent to Resorts World Sentosa (RWS) to collec')]"));
        //选择email
        click(wait, driver, By.xpath("//label[normalize-space()='E-mail']"));
    }

    //输入电话号码
    private static void fillInputPhone(CreditCardInfo creditCardInfo, WebDriverWait wait, WebDriver driver) {
        click(wait, driver, By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='*Contact number'])[1]/following::div[4]"));
        click(wait, driver, By.xpath("//*/text()[normalize-space(.)='1']/parent::*"));
        fillInput(wait, By.id("fxb_75afc65d-8d35-403d-bd73-c48867e5eb18_Fields_c8528200-a461-4c37-8fe5-a706e760f5db__Value"), creditCardInfo.getCardNumber());
    }

    //选择国籍
    private static void fillInputNationality(WebDriverWait wait, WebDriver driver) {
        click(wait, driver, By.xpath("//div[@id='fxb_75afc65d-8d35-403d-bd73-c48867e5eb18_Fields_bf5d3d53-129f-4d58-85b1-30357e3a9e95__Value']/div/div"));
        click(wait, driver, By.xpath("//div[@id='fxb_75afc65d-8d35-403d-bd73-c48867e5eb18_Fields_bf5d3d53-129f-4d58-85b1-30357e3a9e95__Value']/div[2]/div/div[18]"));
    }

    //输入email
    private static void fillInputEmail(CreditCardInfo creditCardInfo, WebDriverWait wait) {
        fillInput(wait, By.id("fxb_75afc65d-8d35-403d-bd73-c48867e5eb18_Fields_172fafce-9ab6-41f1-b644-0e405e9e2313__Value"), creditCardInfo.getEmail());
        fillInput(wait, By.id("fxb_75afc65d-8d35-403d-bd73-c48867e5eb18_Fields_172fafce-9ab6-41f1-b644-0e405e9e2313__ConfirmEmail"), creditCardInfo.getEmail());
    }

    //输入姓名
    private static void fillInputName(CreditCardInfo creditCardInfo, WebDriverWait wait) {
        fillInput(wait, By.id("fxb_75afc65d-8d35-403d-bd73-c48867e5eb18_Fields_12bec712-0abe-4228-b8a6-8a3bec1ba62a__Value"), creditCardInfo.getFirstName());
        fillInput(wait, By.id("fxb_75afc65d-8d35-403d-bd73-c48867e5eb18_Fields_f84c0c3e-e3c5-4896-97cf-f52320714d65__Value"), creditCardInfo.getLastName());
    }

    //选择国家
    private static void selectRegion(WebDriverWait wait, WebDriver driver) {
        //选择国家
        click(wait, driver, By.xpath("//div[@id='fxb_75afc65d-8d35-403d-bd73-c48867e5eb18_Fields_26997082-04fe-42fa-aad7-54b764039648__Value']/div/div"));
        //选择美国
        click(wait, driver, By.xpath("//*/text()[normalize-space(.)='United States of America']/parent::*"));
    }

    //输入验证码
    private static void fillInputCaptcha(WebDriverWait wait, WebDriver driver) {
        // 通过XPath找到图片
        WebElement captchaElement = driver.findElement(By.xpath("//img[@alt='captcha']"));

        // 截图该元素
        File screenshot = captchaElement.getScreenshotAs(OutputType.FILE);
        String code = DDDDOcrUtil.getCode(Base64.encode(screenshot)).toLowerCase();
        System.out.println("验证码：" + code);
        fillInput(wait, By.xpath("//input[@id='fxb_75afc65d-8d35-403d-bd73-c48867e5eb18_Fields_70b16536-6289-4b9d-ba30-3de308232b21__CaptchaCode']"), code);
    }

    //检查提交错误
    public static void submitFormAndHandleErrors(WebDriver driver, WebDriverWait wait, CreditCardInfo creditCardInfo) {
        for (int i = 0; i < 5; i++) {
            boolean isError = false;

            //出现未输入姓名提示
            List<WebElement> firstNameElements = driver.findElements(By.xpath("//p[normalize-space()='*First name is required.']"));
            List<WebElement> lastNameElement = driver.findElements(By.xpath("//p[normalize-space()='*Last name is required.']"));
            if (CollectionUtil.isNotEmpty(firstNameElements) || CollectionUtil.isNotEmpty(lastNameElement)) {
                isError = true;
                fillInputName(creditCardInfo, wait);
            }

            //出现未输入邮箱提示
            List<WebElement> emailElement = driver.findElements(By.xpath("//p[normalize-space()='*Email is required.']"));
            List<WebElement> confirmEmailElement = driver.findElements(By.xpath("//p[normalize-space()='The *Confirm Email and *Email do not match.']"));
            if (CollectionUtil.isNotEmpty(emailElement) || CollectionUtil.isNotEmpty(confirmEmailElement)) {
                isError = true;
                //重新输入邮箱
                fillInputEmail(creditCardInfo, wait);
            }

            //出现未选择国家
            List<WebElement> regionElement = driver.findElements(By.xpath("//p[normalize-space()='*Country / Region of Residence is required.']"));
            if (CollectionUtil.isNotEmpty(regionElement)) {
                isError = true;
                selectRegion(wait, driver);
            }

            //出现未选择国籍
            List<WebElement> nationalityElement = driver.findElements(By.xpath("//p[normalize-space()='*Nationality is required.']"));
            if (CollectionUtil.isNotEmpty(nationalityElement)) {
                isError = true;
                fillInputNationality(wait, driver);
            }

            //出现未选择国籍
            List<WebElement> phoneElement = driver.findElements(By.xpath("//p[normalize-space()='*Contact number is required.']"));
            if (CollectionUtil.isNotEmpty(phoneElement)) {
                isError = true;
                fillInputPhone(creditCardInfo, wait, driver);
            }

            //输入验证码
            List<WebElement> captchaElement = driver.findElements(By.xpath("//p[normalize-space()='Please enter a valid verification code.']"));
            if (CollectionUtil.isNotEmpty(captchaElement)) {
                isError = true;
                fillInputCaptcha(wait, driver);
            }

            //不存在错误
            if (!isError) {
                break;
            } else {
                //存在错误再次提交
                // 尝试提交表单
                click(wait, driver, By.id("fxb_75afc65d-8d35-403d-bd73-c48867e5eb18_b7bbe0b3-b58f-4e40-9947-60b17e7a3136"));
            }
        }

    }


    public static void executeMethod() throws IOException, ReflectiveOperationException, URISyntaxException {

        ChromeOptions chromeOptions = new ChromeOptions();

        String chromeBinaryPath = "F:\\Chrome\\App\\chrome.exe"; // Update this with the actual Chrome path
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
        commonExecuteMethod(wait, driver);

        w();
        driver.quit();
    }

    private static void commonExecuteMethod(WebDriverWait wait, UndetectedChromeDriver driver) {
        //点击预定
        click(wait, driver, By.xpath("//div[@id='root']/header/nav/div/div[2]/section/button"));
        //点击选择日期
        clickDate((JavascriptExecutor) driver);
        //点击立即预定
        click(wait, driver, By.xpath("//div[@id='root']/header/nav/div/div[2]/section/div/div/section/button"));
        //选择第一个项目
        click(wait, driver, By.xpath("//div[@id='container']/section/div[2]/div/div[2]/div/div[2]/div[2]/div[2]/button"));
        //点击+1
        click(wait, driver, By.xpath("//span[4]"));
        //点击添加到购物车
        click(wait, driver, By.xpath("//*/text()[normalize-space(.)='Add To Cart']/parent::*"));
        //点击查看购物车
        click(wait, driver, By.xpath("//div[@id='root']/header/div[2]/div/div/div[2]/div/div[2]/div/a/div"));
        //点击结算
        click(wait, driver, By.xpath("//*/text()[normalize-space(.)='Check out']/parent::*"));
        selectRegion(wait, driver);

        //输入验证码
        fillInputCaptcha(wait, driver);
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

    private static void fillInput(WebDriverWait wait, By byPath, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(byPath));
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
    }

    private static void selectOption(WebDriverWait wait, WebDriver driver, String xpath) {
        // pauseIfNeeded();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath))).click();
    }

    private static void click(WebDriverWait wait, WebDriver driver, By byPath) {
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
