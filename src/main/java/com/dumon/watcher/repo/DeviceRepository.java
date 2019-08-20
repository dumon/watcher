package com.dumon.watcher.repo;

import com.dumon.watcher.entity.Device;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface DeviceRepository extends PagingAndSortingRepository<Device, Long> {

    List<Device> findDeviceByActiveTrue();

}
