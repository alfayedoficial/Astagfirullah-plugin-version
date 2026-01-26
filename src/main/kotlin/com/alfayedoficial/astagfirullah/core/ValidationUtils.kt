package com.alfayedoficial.astagfirullah.core

import com.alfayedoficial.astagfirullah.data.model.ValidationResult
import java.awt.Color

/**
 * Utility object for form validation.
 * Provides validation methods for email, password, and name fields.
 */
object ValidationUtils {

    /**
     * Validates an email address.
     *
     * @param email The email to validate
     * @return ValidationResult indicating validity
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = Constants.EMAIL_REGEX.toRegex()
        return emailRegex.matches(email)
    }

    /**
     * Gets the error message for an invalid email.
     *
     * @param email The email to validate
     * @return Error message or null if valid
     */
    fun getEmailError(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !isValidEmail(email) -> "Invalid email format"
            else -> null
        }
    }

    /**
     * Validates email and returns ValidationResult.
     */
    fun validateEmail(email: String): ValidationResult {
        val error = getEmailError(email)
        return ValidationResult(error == null, error)
    }

    /**
     * Validates a password meets minimum requirements.
     * Only requires minimum length of 8 characters.
     *
     * @param password The password to validate
     * @return true if password is valid
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= Constants.MIN_PASSWORD_LENGTH
    }

    /**
     * Gets the error message for an invalid password.
     * Only requires minimum length of 8 characters.
     *
     * @param password The password to validate
     * @return Error message or null if valid
     */
    fun getPasswordError(password: String): String? {
        return when {
            password.isEmpty() -> "Password is required"
            password.length < Constants.MIN_PASSWORD_LENGTH ->
                "Password must be at least ${Constants.MIN_PASSWORD_LENGTH} characters"
            else -> null
        }
    }

    /**
     * Validates password and returns ValidationResult.
     */
    fun validatePassword(password: String): ValidationResult {
        val error = getPasswordError(password)
        return ValidationResult(error == null, error)
    }

    /**
     * Calculates password strength.
     *
     * @param password The password to evaluate
     * @return PasswordStrength level
     */
    fun getPasswordStrength(password: String): PasswordStrength {
        if (password.length < Constants.MIN_PASSWORD_LENGTH) {
            return PasswordStrength.WEAK
        }

        var score = 0

        // Length bonus
        if (password.length >= 10) score++
        if (password.length >= 12) score++
        if (password.length >= 16) score++

        // Character variety
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score += 2

        return when {
            score >= 6 -> PasswordStrength.STRONG
            score >= 4 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }

    /**
     * Validates a user name.
     *
     * @param name The name to validate
     * @return true if name is valid
     */
    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.length <= Constants.MAX_NAME_LENGTH
    }

    /**
     * Gets the error message for an invalid name.
     *
     * @param name The name to validate
     * @return Error message or null if valid
     */
    fun getNameError(name: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            name.length > Constants.MAX_NAME_LENGTH ->
                "Name must be less than ${Constants.MAX_NAME_LENGTH} characters"
            else -> null
        }
    }

    /**
     * Validates name and returns ValidationResult.
     */
    fun validateName(name: String): ValidationResult {
        val error = getNameError(name)
        return ValidationResult(error == null, error)
    }

    /**
     * Checks if passwords match.
     *
     * @param password The password
     * @param confirmPassword The confirmation password
     * @return true if they match
     */
    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    /**
     * Gets password match error message.
     */
    fun getPasswordMatchError(password: String, confirmPassword: String): String? {
        return if (doPasswordsMatch(password, confirmPassword)) null else "Passwords do not match"
    }

    /**
     * Validates password confirmation and returns ValidationResult.
     */
    fun validatePasswordMatch(password: String, confirmPassword: String): ValidationResult {
        val error = getPasswordMatchError(password, confirmPassword)
        return ValidationResult(error == null, error)
    }
}

/**
 * Password strength levels with associated colors.
 */
enum class PasswordStrength(
    val displayName: String,
    val level: Int,
    val color: Color,
    val progressValue: Int
) {
    WEAK("Weak", 1, Color(244, 67, 54), 33),      // Red
    MEDIUM("Medium", 2, Color(255, 152, 0), 66),  // Orange
    STRONG("Strong", 3, Color(76, 175, 80), 100)  // Green
}