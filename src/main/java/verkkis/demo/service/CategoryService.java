package verkkis.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import verkkis.demo.dto.CategoryRequest;
import verkkis.demo.dto.CategoryResponse;
import verkkis.demo.exception.BadRequestException;
import verkkis.demo.exception.ConflictException;
import verkkis.demo.exception.NotFoundException;
import verkkis.demo.exception.Validation;
import verkkis.demo.model.ProductCategory;
import verkkis.demo.repository.ProductCategoryRepository;
import verkkis.demo.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(ProductCategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryResponse> list() {
        return categoryRepository.findAllWithProductCounts();
    }

    public CategoryResponse get(Integer id) {
        return list().stream()
                .filter(c -> c.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Category", id));
    }

    public void requireExists(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category", id);
        }
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        ProductCategory category = new ProductCategory();
        apply(category, req);
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new ConflictException("Category '" + category.getName() + "' already exists");
        }
        category = categoryRepository.save(category);
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription(), 0L, 0L);
    }

    @Transactional
    public CategoryResponse update(Integer id, CategoryRequest req) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category", id));
        apply(category, req);
        categoryRepository.flush();
        return get(id);
    }

    @Transactional
    public void delete(Integer id) {
        requireExists(id);
        long count = productRepository.countByCategory_Id(id);
        if (count > 0) {
            throw new ConflictException("Category " + id + " still has " + count + " products");
        }
        categoryRepository.deleteById(id);
    }

    private void apply(ProductCategory category, CategoryRequest req) {
        if (req == null) {
            throw new BadRequestException("Request body is required");
        }
        category.setName(Validation.requireText(req.name(), "name", 100));
        category.setDescription(Validation.optionalText(req.description(), "description", 65535));
    }
}
