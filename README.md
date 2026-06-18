# Selenium WebDriver Practical Task: Negative Registration Tests

## Objective
The goal of this task is to implement a set of negative test cases for the registration form on the [GreenCity](https://www.greencity.cx.ua/#/greenCity) website. You will practice identifying validation errors, using CSS selectors, and structuring your test code using helper methods.

## Task Description
You are provided with a test class `GreenCityNegativeRegistrationTest.java` which contains:
1.  **Setup and Teardown**: Code to initialize the WebDriver and navigate to the registration form.
2.  **One Working Example**: `shouldShowErrorForInvalidEmail` which demonstrates how to fill the form and verify a validation error.
3.  **Placeholders (TODOs)**: Five additional test methods that you need to implement.
4.  **Helper Methods**: A set of methods like `typeEmail()`, `typePassword()`, etc., to make your tests more readable.

### Your Assignment
Implement the following test scenarios in `GreenCityNegativeRegistrationTest.java`:
1.  **All fields empty**: Verify that required error messages appear for all mandatory fields when they are left empty.
2.  **Empty username**: Fill in valid email and passwords, but leave the username field empty and verify the error.
3.  **Short password (<8 characters)**: Enter a password with fewer than 8 characters and verify the validation rule error.
4.  **Password with space**: Enter a password containing a space and verify it is rejected.
5.  **Confirm password mismatch**: Enter different values in "Password" and "Confirm Password" fields and verify the mismatch error.

## Key Principles Applied

### 1. Test Design Techniques
*   **Negative Testing**: We specifically provide invalid or unexpected data to verify that the system handles it correctly and shows appropriate error messages.
*   **Equivalence Partitioning**: Instead of testing every possible invalid email, we use one representative (e.g., "invalid-email" without `@`) to test the format validation logic.
*   **Boundary Value Analysis**: Used for the password length test. Testing with 7 characters (just below the minimum of 8) is a classic boundary test.
*   **Error Guessing**: Testing for spaces in passwords or empty fields is based on common user mistakes and typical system vulnerabilities.

### 2. "One Test = One Reason for Failure"
Each test case focuses on a **single** validation rule. To ensure we are testing exactly what we intend, all other fields in the form must be filled with **valid** data. This way, if the test fails, we know exactly which validation is broken.

### 3. Structural Best Practices (Pre-Page Object)
*   **Helper Methods**: Instead of using `driver.findElement(...).sendKeys(...)` directly in every test, we use methods like `typeEmail(value)`. This makes the tests easier to read and maintain.
*   **Decoupled Assertions**: We check for the *presence* and *visibility* of error messages. For stability, we use `contains()` instead of checking the exact full text of the error, as text might change slightly over time.
*   **Wait Strategy**: We use `Thread.sleep()` as a temporary measure for beginners. **Note**: This is considered a *bad practice* in professional automation. In future tasks, you will learn how to use *Explicit Waits* for better performance and stability.

## How to Run
1.  Ensure you have Chrome browser installed.
2.  Open the project in your IDE (IntelliJ IDEA recommended).
3.  Right-click on `GreenCityNegativeRegistrationTest.java` and select **Run**.
4.  Observe the results in the Test Runner window.
