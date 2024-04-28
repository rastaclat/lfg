package me.bramar.task.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Month;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
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
    private static final String testUrl = "https://www.ticketlouvre.fr/louvre/b2c/index.cfm/change.language/lang/zh-CHS/?ref=https%3A%2F%2Fwww.ticketlouvre.fr%2Flouvre%2Fb2c%2Findex.cfm%2Fcalendar%2FeventCode%2FMusWeb";
    private static final String ipUrl = "http://api.tq.roxlabs.cn/getProxyIp?num=6&return_type=txt&lb=1&sb=&flow=1&regions=us&protocol=http";
    private static ConcurrentHashMap<Long, Browser> activeBrowsers = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException, ReflectiveOperationException, URISyntaxException {
        LocalDateTime dt = Convert.toLocalDateTime("2024-04");
        System.out.println(dt);

        // test1();
    }
    private static final Map<String, Integer> monthTextToNumberMap;

    static {
        monthTextToNumberMap = new HashMap<>();
        monthTextToNumberMap.put("一月", 1);
        monthTextToNumberMap.put("二月", 2);
        monthTextToNumberMap.put("三月", 3);
        monthTextToNumberMap.put("四月", 4);
        monthTextToNumberMap.put("五月", 5);
        monthTextToNumberMap.put("六月", 6);
        monthTextToNumberMap.put("七月", 7);
        monthTextToNumberMap.put("八月", 8);
        monthTextToNumberMap.put("九月", 9);
        monthTextToNumberMap.put("十月", 10);
        monthTextToNumberMap.put("十一月", 11);
        monthTextToNumberMap.put("十二月", 12);
    }

    // 将月份文本转换为数字的方法
    private static int monthTextToNumber(String monthText) {
        return monthTextToNumberMap.getOrDefault(monthText, 0);
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
                //page.setViewportSize(1920,1080);
                // 尝试导航，最多重试三次
                boolean success = false;
                for (int i = 0; i < 3 && !success; i++) {
                    try {
                        page.addInitScript(js);
                        log.info("打开网页");
                        page.navigate(testUrl);
                        page.setDefaultNavigationTimeout(60000);
                        page.waitForLoadState(LoadState.LOAD);
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
                page.setDefaultTimeout(90000);
                log.info("网页加载完成");

                //获取可选日期
                // 等待元素出现
                // 获取当前月份和10天后的日期
                Date currentDate = new Date();
                int currentMonth = DateUtil.month(currentDate) + 1;
                DateTime offTime = DateUtil.offsetDay(currentDate, 10);
                int offsetDayOfMonth = offTime.dayOfMonth();
                int offsetMonth = offTime.month() + 1; // Hutool的month()是从0开始的，所以+1

                boolean isCheck = false;
                for (int i = 0; i < 3 && !isCheck; i++) { // 加入 !isCheck 判断，如果已经找到可选日期则跳出循环

                    List<ElementHandle> elementHandles = page.locator(".high_availability").elementHandles();
                    if (CollectionUtil.isNotEmpty(elementHandles)) {
                        for (ElementHandle elementHandle : elementHandles) {
                            int day = Integer.parseInt(elementHandle.textContent());
                            int month = Convert.toInt(elementHandle.getAttribute("data-month")) + 1; // data-month 是从0开始的，需要加1

                            // 确保是在当前月或者下个月，并且日期符合预期
                            if (month <= offsetMonth && day >= offsetDayOfMonth) {
                                elementHandle.click(); // 直接点击 elementHandle
                                log.info("选择日期成功");
                                isCheck = true;
                                break;
                            }
                        }
                    }

                    // 如果没有找到并且不在最后一次循环，尝试点击下个月
                    if (!isCheck && i < 2) {
                        // 检查当前显示的月份是否小于10天后的月份，如果是，则点击下个月
                        String displayedMonthText = page.locator(".ui-datepicker-month").textContent()+1;
                        String displayedYearText = page.locator(".ui-datepicker-year").textContent();
                        int displayedMonth = monthTextToNumber(displayedMonthText);
                        int displayedYear = Integer.parseInt(displayedYearText);

                        if (displayedYear < offTime.year() || (displayedYear == offTime.year() && displayedMonth < offsetMonth)) {
                            clickNextAndGetSelectElements(page);
                        } else {
                            log.info("已经是或超过了目标月份，不再点击下个月");
                            break;
                        }
                    }
                }


                page.waitForTimeout(RandomUtil.randomInt(1000, 3000));
                // 使用XPath定位select元素
               /* Locator locator = page.locator("text=全价门票 >> select[name='quantity']");
                log.info("找到全价门票");
                // 使用值选择选项
                locator.selectOption(new String[]{"1"});*/
                Locator selectLocator = page.locator("text=全价门票 >> ../.. >> select[name='quantity']");
                // 选择值为 "1" 的选项
                selectLocator.selectOption(new String[]{"1"});

                log.info("选择全价门票1份");
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("确认")).click();

                log.info("点击确认");
                page.waitForTimeout(RandomUtil.randomInt(1000, 2000));
                //page.getByLabel("通过捐赠来支持卢浮宫 2,00 €").check();
                log.info("点击通过捐赠来支持卢浮宫");
                page.waitForTimeout(RandomUtil.randomInt(1000, 2000));
                page.locator("input[name=\"cgvConfirm\"]").check();
                log.info("点击接受销售条款");

                //确认订单
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("确认您的订单")).click();
                page.waitForTimeout(RandomUtil.randomInt(1000, 2000));
                //输入邮箱
                page.getByLabel("电子邮件 *").pressSequentially("thatiand20@outlook.com", new Locator.PressSequentiallyOptions().setDelay(120));
                page.waitForTimeout(RandomUtil.randomInt(1000, 2000));
                //输入密码
                page.getByLabel("密码 *").pressSequentially("Suiyan2021@", new Locator.PressSequentiallyOptions().setDelay(120));
                page.waitForTimeout(RandomUtil.randomInt(1000, 2000));
                //登录
                page.locator("#jq-user-form-submit").click();
                page.waitForTimeout(RandomUtil.randomInt(1000, 2000));

                //输入姓
                page.getByLabel("姓 *", new Page.GetByLabelOptions().setExact(true)).pressSequentially(creditCardInfo.getFirstName(), new Locator.PressSequentiallyOptions().setDelay(100));
                page.waitForTimeout(RandomUtil.randomInt(1000, 4000));

                //输入名
                page.getByLabel("名 *").pressSequentially(creditCardInfo.getLastName(), new Locator.PressSequentiallyOptions().setDelay(120));
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //点击支付
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("支付")).click();
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //美国visa
                Locator carteVisa = page.getByRole(AriaRole.ROW, new Page.GetByRoleOptions().setName("Carte Visa").setExact(true)).getByRole(AriaRole.RADIO);
                carteVisa.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                carteVisa.check();

                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //点击确定
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Make payment >>")).click();
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //输入卡号
                page.locator("#NUMERO_CARTE").pressSequentially(creditCardInfo.getCardNumber(), new Locator.PressSequentiallyOptions().setDelay(120));
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //选择日期
                page.locator("#MOIS_VALIDITE").selectOption(creditCardInfo.getMonthNum());
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                page.locator("#AN_VALIDITE").selectOption(creditCardInfo.getYearNum());
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //输入cvv
                page.locator("#CVVX").pressSequentially(creditCardInfo.getSecurityCode(), new Locator.PressSequentiallyOptions().setDelay(120));
                page.waitForTimeout(RandomUtil.randomInt(2000, 4000));
                //确定
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("VALIDER")).click();
                page.waitForTimeout(RandomUtil.randomInt(2000, 3000));
                //判断是否出现卡号错误
               /* Locator cardError = page.getByText("Warning: Unknown card number");
                if (cardError != null) {
                    log.error("不支持的卡号,关闭浏览器");
                    browser.close();
                }*/
                browser.close();
            } catch (Exception e) {
                log.error("发生错误", e);
            } finally {
                // 从映射中移除并关闭浏览器
                closeAndRemoveBrowser(Thread.currentThread().getId());
                playwright.close();
            }
        } catch (URISyntaxException e) {
            log.error("创建驱动错误", e);
            throw new RuntimeException(e);
        }
    }

    private static void  clickNextAndGetSelectElements(Page page) {
        log.info("尝试点击下个月");
        // 从页面中获取当前显示的月份和年份
        String displayedMonthText = page.locator(".ui-datepicker-month").textContent().trim();
        String displayedYearText = page.locator(".ui-datepicker-year").textContent().trim();
        int displayedMonth = monthTextToNumber(displayedMonthText);
        int displayedYear = Integer.parseInt(displayedYearText);

        int nextMonth = DateUtil.nextMonth().month() + 1; // hutool的month()从0开始计数，所以+1
        int currentYear = DateUtil.year(new Date());

        // 如果当前显示的年份低于当前年份，或者当前显示的年份相同但月份小于下一个月，则点击下个月
        if (displayedYear < currentYear || (displayedYear == currentYear && displayedMonth < nextMonth)) {
            // 如果下个月按钮可见，则点击
            Locator nextButton = page.locator("a[title='Next']");
            if (nextButton.isVisible()) {
                nextButton.click();
                log.info("点击下个月并等待日历更新");
                page.waitForSelector(".ui-datepicker-calendar", new Page.WaitForSelectorOptions().setTimeout(30000));
                page.waitForTimeout(2000); // 使用固定的等待时间
                page.waitForLoadState(LoadState.NETWORKIDLE); // 等待网络空闲表示页面已经加载完毕
            }
        } else {
            log.info("已经是或超过了目标月份，不再点击下个月");
        }
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
