package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.repository.IRepository;
import com.campus.secondhand.dto.AddressSaveDTO;
import com.campus.secondhand.dto.AddressUpdateDTO;
import com.campus.secondhand.pojo.Address;
import com.campus.secondhand.vo.AddressListVO;
import com.campus.secondhand.vo.AddressVO;

/**
* @author violet
* @description 针对表【address(收货地址)】的数据库操作Service
* @createDate 2026-09-16 11:11:33
*/
public interface AddressService extends IRepository<Address> {

    /** 查：当前用户的地址列表 + 选中地址 id */
    AddressListVO listAddresses(Long userId, Long selectedId);

    /** 增：新增地址（首条自动设为默认） */
    AddressVO createAddress(Long userId, AddressSaveDTO saveDTO);

    /** 改：修改地址（局部更新，含「设为默认」） */
    AddressVO updateAddress(Long userId, Long id, AddressUpdateDTO updateDTO);

    /** 删：删除地址（删除默认地址后自动补位） */
    void deleteAddress(Long userId, Long id);

}
