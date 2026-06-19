package com.softserve.academy;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import org.openqa.selenium.chrome.ChromeOptions;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenCityNegativeRegistrationTest {
    private static WebDriver driver;

    @BeforeAll
    static void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Check if we are running in CI (GitHub Actions)
        if (System.getenv("GITHUB_ACTIONS") != null) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
        }

        driver = WebDriverManager.chromedriver().capabilities(options).create();
        driver.manage().window().maximize();
        // At this stage, we are not using complex waits, so we just maximize the window
    }

    @BeforeEach
    void openRegistrationForm() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        // 1. Open the main page
        driver.navigate().to("https://www.greencity.cx.ua/#/greenCity");

        // 2. Click the "Sign Up" button to open the modal window
        WebElement signUpButton = wait.until(ExpectedConditions
                .elementToBeClickable(By.cssSelector(".header_sign-up-btn > span")));
        signUpButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
    }

    // --- TESTS ---

    @Test
    @DisplayName("Invalid email format (without @) → email error")
    void shouldShowErrorForInvalidEmail() {
        // One test = one reason for failure. Other fields must be valid.
        typeEmail("invalid-email");
        typeUsername("ValidUsername");
        typePassword("ValidPass123!");
        typeConfirm("ValidPass123!");

        // Check that the error for email appeared
        assertEmailErrorVisible();
        // Check that the registration button is disabled (or registration did not occur)
        assertSignUpButtonDisabled();
    }

    @ParameterizedTest(name = "Invalid email test using ValueSource [{index}]: email = ''{0}''")
    @ValueSource(strings = {"plainaddress", "#@%^%#$@#$@#.com", "@gmail.com", "Joe Smith <email@gmail.com>", "email123.gmail.com", "email123@gmail@com"})
    void shouldShowErrorForInvalidEmail(String email) {
        typeEmail(email);
        typeUsername("user");
        typePassword("ValidPass123!");
        typeConfirm("ValidPass123!");

        assertFalse(isValidEmail(email));
        assertEmailErrorVisible();
        assertSignUpButtonDisabled();
    }

    private static Stream<Arguments> invalidEmail() {
        return Stream.of(
                Arguments.of("", true),
                Arguments.of("plainaddress", false),
                Arguments.of("#@%^%#$@#$@#.com", false),
                Arguments.of("@gmail.com", false),
                Arguments.of("Joe Smith <email@gmail.com>", false),
                Arguments.of("email123.gmail.com", false),
                Arguments.of("email123@gmail@com", false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @CsvFileSource(resources = "/successfulRegistration.csv", numLinesToSkip = 1, delimiter = ',')
    @DisplayName("Successful registration by use of csv file")
    void successfulRegistrationFromCsvFile(String scenario, String username, String password, String repeatPassword) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        fillRegistrationForm(generateUniqueEmail(), username, password, repeatPassword);
        clickSignUpButton();
        WebElement snackbar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".mdc-snackbar__label")));

        assertTrue(snackbar.isDisplayed(), "Snackbar should be displayed after successful registration");
    }

    @ParameterizedTest(name = "{0}")
    @CsvFileSource(resources = "/invalidEmailNegativeRegistration.csv", numLinesToSkip = 1, delimiter = ',')
    @DisplayName("Negative registration with invalid email using csv file")
    void negativeRegistrationFromCsvFile(String scenario, String email, String username, String password, String repeatPassword) {
        fillRegistrationForm(email, username, password, repeatPassword);

        assertEmailErrorVisible();
        assertSignUpButtonDisabled();
        assertIncorrectEmailErrorVisible();
    }

    @ParameterizedTest(name = "{0}")
    @CsvFileSource(resources = "/invalidUserNameRegistration.csv", numLinesToSkip = 1, delimiter = ',')
    @DisplayName("Registration with invalid userName using csv file")
    void registrationWithInvalidUsername(String scenario, String username, String password, String repeatPassword) {
        fillRegistrationForm(generateUniqueEmail(), username, password, repeatPassword);

        assertSignUpButtonDisabled();
        assertUsernameErrorVisible();
    }

    @ParameterizedTest(name = "Invalid email test using MethodSource [{index}]: email = ''{0}''")
    @MethodSource("invalidEmail")
    void shouldShowErrorForInvalidEmail(String email, boolean singleClick) {
        if (singleClick) {
            driver.findElement(By.id("email")).click();
        } else typeEmail(email);
        typeUsername("user");
        typePassword("ValidPass123!");
        typeConfirm("ValidPass123!");

        assertFalse(isValidEmail(email));
        assertEmailErrorVisible();
        assertSignUpButtonDisabled();
    }

    @ParameterizedTest(name = "Invalid password test [{index}]: password = ''{0}''")
    @EmptySource
    @ValueSource(strings = {"Pa123!@", "Valid Pass123!", " ", "12345678"})
    void shouldShowErrorForInvalidPassword(String password) {

        typePassword(password);
        typeConfirm(password);
        typeEmail("some3456@gmail.com");
        typeUsername("user name");

        assertFalse(isValidPassword(password));
        assertPasswordErrorVisible();
        assertSignUpButtonDisabled();
    }

    @ParameterizedTest(name = "{0}")
    @CsvFileSource(resources = "/invalidConfirmPasswordRegistration.csv")
    @DisplayName("Registration with incorrect Confirm Password")
    void registrationWithIncorrectConfirmPassword(String scenario, String username, String password, String repeatPassword){
        fillRegistrationForm(generateUniqueEmail(), username, password, repeatPassword);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.activeElement.blur();");
        assertConfirmPasswordErrorVisible();
        assertSignUpButtonDisabled();
    }

    // --- HELPERS (Helper methods) ---
    // This is the first step towards structuring code before learning Page Object

    private void typeEmail(String value) {
        WebElement field = driver.findElement(By.id("email"));
        field.clear();
        field.sendKeys(value);
    }

    private void typeUsername(String value) {
        WebElement field = driver.findElement(By.id("firstName"));
        field.clear();
        field.sendKeys(value);
    }

    private void typePassword(String value) {
        WebElement field = driver.findElement(By.id("password"));
        field.clear();
        field.sendKeys(value);
    }

    private void typeConfirm(String value) {
        WebElement field = driver.findElement(By.id("repeatPassword"));
        field.clear();
        field.sendKeys(value);
    }

    private void clickSignUpButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'].greenStyle")));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", btn);
    }

    private void fillRegistrationForm(String email, String username, String password, String repeatPassword) {
        typeEmail(email);
        typeUsername(username);
        typePassword(password);
        typeConfirm(repeatPassword);
    }

    public static boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$_\\-!%*#?&])[A-Za-z\\d@$_\\-!%*#?&]{8,}$";
        return password.matches(passwordPattern);
    }

    public static boolean isValidEmail(String email) {
        String emailPattern = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
        return email.matches(emailPattern);
    }

    private static String generateUniqueEmail() {
        String chars = "qwertyuiopasdfghjklzxcvbnm0123456789";
        StringBuilder email = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            email.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        email.append("@gmail.com");
        return email.toString();
    }

    private void assertEmailErrorVisible() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email-err-msg")));
        assertTrue(error.isDisplayed(), "Email error message should be visible");
        // contains("required") or other text to avoid dependency on the full phrase
        assertTrue(error.getText().toLowerCase().contains("check")
                || error.getText().toLowerCase().contains("correctly")
                || error.getText().toLowerCase().contains("required")
        );
    }

    private void assertIncorrectEmailErrorVisible() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email-err-msg")));
        assertTrue(error.isDisplayed(), "Incorrect email error message should be visible");
        assertTrue(error.getText().toLowerCase().contains("please check that your e-mail address is indicated correctly"));
    }

    private void assertUsernameErrorVisible() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstname-err-msg")));
        assertTrue(error.isDisplayed(), "Username error message should be visible");
        assertTrue(error.getText().toLowerCase().contains("the user name must be 1-30 characters long and may include letters, numbers, single dots, hyphens, or apostrophes. dots cannot appear at the beginning or end, nor can they be consecutive. double hyphens or apostrophes are also not allowed."));
    }

    private void assertSignUpButtonDisabled() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement btn = driver.findElement(By.cssSelector("button[type='submit'].greenStyle"));
        wait.until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(btn)));
        assertFalse(btn.isEnabled(), "The 'Sign Up' button should be disabled with invalid data");
    }

    private void assertPasswordErrorVisible() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("password-not-valid")));
        assertTrue(error.isDisplayed(), "Password error message should be highlighted");
    }

    private void assertConfirmPasswordErrorVisible() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("confirm-err-msg")));
        assertTrue(error.isDisplayed(), "Confirm password error message should be visible");
        assertTrue(error.getText().toLowerCase().contains("passwords do not match"));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
