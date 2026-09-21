package com.campus.secondhand.controller;

import com.campus.secondhand.common.BizException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.AddressSaveDTO;
import com.campus.secondhand.dto.AddressUpdateDTO;
import com.campus.secondhand.service.AddressService;
import com.campus.secondhand.utils.TokenUtil;
import com.campus.secondhand.vo.AddressListVO;
import com.campus.secondhand.vo.AddressVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 个人中心 - 收货地址：增删改查。
 */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 当前用户地址列表
     */
    @GetMapping
    public Result<AddressListVO> list(@RequestHeader(value = Constants.TOKEN_HEADER, required = false) String token,
                                      @RequestParam(value = "addressId", required = false) Long addressId) {
        return Result.success(addressService.listAddresses(currentUserId(token), addressId));
    }

    /**
     * 新增地址
     */
    @PostMapping
    public Result<AddressVO> create(@RequestHeader(value = Constants.TOKEN_HEADER, required = false) String token,
                                    @RequestBody @Validated AddressSaveDTO saveDTO) {
        return Result.success(addressService.createAddress(currentUserId(token), saveDTO));
    }

    /**
     * 修改地址
     */
    @PutMapping("/{id}")
    public Result<AddressVO> update(@RequestHeader(value = Constants.TOKEN_HEADER, required = false) String token,
                                    @PathVariable Long id,
                                    @RequestBody @Validated AddressUpdateDTO updateDTO) {
        return Result.success(addressService.updateAddress(currentUserId(token), id, updateDTO));
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestHeader(value = Constants.TOKEN_HEADER, required = false) String token,
                               @PathVariable Long id) {
        addressService.deleteAddress(currentUserId(token), id);
        return Result.success();
    }

    /**
     * 从请求头token获取当前登录用户 ID
     */
    private Long currentUserId(String token) {
        Long userId = tokenUtil.getUserId(token);
        if (userId == null) {
            throw BizException.unauthorized("登录已过期，请重新登录");
        }
        return userId;
    }

}
