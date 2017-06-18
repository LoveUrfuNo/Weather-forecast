package com.testlinenergo.dao;

import com.testlinenergo.model.MeteoStationData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by kosty on 18.06.2017.
 */
public interface MeteoDataDao extends JpaRepository<MeteoStationData, Long> {
}
