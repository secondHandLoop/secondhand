package com.secondhand.user.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.geo.GeocodingService;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.user.entity.Address;
import com.secondhand.user.repository.AddressRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Creates address records and maintains the user's default-address invariant.
 */
@Service
public class AddressAddService {

    @Resource
    private AddressRepository addressRepository;

    @Resource
    private GeocodingService geocodingService;

    /**
     * Adds an address for the current user and optionally promotes it to default.
     *
     * <p>Annotated {@code @Transactional} so clearing the previous default and
     * inserting the new default address commit atomically; a failure on the
     * insert must not leave the user with zero default addresses.
     */
    @Transactional
    public ApiResponse<Void> addAddress(Long currentUserId, String username, String state, String city,
                                        String address, Integer zipcode, Boolean isDefault, String phone) {

        AssertUtil.isTrue(currentUserId != null, "用户未登录");

        boolean hasAny = addressRepository.existsByUserIdAndStatus(currentUserId, Status.ACTIVE);

        boolean makeDefault;
        if (!hasAny) {
            makeDefault = true;
        } else {
            makeDefault = Boolean.TRUE.equals(isDefault);
        }

        if (makeDefault) {
            List<Address> defaults =
                    addressRepository.findByUserIdAndStatusAndIsDefault(currentUserId, Status.ACTIVE, true);

            long now = System.currentTimeMillis() / 1000;
            for (Address a : defaults) {
                a.setIsDefault(false);
                a.setUpdateAt(now);
            }
            if (!defaults.isEmpty()) {
                addressRepository.saveAll(defaults);
            }
        }

        Address addr = new Address();
        addr.setUserId(currentUserId);
        addr.setUsername(username);
        addr.setState(state);
        addr.setCity(city);
        addr.setAddress(address);
        addr.setZipcode(zipcode);
        addr.setIsDefault(makeDefault);
        addr.setPhone(phone);

        // Geocode so distance-from-this-address can be computed later. Best-effort:
        // a null result (Nominatim miss/timeout) just leaves the coordinates unset.
        GeocodingService.LatLng ll = geocodingService.geocode(
                state, city, address, zipcode == null ? null : String.valueOf(zipcode));
        if (ll != null) {
            addr.setLatitude(ll.getLat());
            addr.setLongitude(ll.getLng());
        }

        addressRepository.save(addr);
        return ApiResponse.success();
    }
}
