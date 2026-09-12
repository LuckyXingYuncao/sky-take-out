package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.exception.AddressBookBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    public void save(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        if (addressBook.getIsDefault() == null) {
            addressBook.setIsDefault(0);
        }

        if (addressBook.getIsDefault() == 1) {
            addressBookMapper.clearDefaultByUserId(userId);
        }

        addressBookMapper.insert(addressBook);
        log.info("新增地址：{}", addressBook);
    }

    @Override
    public List<AddressBook> list() {
        Long userId = BaseContext.getCurrentId();
        List<AddressBook> list = addressBookMapper.listByUserId(userId);
        log.info("查询地址列表，用户ID={}，共{}条", userId, list.size());
        return list;
    }

    @Override
    public AddressBook getDefault() {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = addressBookMapper.getDefaultByUserId(userId);
        log.info("查询默认地址，用户ID={}：{}", userId, addressBook);
        return addressBook;
    }

    @Override
    public void update(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBookMapper.update(addressBook);
        log.info("修改地址：{}", addressBook);
    }

    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
        log.info("删除地址，ID={}", id);
    }

    @Override
    public AddressBook getById(Long id) {
        AddressBook addressBook = addressBookMapper.getById(id);
        if (addressBook == null) {
            throw new AddressBookBusinessException("地址不存在");
        }
        log.info("查询地址，ID={}：{}", id, addressBook);
        return addressBook;
    }

    @Override
    @Transactional
    public void setDefault(Long id) {
        AddressBook addressBook = addressBookMapper.getById(id);
        if (addressBook == null) {
            throw new AddressBookBusinessException("地址不存在");
        }

        Long userId = BaseContext.getCurrentId();
        addressBookMapper.clearDefaultByUserId(userId);
        addressBookMapper.setDefaultById(id);
        log.info("设置默认地址，ID={}", id);
    }
}