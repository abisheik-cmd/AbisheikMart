#ifndef CRYPTO_H
#define CRYPTO_H

#include <string>
#include <vector>
#include <cstdint>

class Crypto {
public:
    // Generate a random hex salt string
    static std::string generateSalt(size_t length = 16);

    // Compute PBKDF2-HMAC-SHA256 password hash
    static std::string hashPassword(const std::string& password, const std::string& salt, int iterations = 10000);

    // Verify a password against a salt and stored PBKDF2 hash
    static bool verifyPassword(const std::string& password, const std::string& salt, const std::string& storedHash);

    // SHA-256 helper
    static std::string sha256Hex(const std::string& input);

private:
    static void sha256Transform(const uint8_t data[64], uint32_t state[8]);
    static void sha256(const uint8_t* data, size_t len, uint8_t hash[32]);
    static void hmacSha256(const uint8_t* key, size_t keyLen, const uint8_t* msg, size_t msgLen, uint8_t out[32]);
    static void pbkdf2HmacSha256(const std::string& pass, const std::string& salt, int iterations, uint8_t outKey[32]);
    static std::string bytesToHex(const uint8_t* data, size_t len);
};

#endif // CRYPTO_H
