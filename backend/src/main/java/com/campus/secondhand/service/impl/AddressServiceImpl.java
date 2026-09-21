package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.common.BizException;
import com.campus.secondhand.dto.AddressSaveDTO;
import com.campus.secondhand.dto.AddressUpdateDTO;
import com.campus.secondhand.mapper.AddressMapper;
import com.campus.secondhand.pojo.Address;
import com.campus.secondhand.service.AddressService;
import com.campus.secondhand.vo.AddressListVO;
import com.campus.secondhand.vo.AddressVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author violet
* @description 针对表【address(收货地址)】的数据库操作Service实现
* @createDate 2026-09-16 11:11:33
*/
@Service
public class AddressServiceImpl extends CrudRepository<AddressMapper, Address>
    implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    /**
     * 查：按用户查地址列表，默认地址排最前
     */
    @Override
    public AddressListVO listAddresses(Long userId, Long selectedId) {
        List<Address> list = addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .orderByDesc(Address::getIsDefault)
                        .orderByDesc(Address::getCreateTime)
        );
        List<AddressVO> records = list.stream().map(this::toVO).collect(Collectors.toList());
        return new AddressListVO(records, resolveSelectedId(list, selectedId));
    }

    /**
     * 增：新增地址；用户首条地址自动设为默认
     */
    @Override
    public AddressVO createAddress(Long userId, AddressSaveDTO saveDTO) {
        Address address = new Address();
        // Boolean isDefault 与 Integer isDefault 类型不匹配，Spring 会自动跳过，下面手动赋值
        BeanUtils.copyProperties(saveDTO, address);
        address.setUserId(userId);
        address.setCreateTime(new Date());

        Long count = addressMapper.selectCount(
                new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
        boolean asDefault = (count == null || count == 0) || Boolean.TRUE.equals(saveDTO.getIsDefault());
        if (asDefault) {
            clearDefault(userId);
        }
        address.setIsDefault(asDefault ? 1 : 0);

        addressMapper.insert(address);
        return toVO(address);
    }

    /**
     * 改：局部更新（只改传入的字段）；isDefault=true 时同步把其它地址取消默认
     */
    @Override
    public AddressVO updateAddress(Long userId, Long id, AddressUpdateDTO updateDTO) {
        // 归属校验，防止越权修改他人地址
        Address address = requireOwned(userId, id);

        if (updateDTO.getReceiverName() != null) address.setReceiverName(updateDTO.getReceiverName());
        if (updateDTO.getPhone() != null) address.setPhone(updateDTO.getPhone());
        if (updateDTO.getRegion() != null) address.setRegion(updateDTO.getRegion());
        if (updateDTO.getDetail() != null) address.setDetail(updateDTO.getDetail());

        if (Boolean.TRUE.equals(updateDTO.getIsDefault())) {
            clearDefault(userId);
            address.setIsDefault(1);
        } else if (Boolean.FALSE.equals(updateDTO.getIsDefault())) {
            address.setIsDefault(0);
        }

        addressMapper.updateById(address);
        return toVO(address);
    }

    /**
     * 删：删除地址；若删的是默认地址，把剩余最新一条补为默认
     */
    @Override
    public void deleteAddress(Long userId, Long id) {
        Address address = requireOwned(userId, id);
        addressMapper.deleteById(id);

        if (Integer.valueOf(1).equals(address.getIsDefault())) {
            List<Address> rest = addressMapper.selectList(
                    new LambdaQueryWrapper<Address>()
                            .eq(Address::getUserId, userId)
                            .orderByDesc(Address::getCreateTime));
            if (!rest.isEmpty()) {
                Address first = rest.get(0);
                first.setIsDefault(1);
                addressMapper.updateById(first);
            }
        }
    }

    /**
     * 校验地址存在且属于当前用户
     */
    private Address requireOwned(Long userId, Long id) {
        Address address = addressMapper.selectById(id);
        if (address == null || !userId.equals(address.getUserId())) {
            throw new BizException("收货地址不存在或无权操作");
        }
        return address;
    }

    /**
     * 清空该用户所有地址的默认标记
     */
    private void clearDefault(Long userId) {
        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .set(Address::getIsDefault, 0));
    }

    /**
     * 计算选中地址：优先入参，其次默认地址，最后第一条，都没有则 0
     */
    private Long resolveSelectedId(List<Address> list, Long selectedId) {
        if (selectedId != null && list.stream().anyMatch(item -> item.getId().equals(selectedId))) {
            return selectedId;
        }
        return list.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getIsDefault()))
                .map(Address::getId)
                .findFirst()
                .orElseGet(() -> list.isEmpty() ? 0L : list.get(0).getId());
    }

    /**
     * 实体转 VO
     */
    private AddressVO toVO(Address address) {
        AddressVO vo = new AddressVO();
        BeanUtils.copyProperties(address, vo);
        return vo;
    }

}
