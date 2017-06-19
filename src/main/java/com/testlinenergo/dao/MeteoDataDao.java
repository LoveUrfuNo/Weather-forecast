package com.testlinenergo.dao;

import com.testlinenergo.model.MeteoStationData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Dao for class {@link com.testlinenergo.model.MeteoStationData}.
 */
public interface MeteoDataDao extends JpaRepository<MeteoStationData, Long> {
}
