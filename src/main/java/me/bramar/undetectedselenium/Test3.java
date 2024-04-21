package me.bramar.undetectedselenium;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.util.RandomUtil;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriverLogLevel;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

class Test3 {
    private static final String testUrl = "https://nowsecure.nl";

    public static void main(String[] args) throws IOException, ReflectiveOperationException, URISyntaxException {
        test2();
    }

    public static void w() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press [ENTER] to quit");
        scanner.nextLine();
        scanner.close();
        System.out.println("Quitting");
    }

    public static void test1() throws IOException, ReflectiveOperationException {
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        String chromeBinaryPath = "E:\\124Chrome\\Chrome\\Application\\chrome.exe";
        chromeOptions.setBinary(chromeBinaryPath);
        UndetectedChromeDriver driver = UndetectedChromeDriver.builder()
                .options(chromeOptions)
                .userDataDir("E:\\chromeData\\" + RandomUtil.getRandom().nextLong()).build();
        driver.cloudflareGet(testUrl);
        w();
        driver.quit();
    }

    public static void test2() throws URISyntaxException {
        ChromeOptions chromeOptions = new ChromeOptions();
        String chromeBinaryPath = "E:\\124Chrome\\Chrome\\Application\\chrome.exe";
        String userDataDirPath = "E:\\chromeData\\" + RandomUtil.getRandom().nextLong();
        chromeOptions.setBinary(chromeBinaryPath);
        chromeOptions.addArguments("--user-data-dir=" + userDataDirPath);

        URL myFingerprintChromeUrl = Test2.class.getClassLoader().getResource("my-fingerprint-chrome-1.2.1");
        if (myFingerprintChromeUrl == null) {
            System.err.println("Cannot find 'my-fingerprint-chrome-1.2.1' extension directory in resources.");
            return;
        }
        String canvasBlockerExtensionDirectoryPath = Paths.get(myFingerprintChromeUrl.toURI()).toFile().getAbsolutePath();
        chromeOptions.addArguments("--load-extension=" + canvasBlockerExtensionDirectoryPath);

        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        chromeOptions.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        chromeOptions.addArguments("--disable-blink-features");
        chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
        chromeOptions.setExperimentalOption("useAutomationExtension", false);
        chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
        chromeOptions.setExperimentalOption("useAutomationExtension", false);
        chromeOptions.setExperimentalOption("prefs", prefs);

        chromeOptions.addArguments("--proxy-server=http://" + "43.152.114.120:19610");

        //chromeOptions.merge(capabilities);

        WebDriverManager.chromedriver().setup();
        ChromeDriver driver = new ChromeDriver(chromeOptions);

        SeleniumStealthOptions.getDefault().apply(driver);

        // 去除seleium全部指纹特征
        URL stealthMinUrl = Test2.class.getClassLoader().getResource("stealth.min.js");
        String stealthMinUrlDirectoryPath = Paths.get(stealthMinUrl.toURI()).toFile().getAbsolutePath();
        FileReader fileReader = new FileReader(stealthMinUrlDirectoryPath);
        String js = fileReader.readString();
        // MapBuilder是依赖hutool工具包的api
        Map<String, Object> commandMap = MapBuilder.create(new LinkedHashMap<String, Object>()).put("source", js)
                .build();
        // executeCdpCommand这个api在selenium3中是没有的,请使用selenium4才能使用此api
        ((ChromeDriver) driver).executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", commandMap);
        driver.get(testUrl);

        // 在这里执行针对每个浏览器实例的其他自动化操作...
        w();
        // 关闭当前浏览器实例
        driver.quit();


        //driver.quit();
    }

    public static void test3() throws IOException, ReflectiveOperationException {
        UndetectedChromeDriver driver = UndetectedChromeDriver.builder()
                .pageLoadStrategy(PageLoadStrategy.NONE)
                .headless(false)
                .driverFromCFT(true)
                .versionMain(115)
                .autoOpenDevtools(true)
                .serviceBuilder(new ChromeDriverService.Builder().withSilent(true).withLogLevel(ChromiumDriverLogLevel.OFF))
                .seleniumStealth(SeleniumStealthOptions.getDefault()).build();
        System.out.println("Bypassed: " + driver.cloudflareGet(testUrl));
        w();
        driver.quit();
    }

    public static void cloudflareTest() throws IOException, ReflectiveOperationException, InterruptedException {
        int success = 0;
        int fail = 0;
        int attempts = 100;
        boolean headless = false;
        for (int i = 0; i < attempts; i++) {
            UndetectedChromeDriver driver = UndetectedChromeDriver.builder()
                    .pageLoadStrategy(PageLoadStrategy.NONE)
                    .headless(headless)
                    .driverFromCFT(true)
                    .versionMain(115)
                    .autoOpenDevtools(true)
                    .seleniumStealth(SeleniumStealthOptions.getDefault()).build();
            if (driver.cloudflareGet(testUrl)) success++;
            else fail++;
            Thread.sleep(2391);
            driver.quit();
            System.out.println((headless ? "Headless" : "Headful") + " Success: " + success + " Fail: " + fail + " | Success Rate: " + (success / attempts * 100d) + "% Attempts = " + (i + 1) + "/" + attempts + " (" + ((i + 1) / attempts * 100d) + "%)");
        }
        System.out.println((headless ? "Headless" : "Headful") + " Success: " + success + " Fail: " + fail + " | Success Rate: " + (success / attempts * 100d) + "%");
    }
}
