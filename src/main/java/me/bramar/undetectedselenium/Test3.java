package me.bramar.undetectedselenium;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

@Slf4j
class Test3 {
    private static final String testUrl = "https://www.ticketlouvre.fr/louvre/b2c/index.cfm/calendar/eventCode/MusWeb";

    public static void main(String[] args) throws IOException, ReflectiveOperationException, URISyntaxException {
        test1();
    }

    public static void w() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press [ENTER] to quit");
        scanner.nextLine();
        scanner.close();
        System.out.println("Quitting");
    }

    public static void test1() {
        try (Playwright playwright = Playwright.create()) {
            URL myFingerprintURL = Test3.class.getClassLoader().getResource("my-fingerprint-chrome-1.2.1");
            if (myFingerprintURL == null) {
                System.err.println("Cannot find 'my-fingerprint-chrome' extension directory in resources.");
                return;
            }
            String myFingerprintPath = Paths.get(myFingerprintURL.toURI()).toFile().getAbsolutePath();

            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setArgs(Arrays.asList(
                            "--start-maximized",
                            "--disable-extensions-except=" + myFingerprintPath,
                            "--load-extension=" + myFingerprintPath,
                            "--disable-gpu",
                            "--hide-extensions", // 添加此行以隐藏扩展插件图标
                            "--disable-software-rasterize",
                            "--disable-blink-features=AutomationControlled"
                    ))
                            .setChromiumSandbox(false)
                    )
                    ;
            BrowserContext context = browser.newContext();
            Page page = browser.newPage();

            String js = """
                    Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});
                    """;
            try {
                page.addInitScript(js);
                page.setViewportSize(1920,1080);
                page.navigate(testUrl);
               /* Locator byLabel = page.getByLabel("Dernière réactualisation");
                if (byLabel != null) {
                    log.info("进入等待页面，延长等待时间");
                    page.setDefaultTimeout(90000); //90秒
                }*/
                page.setDefaultTimeout(60000);
                //获取可选日期
                // 等待元素出现
                page.waitForSelector("td.high_availability");
                List<ElementHandle> elementHandles = page.querySelectorAll("td.high_availability");
                if (CollectionUtil.isEmpty(elementHandles)) {
                    log.error("未获取到可选日期");
                }

                //获取10天后的月份
                DateTime dateTime = DateUtil.offsetDay(new Date(), 10);
                int offsetMonth = DateUtil.monthEnum(dateTime).getValueBaseOne();
                int offsetDay = dateTime.dayOfMonth();
                //获取当前月份
                int curMonth = DateUtil.monthEnum(new Date()).getValueBaseOne();

                if (offsetMonth != curMonth) {
                    page.getByTitle("Next").click();
                }
                page.waitForSelector("td.high_availability");
                List<ElementHandle> dateList = page.querySelectorAll("td.high_availability");
                if (CollectionUtil.isEmpty(dateList)) {
                    log.info("未获取到可选日期");
                    browser.close();
                }
                int selectDay = offsetDay;
                for (ElementHandle dateHandle : dateList) {
                    Integer year = Convert.toInt(dateHandle.getAttribute("data-year"));
                    int day = Integer.parseInt(dateHandle.textContent());
                    if (day == offsetDay || day > offsetDay) {
                        selectDay = day;
                        log.info("选择{}年{}月{}日", year, offsetMonth, day);
                        break;
                    }
                }
                page.waitForTimeout(RandomUtil.randomInt(1000,3000));// 最大等待3秒,选择具体的日期
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(Convert.toStr(selectDay)).setExact(true)).click();
                log.info("选择日期成功");
                page.waitForTimeout(RandomUtil.randomInt(1000,3000));
                //page.pause();
                page.locator("#elements #product-list div").wait();
                page.locator("#elements #product-list div").filter(new Locator.FilterOptions().setHasText("Plein Tarif Musée 01 22,00 €")).getByRole(AriaRole.COMBOBOX).selectOption("1");
                log.info("选择全价门票1份");
                page.waitForTimeout(RandomUtil.randomInt(1000,2000));
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Finaliser la commande")).click();
                log.info("点击完成订单");
                page.waitForTimeout(RandomUtil.randomInt(1000,2000));
                page.getByLabel("Je soutiens le Louvre en").check();
                log.info("点击通过捐赠来支持卢浮宫");
                page.waitForTimeout(RandomUtil.randomInt(1000,2000));
                page.locator("input[name=\"cgvConfirm\"]").check();
                log.info("点击接受销售条款");

                //确认订单
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Confirmer votre commande")).click();
                page.waitForTimeout(RandomUtil.randomInt(1000,2000));
                //输入邮箱
                page.getByLabel("Email *").fill("shigua161@outlook.com");
                //输入密码
                page.getByLabel("Mot de Passe *").fill("Tqasa1213@");

                browser.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    Thread.sleep(1000 * 1000); // 等待1000秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


}
