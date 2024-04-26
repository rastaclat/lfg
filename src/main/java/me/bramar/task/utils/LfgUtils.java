package me.bramar.task.utils;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
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
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LfgUtils {
    private static final String testUrl = "https://www.ticketlouvre.fr/louvre/b2c/index.cfm/calendar/eventCode/MusWeb";
    private static final String ipUrl = "http://api.tq.roxlabs.cn/getProxyIp?num=6&return_type=txt&lb=1&sb=&flow=1&regions=us&protocol=http";
    private static ConcurrentHashMap<Long, Browser> activeBrowsers = new ConcurrentHashMap<>();

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
      /*  if (StrUtil.isNotBlank(ipUrl)) {
            String ipStr = Forest.get(ipUrl).connectTimeout(5000).executeAsString();
            if (StrUtil.isNotBlank(ipStr)) {
                ipList.addAll(StrUtil.split(ipStr, "\r\n"));
            }
        }*/
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
                String selectIpProxy = "";
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
                } else {
                    log.error("获取代理ip失败");
                    return;
                }
            }

            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(allParamList)
                .setChromiumSandbox(false)
            );
            // 将浏览器实例存入映射
            activeBrowsers.put(Thread.currentThread().getId(), browser);
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
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                page.waitForSelector(".datepicker"); // 确保日期选择器已加载
                log.info("日期选择器加载完成");
                page.waitForSelector(".ui-datepicker-calendar");
                List<ElementHandle> elementHandles = page.querySelectorAll(".ui-datepicker-calendar .high_availability");
                ;
                if (CollectionUtil.isEmpty(elementHandles)) {
                    log.info("未获取到可选日期,点击下个月");
                    elementHandles = clickNextAndGetSelectElements(page);
                }
                if (CollectionUtil.isEmpty(elementHandles)) {
                    log.error("未获取到可选日期");
                    browser.close();
                }

                //获取10天后的月份
                DateTime dateTime = DateUtil.offsetDay(new Date(), 10);
                int offsetMonth = DateUtil.monthEnum(dateTime).getValueBaseOne();
                int offsetDay = dateTime.dayOfMonth();
                //获取当前月份
                int curMonth = DateUtil.monthEnum(new Date()).getValueBaseOne();
                Integer elementMonth = Convert.toInt(elementHandles.get(0).getAttribute("data-month"));
                if (offsetMonth != curMonth && elementMonth != null && elementMonth < offsetMonth) {
                    page.waitForTimeout(RandomUtil.randomInt(4000, 6000));
                    elementHandles = clickNextAndGetSelectElements(page);
                }
                if (CollectionUtil.isEmpty(elementHandles)) {
                    log.info("未获取到可选日期");
                    browser.close();
                }
                int selectDay = offsetDay;
                for (ElementHandle dateHandle : elementHandles) {
                    Integer year = Convert.toInt(dateHandle.getAttribute("data-year"));
                    int day = Integer.parseInt(dateHandle.textContent());
                    if (day == offsetDay || day > offsetDay) {
                        selectDay = day;
                        log.info("选择{}年{}月{}日", year, offsetMonth, day);
                        break;
                    }
                }
                page.waitForTimeout(RandomUtil.randomInt(3000, 5000));// 最大等待3秒,选择具体的日期
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(Convert.toStr(selectDay)).setExact(true)).click();
                log.info("选择日期成功");
                page.waitForTimeout(RandomUtil.randomInt(1000, 3000));
                assertThat(page.locator("#elements #product-list div").filter(new Locator.FilterOptions().setHasText("Plein Tarif Musée 0123456 22,")).getByRole(AriaRole.COMBOBOX)).isVisible();
                page.locator("#elements #product-list div").filter(new Locator.FilterOptions().setHasText("Plein Tarif Musée 0123456 22,")).locator("div").nth(1).click();
                page.locator("#elements #product-list div").filter(new Locator.FilterOptions().setHasText("Plein Tarif Musée 0123456 22,")).getByRole(AriaRole.COMBOBOX).selectOption("1");
                log.info("选择全价门票1份");
                page.waitForTimeout(RandomUtil.randomInt(1000, 2000));
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Finaliser la commande")).click();
                log.info("点击完成订单");
                page.waitForTimeout(RandomUtil.randomInt(1000, 2000));
                page.getByLabel("Je soutiens le Louvre en").check();
                log.info("点击通过捐赠来支持卢浮宫");
                page.waitForTimeout(RandomUtil.randomInt(1000, 2000));
                page.locator("input[name=\"cgvConfirm\"]").check();
                log.info("点击接受销售条款");

                //确认订单
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Confirmer votre commande")).click();
                page.waitForTimeout(RandomUtil.randomInt(1000, 2000));
                //输入邮箱
                page.getByLabel("Email *").fill("thatiand20@outlook.com");
                page.waitForTimeout(RandomUtil.randomInt(1000, 2000));
                //输入密码
                page.getByLabel("Mot de Passe *").fill("Suiyan2021@");
                page.waitForTimeout(RandomUtil.randomInt(1000, 2000));
                //登录
                page.locator("#jq-user-form-submit").click();
                page.waitForTimeout(RandomUtil.randomInt(1000, 2000));
                //输入姓
                page.getByLabel("Nom *", new Page.GetByLabelOptions().setExact(true)).fill(creditCardInfo.getFirstName());
                page.waitForTimeout(RandomUtil.randomInt(1000, 4000));
                //输入名
                page.getByLabel("Prénom *").fill(creditCardInfo.getLastName());
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //点击支付
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Paiement")).click();
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //选择visa
                page.getByRole(AriaRole.ROW, new Page.GetByRoleOptions().setName("Carte Visa").setExact(true)).getByRole(AriaRole.RADIO).check();
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //点击确定
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Effectuer le paiement >>")).click();
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //输入卡号
                page.locator("#NUMERO_CARTE").fill(creditCardInfo.getCardNumber());
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //选择日期
                page.locator("#MOIS_VALIDITE").selectOption(creditCardInfo.getMonthNum());
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                page.locator("#AN_VALIDITE").selectOption(creditCardInfo.getYearNum());
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //输入cvv
                page.locator("#CVVX").fill(creditCardInfo.getSecurityCode());
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //确定
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("VALIDER")).click();
                page.waitForTimeout(RandomUtil.randomInt(2000,3000));
                //判断是否出现卡号错误
                Locator cardError = page.getByText("Attention: Carte inconnue");
                if (cardError != null) {
                    log.error("不支持的卡号,关闭浏览器");
                    browser.close();
                }

                page.pause();
                browser.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                // 从映射中移除并关闭浏览器
                closeAndRemoveBrowser(Thread.currentThread().getId());
                playwright.close();
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<ElementHandle> clickNextAndGetSelectElements(Page page) {
        log.info("点击下个月按钮");
        assertThat(page.getByTitle("Next")).isVisible();
        page.getByTitle("Next").click();
        log.info("等待日历更新");
        page.waitForSelector(".ui-datepicker-calendar", new Page.WaitForSelectorOptions().setTimeout(30000));
        page.waitForTimeout(RandomUtil.randomInt(1000, 5000));
        return page.querySelectorAll(".ui-datepicker-calendar .high_availability");
    }

    // 关闭并移除指定的浏览器实例
    public static void closeAndRemoveBrowser(long threadId) {
        Browser browser = activeBrowsers.remove(threadId);
        if (browser != null) {
            browser.close();
        }
    }

    // 关闭所有活跃的浏览器实例
    public static void closeAllBrowsers() {
        activeBrowsers.forEach((id, browser) -> {
            if (browser != null) {
                browser.close();
            }
        });
        activeBrowsers.clear();
    }

}
