package com.testlinenergo.dao;

import com.testlinenergo.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by kosty on 19.06.2017.
 */
public interface ReportDao extends JpaRepository<Report, Long> {
}
