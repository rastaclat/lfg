package me.bramar.undetectedselenium;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriverLogLevel;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * @author qtq
 * @since 2024-04-02 17:32
 */
public class Test2 {

    private static final String testUrl = "https://www.ticketlouvre.fr/louvre/b2c/index.cfm/home";
    public static int waitTime = 60;

    public static void main(String[] args) throws IOException, ReflectiveOperationException, URISyntaxException {
        test3();
    }

    public static void test3() throws IOException, ReflectiveOperationException, URISyntaxException {

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
            .versionMain(115)
            .options(chromeOptions)
            .serviceBuilder(new ChromeDriverService.Builder().withSilent(true).withLogLevel(ChromiumDriverLogLevel.OFF))
            .seleniumStealth(SeleniumStealthOptions.getDefault()).build();

        driver.cloudflareGet(testUrl);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(waitTime));
        //1.点击页面
        click(wait,driver,"/html[1]/body[1]/div[1]/div[1]/div[2]/div[1]/div[1]/div[2]/div[1]/div[2]/div[1]/div[1]/div[1]/img[1]");

        //2.找到可选日期
        // 等待直到所有可选日期的元素都可见
        List<WebElement> availableDates = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("td.high_availability")));

        WebElement nearestDateElement = null;
        LocalDate nearestDate = LocalDate.MAX;

        for (WebElement dateElement : availableDates) {
            // 解析日期信息
            int day = Integer.parseInt(dateElement.findElement(By.tagName("a")).getText().trim()); // 假设日期是在<a>标签中
            int month = Integer.parseInt(dateElement.getAttribute("data-month")) + 1; // 月份从0开始计算，所以加1
            int year = Integer.parseInt(dateElement.getAttribute("data-year"));

            LocalDate date = LocalDate.of(year, month, day);

            // 检查日期是否为最近的一个
            if (date.isBefore(nearestDate)) {
                nearestDate = date;
                nearestDateElement = dateElement;
            }
        }

        if (nearestDateElement != null) {
            System.out.println("找到最近的可选日期：" + nearestDate);
            // 执行点击操作之前，确保元素是可点击的
            WebElement clickableDate = wait.until(ExpectedConditions.elementToBeClickable(nearestDateElement));
            clickableDate.click();
        } else {
            System.out.println("没有找到可选日期。");
        }

        //3.选择票数
        // 等待直到<select>元素可见
        WebElement selectElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("jq-basket-item-quantity")));
        // 使用Select类处理<select>元素
        Select select = new Select(selectElement);
        // 选择值为"1"的<option>
        select.selectByValue("1");
        //4.点击确认订单
        WebElement finalizeButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.button-next.jq-a-disable.jq-basket-submit.jq-basket-submit-do")));
        finalizeButton.click();

        //5.同意协议
        // 使用WebDriverWait等待复选框变为可点击状态
        WebElement checkbox = wait.until(ExpectedConditions.elementToBeClickable(By.name("cgvConfirm")));

        // 检查复选框是否已经被选中
        if (!checkbox.isSelected()) {
            // 如果复选框未被选中，则点击它
            checkbox.click();
        }

        //6.点击确认订单
        WebElement checkoutConfirmButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("jq-checkout-confirm")));
        checkoutConfirmButton.click();

        //7.注册账号


        w();
        driver.quit();
    }

    public static void w() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press [ENTER] to quit");
        scanner.nextLine();
        scanner.close();
        System.out.println("Quitting");
    }

    private static void click(WebDriverWait wait, WebDriver driver, String xpath) {
        // 等待元素可见
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        // 再次确保元素可点击
        element = wait.until(ExpectedConditions.elementToBeClickable(element));

        // 尝试将元素滚动到视口中央
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);

        // 短暂等待以确保页面滚动完成
        randomSleep(500, 1000);

        try {
            // 尝试正常点击
            element.click();
        } catch (ElementClickInterceptedException e) {
            // 如果正常点击失败，尝试使用JavaScript点击
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            // 如果其他错误发生，使用Actions类尝试点击
            new Actions(driver).moveToElement(element).click().perform();
        }


        // 额外的等待时间，以确保任何由点击引发的操作都有时间执行
        randomSleep(500, 3000);
    }

    private static void clickButton(WebDriverWait wait, WebDriver driver, String xpath) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
        randomSleep(500, 3000);
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
