#ifndef DATABASE_H
#define DATABASE_H

#include <string>
#include <vector>
#include "sqlite/sqlite3.h"

class Database {
public:
    static Database& getInstance();

    bool init(const std::string& dbPath = "ecommerce.db");
    void close();

    sqlite3* getDbHandle() const;

    bool execute(const std::string& sql);
    bool executeParam(const std::string& sql, const std::vector<std::string>& params);

    ~Database();

private:
    Database();
    Database(const Database&) = delete;
    Database& operator=(const Database&) = delete;

    bool createTables();
    bool seedDefaultAdmin();
    bool seedDefaultCatalog();

    sqlite3* db;
    bool initialized;
};

#endif // DATABASE_H
