package me.bramar.undetectedselenium;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

@Slf4j
class Test3 {
    private static final String testUrl = "https://www.ticketlouvre.fr/louvre/b2c/index.cfm/home";
    private static final String testUrl2 = "https://www.ticketlouvre.fr/louvre/b2c/index.cfm/calendar/eventCode/MusWeb";
    public static int waitTime = 60;

    public static void main(String[] args) throws IOException, ReflectiveOperationException, URISyntaxException, InterruptedException {
        test2();
    }

    public static void w() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press [ENTER] to quit");
        scanner.nextLine();
        scanner.close();
        System.out.println("Quitting");
    }


    public static void test2() throws URISyntaxException, InterruptedException {
        // 设置扩展的路径
        URL myFingerprintChromeUrl = Test3.class.getClassLoader().getResource("my-fingerprint-chrome-1.2.1");
        if (myFingerprintChromeUrl == null) {
            System.err.println("Cannot find 'my-fingerprint-chrome-1.2.1' extension directory in resources.");
            return;
        }
        String myFingerprintChromeUrlPath = Paths.get(myFingerprintChromeUrl.toURI()).toFile().getAbsolutePath();
        // Playwright 实例
        try (Playwright playwright = Playwright.create()) {
            // 创建浏览器类型
            BrowserType browserType = playwright.chromium();

            // 启动参数配置
            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(false) // 设置为非无头模式
                .setArgs(List.of(
                    "--start-maximized", // 启动最大化
                    "--disable-extensions-except=" + myFingerprintChromeUrlPath,
                    "--load-extension=" + myFingerprintChromeUrlPath,
                    "--disable-blink-features=AutomationControlled",
                    "--disable-gpu",
                    "--disable-software-rasterize"));
            // 启动浏览器
            Browser browser = browserType.launch(options);
            // 创建浏览器上下文
            BrowserContext context = browser.newContext();
            // 创建页面
            Page page = context.newPage();
            // JavaScript 脚本用于修改 navigator 对象，防止检测到自动化工具
            String js = "Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});";

            // 在每个新页面加载前执行 JavaScript
            page.addInitScript(js);
            // 尝试导航，最多重试三次
            boolean success = false;
            for (int i = 0; i < 3 && !success; i++) {
                try {
                    page.navigate(testUrl2);
                    success = true; // 页面成功加载
                } catch (PlaywrightException e) {
                    System.out.println("网络错误，正在尝试重新连接... (" + (i + 1) + ")");
                    try {
                        Thread.sleep(2000); // 等待2秒后重试
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (!success) {
                System.out.println("无法加载页面，请检查网络连接");
                return;
            }

            log.info("打开网页");
            //2.找到可选日期
            // 等待日期选择器元素加载完毕
            page.waitForSelector(".ui-datepicker");
            List<ElementHandle> elementHandles = page.querySelectorAll("td.high_availability");
            // 获取当前日期，并偏移10天作为参考日期
            LocalDate referenceDate = LocalDate.now().plusDays(10);
            int curMonthValue = referenceDate.getMonthValue();
            if (CollectionUtil.isEmpty(elementHandles)) {
                log.info("没有找到日期元素");
                return;
            }
            ElementHandle elementHandle1 = elementHandles.get(0);
            String selectMonth = elementHandle1.getAttribute("data-month");
            if (Convert.toInt(selectMonth) != curMonthValue) {
                //点击下个月重新查找
                page.locator("//div[@id='vcDialogTitle0']|//div[@role='documentcontent']").waitFor();
                page.locator("//div[@id='vcDialogTitle0']|//div[@role='documentcontent']").click();

                elementHandles = page.querySelectorAll("td.high_availability"); //123主页

                if (CollectionUtil.isEmpty(elementHandles)) {
                    log.info("从下个月中，没有找到日期元素");
                    return;
                }
            }

           /* for (ElementHandle elementHandle : elementHandles) {
                String year = elementHandle.getAttribute("data-year"); // 2024
                String month = elementHandle.getAttribute("data-month"); // 4
                String day = elementHandle.textContent(); // 3

                if (curMonthValue != Integer.parseInt(month)){
                    //点击下个月重新查找
                    Locator nextMonth = page.locator("button.ui-datepicker-next");
                    // 确保元素可点击，然后点击
                    nextMonth.click();
                }
                System.out.println(curMonthValue);
                // 将年月日字符串转换为 LocalDate 对象
                LocalDate candidateDate = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));

                // 检查候选日期是否大于等于参考日期
                if (candidateDate.isAfter(referenceDate)) {
                    System.out.println("Found a date greater than or equal to 10 days after today: " + candidateDate);
                }
            }*/

            /*// 获取月份
            // 获取当前显示的月份和年份
            String year = page.querySelector(".ui-datepicker-year").textContent();
            System.out.println("年份"+year) ;
            String month = page.querySelector(".ui-datepicker-month").textContent();
            System.out.println("月份"+month) ;
            // 获取所有可点击的日期元素
            List<ElementHandle> elementHandles = page.querySelectorAll(".high_availability[data-handler='selectDay'] a");
            // 假设我们要找到最近的可选择日期
            if (!elementHandles.isEmpty()) {
                for (ElementHandle elementHandle : elementHandles) {

                }
                ElementHandle elementHandle = elementHandles.get(0);
                String nearestDate = elementHandle.textContent();
                System.out.println("最近的可选择日期是: " + nearestDate);
                elementHandle.click();
            } else {
                System.out.println("没有可选择的日期.");
            }*/

            //3.选择票数
            // 等待<select>元素可见
            Locator selectElement = page.locator(".jq-basket-item-quantity");
            selectElement.waitFor();

            // 点击<select>打开选项列表
            selectElement.click();
            // 选择值为"1"的<option>
            page.locator("option[value='1']").click();
            System.out.println("选择订单");

            // 4.点击确认订单
            Locator finalizeButton = page.locator("a.button-next.jq-a-disable.jq-basket-submit.jq-basket-submit-do");
            finalizeButton.click();

            // 5.同意协议
            Locator checkbox = page.locator("input[name='cgvConfirm']");
            checkbox.waitFor();

            // 检查复选框是否已经被选中
            if (!checkbox.isChecked()) {
                // 如果复选框未被选中，则点击它
                checkbox.click();
            }

            // 6.点击确认订单
            Locator checkoutConfirmButton = page.locator("#jq-checkout-confirm");
            checkoutConfirmButton.click();
            // 在这里执行针对每个浏览器实例的其他自动化操作...
            w();
            browser.close();
        }
    }


    private static void click(Page page, String xpath) {
        // 等待元素可见并可点击
        page.waitForSelector(xpath, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
        Locator element = page.locator(xpath);

        // 将元素滚动到视图中心
        element.scrollIntoViewIfNeeded();

        // 等待一段时间以确保滚动完成
        page.waitForTimeout(500);

        try {
            // 尝试正常点击
            element.click();
        } catch (PlaywrightException e) {
            // 如果点击失败，记录异常处理
            System.out.println("点击异常，尝试备选方案");
            // 可以继续添加其他备选点击方法
        }
        // 额外的等待时间，以确保点击后的操作有时间执行
        page.waitForTimeout(1000);
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
