package com.cqie.reggie_take_out.contoller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqie.reggie_take_out.common.R;
import com.cqie.reggie_take_out.entity.Category;
import com.cqie.reggie_take_out.entity.Employee;
import com.cqie.reggie_take_out.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    public CategoryService categoryService;

    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("category:{}", category);
        boolean save = categoryService.save(category);
        if (save)
            return R.success("新增分类成功");
        return R.error("新增分类失败");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Category>> page(int page, int pageSize) {
        log.info("分类管理分页请求：page:{},pageSize:{}", page, pageSize);
       Page pageInfo=new Page(page, pageSize);
       LambdaQueryWrapper<Category> queryWrapper =new LambdaQueryWrapper<>();
       queryWrapper.orderByAsc(Category::getSort);
       categoryService.page(pageInfo,queryWrapper);
        if (pageInfo != null)
            return R.success(pageInfo);
        return R.error("暂无数据信息");
    }

    /**
     * 删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids) {
        log.info("删除ids:{}", ids);
        categoryService.remove(ids);
        return R.success("删除成功");
    }

    /**
     * 修改分类
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("修改分类信息:{}", category);
        boolean update = categoryService.updateById(category);
        if (update) {
            return R.success("修改成功");
        }
        return R.error("修改失败");
    }

    /**
     * 菜品管理中添加菜品时查询菜品分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        LambdaQueryWrapper<Category> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType()!=null,Category::getType, category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
