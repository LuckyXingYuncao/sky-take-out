package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 新增分类
     * @param category
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into category (type, name, sort, status, create_time, update_time, create_user, update_user) " +
            "values (#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Category category);

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    List<Category> pageQuery(@Param("dto") CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 更新分类信息
     * @param category
     */
    @AutoFill(OperationType.UPDATE)
    void update(Category category);

    /**
     * 根据ID查询分类
     * @param id
     * @return
     */
    @Select("select * from category where id = #{id}")
    Category getById(Long id);

    /**
     * 根据名称查询分类
     * @param name
     * @return
     */
    @Select("select * from category where name = #{name}")
    Category getByName(String name);

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @Select("select * from category where type = #{type} order by sort asc")
    List<Category> getByType(Integer type);

    /**
     * 根据ID删除分类
     * @param id
     */
    @Select("delete from category where id = #{id}")
    void deleteById(Long id);
}