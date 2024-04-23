package me.bramar.task.utils;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.dtflys.forest.Forest;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import lombok.extern.slf4j.Slf4j;
import me.bramar.task.entity.CreditCardInfo;
import me.bramar.task.entity.IpProxyInfo;
import me.bramar.task.entity.common.Ret;
import me.bramar.task.entity.dao.CheckAgentDO;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class LfgUtils {
    private static final String testUrl = "https://www.ticketlouvre.fr/louvre/b2c/index.cfm/calendar/eventCode/MusWeb";
    private static final String ipUrl = "http://api.tq.roxlabs.cn/getProxyIp?num=6&return_type=txt&lb=1&sb=&flow=1&regions=us&protocol=http";

    public static void main(String[] args) throws IOException, ReflectiveOperationException, URISyntaxException {
        LocalDateTime dt = Convert.toLocalDateTime("2024-04");
        System.out.println(dt);

       // test1();
    }

    public static void w() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press [ENTER] to quit");
        scanner.nextLine();
        scanner.close();
        System.out.println("Quitting");
    }

    public static void start(CreditCardInfo creditCardInfo) {
        List<String> ipList = new ArrayList<>();
        if (StrUtil.isNotBlank(ipUrl)) {
            String ipStr = Forest.get(ipUrl).executeAsString();
            if (StrUtil.isNotBlank(ipStr)) {
                ipList.addAll(StrUtil.split(ipStr, "\r\n"));
            }
        }
        try (Playwright playwright = Playwright.create()) {
            URL myFingerprintURL = LfgUtils.class.getClassLoader().getResource("my-fingerprint-chrome-1.2.1");
            if (myFingerprintURL == null) {
                System.err.println("Cannot find 'my-fingerprint-chrome' extension directory in resources.");
                return;
            }
            String myFingerprintPath = Paths.get(myFingerprintURL.toURI()).toFile().getAbsolutePath();

            List<String> defaultParamList = Arrays.asList(
                "--start-maximized",
                "--disable-extensions-except=" + myFingerprintPath,
                "--load-extension=" + myFingerprintPath,
                "--disable-gpu",
                "--hide-extensions", // 添加此行以隐藏扩展插件图标
                "--disable-software-rasterize",
                "--disable-blink-features=AutomationControlled"
            );
            List<String> allParamList = new ArrayList<>(defaultParamList);
            String selectIpProxy = "";
            if (CollectionUtil.isNotEmpty(ipList)) {
                for (String ipAddress : ipList) {
                    String[] ipArray = ipAddress.split(":");
                    if (ipArray.length > 1) {
                        IpProxyInfo ipProxyInfo = new IpProxyInfo();
                        ipProxyInfo.setHost(ipArray[0]);
                        ipProxyInfo.setPort(Convert.toInt(ipArray[1]));
                        Ret<CheckAgentDO> checkAgentDO =
                            ProxyUtils.getCheckAgentDO(ipProxyInfo);
                        if (checkAgentDO.getCode() == 0) {
                            log.info("ip检测成功");
                            selectIpProxy = ipAddress;
                            CheckAgentDO agentDO = checkAgentDO.getData();
                            log.info(JSON.toJSONString(agentDO));
                            break;
                        }
                    }
                }
                //socks://
                if (StrUtil.isNotBlank(selectIpProxy)) {
                    allParamList.add("--proxy-server=http://" + selectIpProxy);
                }else {
                    log.error("获取代理ip失败");
                    return;
                }
            }

            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(allParamList)
                .setChromiumSandbox(false)
            )
                ;
            BrowserContext context = browser.newContext();
            Page page = browser.newPage();
            String js = "Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});";

            try {
                page.addInitScript(js);
                //page.setViewportSize(1920,1080);
                // 尝试导航，最多重试三次
                boolean success = false;
                for (int i = 0; i < 3 && !success; i++) {
                    try {
                        page.navigate(testUrl);
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
                assertThat(page.locator("#elements #product-list div").filter(new Locator.FilterOptions().setHasText("Plein Tarif Musée 0123456 22,")).getByRole(AriaRole.COMBOBOX)).isVisible();
                page.locator("#elements #product-list div").filter(new Locator.FilterOptions().setHasText("Plein Tarif Musée 0123456 22,")).locator("div").nth(1).click();
                page.locator("#elements #product-list div").filter(new Locator.FilterOptions().setHasText("Plein Tarif Musée 0123456 22,")).getByRole(AriaRole.COMBOBOX).selectOption("1");
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
                page.getByLabel("Email *").fill("thatiand20@outlook.com");
                page.waitForTimeout(RandomUtil.randomInt(1000,2000));
                //输入密码
                page.getByLabel("Mot de Passe *").fill("Suiyan2021@");
                page.waitForTimeout(RandomUtil.randomInt(1000,2000));
                //登录
                page.locator("#jq-user-form-submit").click();
                page.waitForTimeout(RandomUtil.randomInt(1000,2000));
                page.pause();
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

    public static void test1() {
        List<String> ipList = new ArrayList<>();
        if (StrUtil.isNotBlank(ipUrl)) {
            String ipStr = Forest.get(ipUrl).executeAsString();
            if (StrUtil.isNotBlank(ipStr)) {
                ipList.addAll(StrUtil.split(ipStr, "\r\n"));
            }
        }

        try (Playwright playwright = Playwright.create()) {
            URL myFingerprintURL = LfgUtils.class.getClassLoader().getResource("my-fingerprint-chrome-1.2.1");
            if (myFingerprintURL == null) {
                System.err.println("Cannot find 'my-fingerprint-chrome' extension directory in resources.");
                return;
            }
            String myFingerprintPath = Paths.get(myFingerprintURL.toURI()).toFile().getAbsolutePath();
            List<String> defaultParamList = Arrays.asList(
                "--start-maximized",
                "--disable-extensions-except=" + myFingerprintPath,
                "--load-extension=" + myFingerprintPath,
                "--disable-gpu",
                "--hide-extensions", // 添加此行以隐藏扩展插件图标
                "--disable-software-rasterize",
                "--disable-blink-features=AutomationControlled"
            );
            List<String> allParamList = new ArrayList<>(defaultParamList);
            if (CollectionUtil.isNotEmpty(ipList)) {
                //socks://
                allParamList.add("--proxy-server=http://" + ipList.get(0));
            }
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setArgs(allParamList)
                            .setChromiumSandbox(false)
                    )
                    ;
            BrowserContext context = browser.newContext();
            Page page = browser.newPage();
            String js = "Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});";

            try {
                page.addInitScript(js);
                //page.setViewportSize(1920,1080);
                // 尝试导航，最多重试三次
                boolean success = false;
                for (int i = 0; i < 3 && !success; i++) {
                    try {
                        page.navigate(testUrl);
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
                page.waitForTimeout(RandomUtil.randomInt(4000,6000));// 最大等待3秒,选择具体的日期
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(Convert.toStr(selectDay)).setExact(true)).click();
                //Locator erreurLocator = page.getByLabel("Erreur générique");
                log.info("选择日期成功");
                page.waitForTimeout(RandomUtil.randomInt(1000,3000));
                assertThat(page.locator("#elements #product-list div").filter(new Locator.FilterOptions().setHasText("Plein Tarif Musée 0123456 22,")).getByRole(AriaRole.COMBOBOX)).isVisible();
                page.locator("#elements #product-list div").filter(new Locator.FilterOptions().setHasText("Plein Tarif Musée 0123456 22,")).locator("div").nth(1).click();
                page.locator("#elements #product-list div").filter(new Locator.FilterOptions().setHasText("Plein Tarif Musée 0123456 22,")).getByRole(AriaRole.COMBOBOX).selectOption("1");
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
                page.getByLabel("Email *").fill("thatiand20@outlook.com");
                page.waitForTimeout(RandomUtil.randomInt(1000,2000));
                //输入密码
                page.getByLabel("Mot de Passe *").fill("Suiyan2021@");
                page.waitForTimeout(RandomUtil.randomInt(1000,2000));
                //登录
                page.locator("#jq-user-form-submit").click();
                page.waitForTimeout(RandomUtil.randomInt(1000,2000));
                //page.pause();
                w();
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
