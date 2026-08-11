#include "utils/Crypto.h"
#include <random>
#include <sstream>
#include <iomanip>
#include <cstring>

// Standard SHA-256 constants
static const uint32_t K[64] = {
    0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
    0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
    0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
    0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
    0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
    0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
    0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
    0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
};

#define ROTR(x, n) (((x) >> (n)) | ((x) << (32 - (n))))
#define CH(x, y, z) (((x) & (y)) ^ (~(x) & (z)))
#define MAJ(x, y, z) (((x) & (y)) ^ ((x) & (z)) ^ ((y) & (z)))
#define EP0(x) (ROTR(x, 2) ^ ROTR(x, 13) ^ ROTR(x, 22))
#define EP1(x) (ROTR(x, 6) ^ ROTR(x, 11) ^ ROTR(x, 25))
#define SIG0(x) (ROTR(x, 7) ^ ROTR(x, 18) ^ ((x) >> 3))
#define SIG1(x) (ROTR(x, 17) ^ ROTR(x, 19) ^ ((x) >> 10))

void Crypto::sha256Transform(const uint8_t data[64], uint32_t state[8]) {
    uint32_t m[64];
    for (int i = 0; i < 16; ++i) {
        m[i] = (data[i * 4] << 24) | (data[i * 4 + 1] << 16) | (data[i * 4 + 2] << 8) | (data[i * 4 + 3]);
    }
    for (int i = 16; i < 64; ++i) {
        m[i] = SIG1(m[i - 2]) + m[i - 7] + SIG0(m[i - 15]) + m[i - 16];
    }

    uint32_t a = state[0], b = state[1], c = state[2], d = state[3];
    uint32_t e = state[4], f = state[5], g = state[6], h = state[7];

    for (int i = 0; i < 64; ++i) {
        uint32_t t1 = h + EP1(e) + CH(e, f, g) + K[i] + m[i];
        uint32_t t2 = EP0(a) + MAJ(a, b, c);
        h = g; g = f; f = e; e = d + t1;
        d = c; c = b; b = a; a = t1 + t2;
    }

    state[0] += a; state[1] += b; state[2] += c; state[3] += d;
    state[4] += e; state[5] += f; state[6] += g; state[7] += h;
}

void Crypto::sha256(const uint8_t* data, size_t len, uint8_t hash[32]) {
    uint32_t state[8] = {
        0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
        0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
    };

    uint8_t buffer[64];
    size_t i = 0;

    for (i = 0; i + 64 <= len; i += 64) {
        sha256Transform(data + i, state);
    }

    size_t rem = len - i;
    std::memcpy(buffer, data + i, rem);
    buffer[rem] = 0x80;
    if (rem + 1 > 56) {
        std::memset(buffer + rem + 1, 0, 64 - rem - 1);
        sha256Transform(buffer, state);
        std::memset(buffer, 0, 56);
    } else {
        std::memset(buffer + rem + 1, 0, 56 - rem - 1);
    }

    uint64_t bits = len * 8;
    for (int j = 0; j < 8; ++j) {
        buffer[56 + j] = static_cast<uint8_t>(bits >> (56 - j * 8));
    }
    sha256Transform(buffer, state);

    for (int j = 0; j < 8; ++j) {
        hash[j * 4]     = static_cast<uint8_t>(state[j] >> 24);
        hash[j * 4 + 1] = static_cast<uint8_t>(state[j] >> 16);
        hash[j * 4 + 2] = static_cast<uint8_t>(state[j] >> 8);
        hash[j * 4 + 3] = static_cast<uint8_t>(state[j]);
    }
}

void Crypto::hmacSha256(const uint8_t* key, size_t keyLen, const uint8_t* msg, size_t msgLen, uint8_t out[32]) {
    uint8_t k[64];
    std::memset(k, 0, 64);
    if (keyLen > 64) {
        sha256(key, keyLen, k);
    } else {
        std::memcpy(k, key, keyLen);
    }

    uint8_t iKey[64];
    uint8_t oKey[64];
    for (int i = 0; i < 64; ++i) {
        iKey[i] = k[i] ^ 0x36;
        oKey[i] = k[i] ^ 0x5c;
    }

    std::vector<uint8_t> inner(64 + msgLen);
    std::memcpy(inner.data(), iKey, 64);
    if (msgLen > 0) std::memcpy(inner.data() + 64, msg, msgLen);

    uint8_t innerHash[32];
    sha256(inner.data(), inner.size(), innerHash);

    uint8_t outer[64 + 32];
    std::memcpy(outer, oKey, 64);
    std::memcpy(outer + 64, innerHash, 32);

    sha256(outer, 96, out);
}

void Crypto::pbkdf2HmacSha256(const std::string& pass, const std::string& salt, int iterations, uint8_t outKey[32]) {
    std::vector<uint8_t> saltWithBlock(salt.size() + 4);
    std::memcpy(saltWithBlock.data(), salt.data(), salt.size());
    saltWithBlock[salt.size()]     = 0;
    saltWithBlock[salt.size() + 1] = 0;
    saltWithBlock[salt.size() + 2] = 0;
    saltWithBlock[salt.size() + 3] = 1; // Block 1

    uint8_t u[32];
    hmacSha256(reinterpret_cast<const uint8_t*>(pass.data()), pass.size(),
               saltWithBlock.data(), saltWithBlock.size(), u);

    std::memcpy(outKey, u, 32);

    for (int i = 1; i < iterations; ++i) {
        hmacSha256(reinterpret_cast<const uint8_t*>(pass.data()), pass.size(), u, 32, u);
        for (int j = 0; j < 32; ++j) {
            outKey[j] ^= u[j];
        }
    }
}

std::string Crypto::bytesToHex(const uint8_t* data, size_t len) {
    std::stringstream ss;
    for (size_t i = 0; i < len; ++i) {
        ss << std::hex << std::setw(2) << std::setfill('0') << static_cast<int>(data[i]);
    }
    return ss.str();
}

std::string Crypto::generateSalt(size_t length) {
    static const char chars[] = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(0, sizeof(chars) - 2);

    std::string salt;
    salt.reserve(length);
    for (size_t i = 0; i < length; ++i) {
        salt += chars[dis(gen)];
    }
    return salt;
}

std::string Crypto::sha256Hex(const std::string& input) {
    uint8_t hash[32];
    sha256(reinterpret_cast<const uint8_t*>(input.data()), input.size(), hash);
    return bytesToHex(hash, 32);
}

std::string Crypto::hashPassword(const std::string& password, const std::string& salt, int iterations) {
    uint8_t derivedKey[32];
    pbkdf2HmacSha256(password, salt, iterations, derivedKey);
    return bytesToHex(derivedKey, 32);
}

bool Crypto::verifyPassword(const std::string& password, const std::string& salt, const std::string& storedHash) {
    std::string computedHash = hashPassword(password, salt);
    return (computedHash == storedHash);
}
