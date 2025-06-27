package in.assignment;

import com.microsoft.playwright.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.cucumber.java.en.*;

import java.io.FileWriter;
import java.util.*;

public class STest {
    Playwright playwright;
    Browser browser;
    Page page;
    Page applePage;
    List<Map<String, String>> list;

    @Given("I open the Casekaro website")
    public void openCasekaro() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
        page.navigate("https://casekaro.com/");
        page.waitForTimeout(5000);
        assert page.url().contains("casekaro.com");
    }

    @When("I navigate to Mobile Covers and search for Apple")
    public void navigateToApple() {
        page.locator("#HeaderMenu-mobile-covers").click();
        page.waitForTimeout(2000);
        applePage = page.waitForPopup(() -> {
            page.locator("#search-bar-cover-page").fill("Apple");
            page.waitForTimeout(2000);
            page.locator("a:has-text('Apple')").click();
        });
        applePage.waitForLoadState();
        assert applePage.url().contains("iphone-covers-cases");
    }

    @When("I search and open the iPhone 16 Pro page")
    public void openIphone16Pro() {
        applePage.locator("#search-bar-cover-page").fill("iPhone 16 Pro");
        applePage.waitForTimeout(2000);
        applePage.getByText("iPhone 16 Pro", new Page.GetByTextOptions().setExact(true)).click();
        applePage.waitForTimeout(2000);
        assert applePage.url().toLowerCase().contains("iphone-16-pro-back-covers");
    }

    @When("I apply the In Stock filter")
    public void applyFilter() {
        applePage.locator(".facets__summary-label:has-text('Availability')").click();
        applePage.waitForTimeout(2000);
        applePage.locator("label[for='Filter-filter.v.availability-1']").click();
        applePage.waitForTimeout(2000);
        assert applePage.locator("label[for='Filter-filter.v.availability-1'] input").isChecked();
    }

    @Then("I scrape the product data and save it as JSON")
    public void scrapeAndSave() throws Exception {
        applePage.waitForSelector("li.grid__item");
        Locator products = applePage.locator("li.grid__item");
        int count = products.count();
        assert count > 0;

        list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Locator product = products.nth(i);
            String desc = product.locator("h3.card__heading a").first().innerText();

            String priceAfter = product.locator(".price-item--sale").count() > 0
                    ? product.locator(".price-item--sale").innerText()
                    : product.locator(".price-item--regular").innerText();

            String actual = product.locator(".price__sale s").count() > 0
                    ? product.locator(".price__sale s").innerText()
                    : "";

            String img = product.locator("img").getAttribute("src");

            Map<String, String> item = new LinkedHashMap<>();
            item.put("description", desc);
            item.put("price_after_discount", priceAfter);
            item.put("actual_price", actual);
            item.put("image_link", "https:" + img);
            list.add(item);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter fw = new FileWriter("products.json");
        gson.toJson(list, fw);
        fw.close();

        System.out.println("Done. " + list.size() + " items saved to products.json");
    }

    @Then("I sort the products by price and print them")
    public void sortAndPrint() {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - i - 1; j++) {
                String s1 = list.get(j).get("price_after_discount").split("₹ ")[1].trim();
                String s2 = list.get(j + 1).get("price_after_discount").split("₹ ")[1].trim();
                int price1 = (int) Float.parseFloat(s1);
                int price2 = (int) Float.parseFloat(s2);
                if (price1 > price2) {
                    Map<String, String> temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }

        for (Map<String, String> item : list) {
            System.out.println("Description: " + item.get("description"));
            System.out.println("Price After Discount: " + item.get("price_after_discount"));
            System.out.println("Actual Price: " + item.get("actual_price"));
            System.out.println("Image Link: " + item.get("image_link"));
            System.out.println("----------------------------------------");
        }

        browser.close();
        System.out.println("Done. " + list.size() + " products saved & printed.");
    }
}
