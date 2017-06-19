package com.testlinenergo.dao;

import com.testlinenergo.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Dao for class {@link com.testlinenergo.model.Report}.
 */
public interface ReportDao extends JpaRepository<Report, Long> {
}
