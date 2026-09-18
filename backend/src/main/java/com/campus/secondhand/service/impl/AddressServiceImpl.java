package com.campus.secondhand.service.impl;
import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.pojo.Address;
import com.campus.secondhand.service.AddressService;
import com.campus.secondhand.mapper.AddressMapper;
import org.springframework.stereotype.Service;

/**
* @author violet
* @description 针对表【address(收货地址)】的数据库操作Service实现
* @createDate 2026-09-16 11:11:33
*/
@Service
public class AddressServiceImpl extends CrudRepository<AddressMapper, Address>
    implements AddressService{
}




