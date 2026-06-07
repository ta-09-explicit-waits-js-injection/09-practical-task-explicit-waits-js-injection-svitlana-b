package com.softserve.academy;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
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
    void openRegistrationForm() throws InterruptedException {
        // 1. Open the main page
        driver.navigate().to("https://www.greencity.cx.ua/#/greenCity");

        // Bad practice: using a delay to allow the page to load completely.
        // This is necessary because the site may load slowly.
        Thread.sleep(5000);

        // 2. Click the "Sign Up" button to open the modal window
        driver.findElement(By.cssSelector(".header_sign-up-btn > span")).click();

        // Bad practice: using a delay to allow the modal window to open.
        Thread.sleep(2000);
    }

    // --- TESTS ---

    @Test
    @DisplayName("Invalid email format (without @) → email error")
    void shouldShowErrorForInvalidEmail() throws InterruptedException {
        // One test = one reason for failure. Other fields must be valid.
        typeEmail("invalid-email");
        typeUsername("ValidUsername");
        typePassword("ValidPass123!");
        typeConfirm("ValidPass123!");

        // Give the system some time to validate and display the error
        Thread.sleep(1000);

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

    @ParameterizedTest(name = "Invalid email test using MethodSource [{index}]: email = ''{0}''")
    @MethodSource("invalidEmail")
    void shouldShowErrorForInvalidEmail(String email, boolean singleClick) {
        if (singleClick) {
            driver.findElement(By.id("email")).click();
        } else typeEmail(email);
        typeUsername("user");
        typePassword("ValidPass123!");
        typeConfirm("ValidPass123!");

        assertEmailErrorVisible();
        assertSignUpButtonDisabled();
    }

    @Test
    @DisplayName("All fields empty → required errors shown")
    void shouldShowErrorsForAllEmptyFields() throws InterruptedException {
        // TODO:
        // 1. Click each field or try to click Sign Up
        // 2. Check assertEmailErrorVisible(), assertUsernameErrorVisible(), etc.
    }

    @Test
    @DisplayName("Empty username → username required")
    void shouldShowErrorForEmptyUsername() throws InterruptedException {
        // TODO:
        // 1. Enter valid email and passwords
        // 2. Leave username empty (or click and leave)
        // 3. assertUsernameErrorVisible()
    }

    @Test
    @DisplayName("Confirm password mismatch → confirm error")
    void shouldShowErrorForPasswordMismatch() throws InterruptedException {
        // Enter different passwords in the Password and Confirm Password fields
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

    private void clickSignUp() {
        driver.findElement(By.cssSelector("button[type='submit'].greenStyle")).click();
    }

    public static boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$_\\-!%*#?&])[A-Za-z\\d@$_\\-!%*#?&]{8,}$";
        return password.matches(passwordPattern);
    }

    public static boolean isValidEmail(String email) {
        String emailPattern = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
        return email.matches(emailPattern);
    }

    private void assertEmailErrorVisible() {
        WebElement error = driver.findElement(By.id("email-err-msg"));
        assertTrue(error.isDisplayed(), "Email error message should be visible");
        // contains("required") or other text to avoid dependency on the full phrase
        assertTrue(error.getText().toLowerCase().contains("check")
                || error.getText().toLowerCase().contains("correctly")
                || error.getText().toLowerCase().contains("required")
        );
    }

    private void assertUsernameErrorVisible() {
        // Find the error element for the username (id may differ, check on the site)
        // For example: driver.findElement(By.xpath("//input[@id='firstName']/following-sibling::div"))
    }

    private void assertSignUpButtonDisabled() {
        WebElement btn = driver.findElement(By.cssSelector("button[type='submit'].greenStyle"));
        assertFalse(btn.isEnabled(), "The 'Sign Up' button should be disabled with invalid data");
    }

    private void assertPasswordErrorVisible() {
        WebElement error = driver.findElement(By.className("password-not-valid"));
        assertTrue(error.isDisplayed(), "Password error message should be highlighted");
    }

    private void assertConfirmPasswordErrorVisible() {
        WebElement error = driver.findElement(By.id("confirm-err-msg"));
        assertTrue(error.isDisplayed(), "Confirm password error message should be visible");
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
