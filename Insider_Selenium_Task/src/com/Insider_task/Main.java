package com.Insider_task;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.Assert;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) throws InterruptedException {

		/*
		 * Required for Verification Gmail Info email: taskselenium@gmail.com pass:
		 * insiderTask1
		 */
		//------------- change exe path -------------// 
		System.setProperty("webdriver.gecko.driver", "D:\\SeleniumDrivers\\geckodriver-v0.22.0-win64\\geckodriver.exe");
		WebDriver driver = new FirefoxDriver();

		init(driver, "https://www.amazon.com", "Amazon");
		amazonSignIn(driver, "taskselenium@gmail.com", "insiderTask");
		amazonSearch(driver, "Samsung");
		findCorrectPage(driver, 2);
		String itemTitle = findCorrectItem(driver, 3);
		addToList(driver);
		driver.get(getListLinkWithMouseOver(driver, 5));
		boolean isContain = checkCorrectItemInWishList(driver, itemTitle);
		String addedItemId = findItemId(driver, itemTitle, isContain);
		deleteAddedItem(driver, addedItemId);
		checkCorrectItemDeleted(driver, addedItemId, itemTitle);
		quitBrowser(driver);

	}

	public static void init(WebDriver driver, String address, String key) {
		driver.manage().window().maximize();
		driver.get(address);
		try {
			Assert.assertTrue(driver.getTitle().contains(key));
			System.out.println("Web site title is true");
		} catch (Error e) {
			System.out.println("Web site title is false");
		}
	}

	public static void quitBrowser(WebDriver driver) throws InterruptedException {
		Thread.sleep(4000);
		driver.quit();
	}

	// region SignIn
	private static void amazonSignIn(WebDriver driver, String email, String pass) throws InterruptedException {
		driver.findElement(By.id("nav-link-accountList")).click();

		inputFiller(driver, "ap_email", email);
		inputFiller(driver, "ap_password", pass);

	}

	private static void inputFiller(WebDriver driver, String id, String keys) throws InterruptedException {
		WebDriverWait wait = new WebDriverWait(driver, 10);
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));

		element.sendKeys(keys);
		element.submit();
	}

	//

	// region SearchInput
	private static void amazonSearch(WebDriver driver, String searchText) throws InterruptedException {
		inputFiller(driver, "twotabsearchtextbox", searchText);
		searchControl(driver, searchText);
	}

	// ------------- this process is unstable. -------------//
	private static void searchControl(WebDriver driver, String searchText) throws InterruptedException {
		Thread.sleep(3000); // for get correct title
		try {
			Assert.assertTrue(driver.getTitle().contains("Amazon.com: " + searchText));
			System.out.println("Phase 4: You searched for " + searchText + "!");
		} catch (Error e) {
			System.out.println("Phase 4: Something went wrong!");
		}
	}

	//

	// region Page Process
	private static void findCorrectPage(WebDriver driver, int page) {
		for (int i = 0; i < page - 1; i++) {
			WebDriverWait wait = new WebDriverWait(driver, 10);
			WebElement nextPage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pagnNextLink")));
			driver.get(nextPage.getAttribute("href"));
		}

		int currentPage = Integer.parseInt(driver.findElement(By.className("pagnCur")).getText());
		try {
			Assert.assertEquals(page, currentPage);
			System.out.println("Phase 5: You are looking " + currentPage + ". page!");
		} catch (Error e) {
			System.out.println("Phase 5: Something went wrong!");
		}
	}
	//

	// region Find Correct Item
	private static String findCorrectItem(WebDriver driver, int product) {
		WebElement ulElement = driver.findElement(By.id("s-results-list-atf"));
		List<WebElement> results = ulElement.findElements(By.tagName("li"));

		WebElement itemElement = results.get(product - 1);
		WebElement item = itemElement.findElement(By.className("a-fixed-left-grid"));
		String itemDetailUrl = item.findElement(By.tagName("a")).getAttribute("href");
		driver.get(itemDetailUrl);
		return driver.findElement(By.id("productTitle")).getText();
	}
	//

	// region Add Item to List
	private static void addToList(WebDriver driver) {
		driver.findElement(By.id("wishlistButtonStack")).click();
		WebDriverWait wait = new WebDriverWait(driver, 10);
		WebElement continueElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("WLHUC_continue")));
		continueElement.click();
	}
	//

	// region MouseOver process
	private static String getListLinkWithMouseOver(WebDriver driver, int xpathNo) throws InterruptedException {
		String xpath = "//*[@id=\"nav-al-your-account\"]/a[" + xpathNo + "]";
		WebDriverWait wait = new WebDriverWait(driver, 10);
		WebElement accountList = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-link-accountList")));
		Actions act = new Actions(driver);
		scrollToTop(driver);
		act.moveToElement(accountList).build().perform();
		WebElement element = driver.findElement(By.xpath(xpath));
		String linkHref = element.getAttribute("href");

		if (!linkHref.contains("wishlist")) {
			xpathNo--;
			linkHref = getListLinkWithMouseOver(driver, xpathNo);
		}
		return linkHref;
	}

	// https://www.amazon.com/s?k=Samsung&ref=nb_sb_noss
	private static void scrollToTop(WebDriver driver) {
		WebElement header = driver.findElement(By.tagName("header"));

		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView();", header);
	}
	//

	// region Wish List Check
	private static boolean checkCorrectItemInWishList(WebDriver driver, String productTitle) {
		List<String> wishListItemsName = getWishListItems(driver);
		boolean isContain = isListContainsItem(productTitle, wishListItemsName);
		System.out.println(isContain ? "Phase 8: Correct Product added to Wish List!"
				: "Phase 8: No item in list:" + productTitle);

		return isContain;
	}

	private static boolean isListContainsItem(String productTitle, List<String> wishListItemsName) {
		boolean isContain = false;
		for (String item : wishListItemsName) {
			if (item.equals(productTitle))
				isContain = true;
		}

		return isContain;
	}

	private static List<String> getWishListItems(WebDriver driver) {
		WebElement wishListItem = driver.findElement(By.id("g-items"));
		List<WebElement> wishListItems = wishListItem.findElements(By.tagName("li"));
		List<String> wishListItemsName = new ArrayList<String>();
		for (WebElement liElement : wishListItems) {
			String itemId = liElement.getAttribute("data-itemid");
			WebElement itemName = driver.findElement(By.id("itemName_" + itemId));
			wishListItemsName.add(itemName.getAttribute("title"));
		}
		return wishListItemsName;
	}
	//

	// region Find Item for Delete
	private static String findItemId(WebDriver driver, String productTitle, boolean isContain) {
		String addedItemId = new String();
		if (isContain) {

			WebElement wishListItem = driver.findElement(By.id("g-items"));
			List<WebElement> wishListItems = wishListItem.findElements(By.tagName("li"));
			for (WebElement liElement : wishListItems) {
				String itemId = liElement.getAttribute("data-itemid");
				WebElement itemName = driver.findElement(By.id("itemName_" + itemId));
				if (itemName.getAttribute("title").equals(productTitle))
					addedItemId = String.valueOf(itemId);
			}
		}
		return addedItemId;
	}
	//

	// region Delete Item From List
	private static void deleteAddedItem(WebDriver driver, String addedItemId) {
		WebDriverWait wait = new WebDriverWait(driver, 10);
		WebElement itemActionElement = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.id("itemAction_" + addedItemId)));
		WebElement deleteSpan = itemActionElement.findElement(By.name("submit.deleteItem"));
		deleteSpan.click();
	}
	//

	// region Check Item Deleted
	private static void checkCorrectItemDeleted(WebDriver driver, String itemId, String itemTitle) {
		try {
			WebElement itemElement = driver.findElement(By.id("item_" + itemId));
			String deletedTitle = itemElement.findElement(By.tagName("div")).getText();
			Assert.assertTrue(deletedTitle.contains(itemTitle));
			System.out.println("Phase 10: Correct item deleted!");

		} catch (Error e) {
			System.out.println("Phase 10: Something went wrong!");
		}

	}
	//
}
