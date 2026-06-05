package com.example.auth

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.json.JSONObject

object JwtUtils {
    // Secret key for HMAC-SHA256 signature verification
    private const val SECRET_KEY = "auctronix_live_secure_jwt_secret_key_2026_xyz"

    /**
     * Hashes passwords using standard MessageDigest SHA-256
     */
    fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password // Fallback in case exception is thrown
        }
    }

    /**
     * Generates a standard JWT token string
     */
    fun generateToken(username: String, email: String, isAdmin: Boolean, budget: Double, expirationMs: Long = 3600_000 * 24): String {
        val headerJson = JSONObject().apply {
            put("alg", "HS256")
            put("typ", "JWT")
        }
        
        val exp = System.currentTimeMillis() + expirationMs
        val payloadJson = JSONObject().apply {
            put("sub", username)
            put("email", email)
            put("isAdmin", isAdmin)
            put("budget", budget)
            put("exp", exp)
        }
        
        val encodedHeader = base64UrlEncode(headerJson.toString().toByteArray())
        val encodedPayload = base64UrlEncode(payloadJson.toString().toByteArray())
        
        val dataToSign = "$encodedHeader.$encodedPayload"
        val signatureBytes = hmacSha256(dataToSign, SECRET_KEY)
        val encodedSignature = base64UrlEncode(signatureBytes)
        
        return "$dataToSign.$encodedSignature"
    }

    /**
     * Validates and parses structural and signature integrity of a JWT token
     */
    fun parseAndValidateToken(token: String): TokenClaims? {
        val parts = token.split(".")
        if (parts.size != 3) return null
        
        val header = parts[0]
        val payload = parts[1]
        val signature = parts[2]
        
        // Check signature integrity
        val dataToSign = "$header.$payload"
        val expectedSignature = base64UrlEncode(hmacSha256(dataToSign, SECRET_KEY))
        if (signature != expectedSignature) return null
        
        return try {
            val decodedPayload = String(base64UrlDecode(payload), Charsets.UTF_8)
            val json = JSONObject(decodedPayload)
            val username = json.getString("sub")
            val email = json.getString("email")
            val isAdmin = json.getBoolean("isAdmin")
            val budget = json.getDouble("budget")
            val exp = json.getLong("exp")
            
            // Check expiry
            if (System.currentTimeMillis() > exp) {
                null // Expired token
            } else {
                TokenClaims(username, email, isAdmin, budget, exp)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun base64UrlEncode(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE).trim()
    }

    private fun base64UrlDecode(str: String): ByteArray {
        return Base64.decode(str, Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE)
    }

    private fun hmacSha256(data: String, key: String): ByteArray {
        val sha256HMAC = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
        sha256HMAC.init(secretKey)
        return sha256HMAC.doFinal(data.toByteArray(Charsets.UTF_8))
    }
}

data class TokenClaims(
    val username: String,
    val email: String,
    val isAdmin: Boolean,
    val budget: Double,
    val expiration: Long
)
