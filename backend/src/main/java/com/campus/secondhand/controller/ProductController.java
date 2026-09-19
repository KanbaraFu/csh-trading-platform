package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.common.BizException;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.ProductQueryDTO;
import com.campus.secondhand.dto.ProductSaveDTO;
import com.campus.secondhand.service.ProductService;
import com.campus.secondhand.vo.ProductDetailVO;
import com.campus.secondhand.vo.ProductVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products
     */
    @GetMapping("/products")
    public Result<PageResult<ProductVO>> list(ProductQueryDTO queryDTO) {
        IPage<ProductVO> page = productService.getProductPage(queryDTO);
        PageResult<ProductVO> pageResult = new PageResult<>(
                page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()
        );
        return Result.success(pageResult);
    }

    /**
     * GET /api/products/{id}
     */
    @GetMapping("/products/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        productService.incrViewCount(id);
        ProductDetailVO detail = productService.getProductDetail(id);
        return Result.success(detail);
    }

    /**
     * POST /api/products
     */
    @PostMapping("/products")
    public Result<Long> publish(@RequestBody @Valid ProductSaveDTO saveDTO,
                                HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Long productId = productService.publishProduct(userId, saveDTO);
        return Result.success(productId);
    }

    /**
     * PUT /api/products/{id}
     */
    @PutMapping("/products/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid ProductSaveDTO saveDTO,
                               HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        productService.updateProduct(userId, id, saveDTO);
        return Result.success(null);
    }

    /**
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/products/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        productService.deleteProduct(userId, id);
        return Result.success(null);
    }

    /**
     * POST /api/products/{id}/relist  重新上架
     */
    @PostMapping("/products/{id}/relist")
    public Result<Void> relist(@PathVariable Long id,
                               HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        productService.updateStatus(userId, id, 1);
        return Result.success(null);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("currentUserId");
        if (userIdAttr == null) {
            throw BizException.unauthorized("请先登录");
        }
        return Long.valueOf(userIdAttr.toString());
    }
}
