package com.abisheikmart.service;

import com.abisheikmart.dao.CategoryDao;
import com.abisheikmart.model.Category;

import java.util.List;
import java.util.Optional;

public class CategoryService {

    private final CategoryDao categoryDao;

    public CategoryService() {
        this.categoryDao = new CategoryDao();
    }

    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public List<Category> getAllCategories() {
        return categoryDao.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryDao.findById(id);
    }

    public Optional<Category> getCategoryByName(String name) {
        return categoryDao.findByName(name);
    }
}
