package com.secondhand.user.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.geo.GeocodingService;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.user.entity.Address;
import com.secondhand.user.repository.AddressRepository;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Updates address fields and keeps at most one default address per user.
 */
@Service
public class AddressEditService {

    @Resource
    private AddressRepository addressRepository;

    @Resource
    private GeocodingService geocodingService;

    /**
     * Applies partial updates to an address after validating ownership and status.
     */
    @Transactional
    public ApiResponse<Void> editAddress(Long currentUserId,
                                         Long id,
                                         String state,
                                         String city,
                                         String address,
                                         Integer zipcode,
                                         Boolean isDefault,
                                         String phone) {

        Address addr = addressRepository.findById(id).orElse(null);
        AssertUtil.isTrue(addr != null, "地址不存在");

        AssertUtil.isTrue(addr.getStatus() == Status.ACTIVE, "地址已删除");
        AssertUtil.isTrue(currentUserId.equals(addr.getUserId()), "无权限修改该地址");

        if (Boolean.TRUE.equals(isDefault)) {
            List<Address> defaults =
                    addressRepository.findByUserIdAndStatusAndIsDefault(currentUserId, Status.ACTIVE, true);

            long now = System.currentTimeMillis() / 1000;
            for (Address a : defaults) {
                if (!a.getId().equals(id)) {
                    a.setIsDefault(false);
                    a.setUpdateAt(now);
                }
            }
            if (!defaults.isEmpty()) {
                addressRepository.saveAll(defaults);
            }

            addr.setIsDefault(true);
        } else if (isDefault != null) {
            addr.setIsDefault(false);
        }

        boolean locationChanged = state != null || city != null || address != null || zipcode != null;
        if (state != null) addr.setState(state);
        if (city != null) addr.setCity(city);
        if (address != null) addr.setAddress(address);
        if (zipcode != null) addr.setZipcode(zipcode);
        if (phone != null) addr.setPhone(phone);

        // Re-geocode when any location field changed so the stored coordinates stay
        // in sync with the (possibly edited) address. Best-effort; nulls on a miss.
        if (locationChanged) {
            GeocodingService.LatLng ll = geocodingService.geocode(
                    addr.getState(), addr.getCity(), addr.getAddress(),
                    addr.getZipcode() == null ? null : String.valueOf(addr.getZipcode()));
            // Only overwrite on a successful geocode, so a transient Nominatim failure
            // (429/timeout) doesn't wipe previously-good coordinates.
            if (ll != null) {
                addr.setLatitude(ll.getLat());
                addr.setLongitude(ll.getLng());
            }
        }

        addr.setUpdateAt(System.currentTimeMillis() / 1000);

        addressRepository.save(addr);
        return ApiResponse.success();
    }
}
