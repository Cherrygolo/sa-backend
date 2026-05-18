package ld.feeltrack_backend.projection;

import java.time.LocalDate;

import ld.feeltrack_backend.enums.ReviewType;

/**
 * Projection used for review time-series analytics.
 *
 * Groups reviews by creation date (day) and type
 * (POSITIVE, NEUTRAL, NEGATIVE) in order to build
 * time-based dashboard charts.
 *
 * This projection is used to retrieve aggregated data
 * without loading the full Review entity, improving
 * performance and reducing memory usage.
 */

public interface ReviewTimelineProjection {
    LocalDate getCreatedDate();

    ReviewType getType();

    Long getCount();
}
