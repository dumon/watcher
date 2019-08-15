package com.dumon.watcher.repo;

import com.dumon.watcher.entity.Device;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DeviceRepository extends PagingAndSortingRepository<Device, String> {

}
