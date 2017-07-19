import org.springframework.boot.actuate.health.DataSourceHealthIndicator
import org.springframework.boot.actuate.health.DiskSpaceHealthIndicatorProperties
import org.springframework.boot.actuate.health.MongoHealthIndicator

// Place your Spring DSL code here
beans = {

    // ** spring boot actuator health check indicators ** //

    databaseHealthCheck(DataSourceHealthIndicator, dataSource)

    diskSpaceHealthIndicatorProperties(DiskSpaceHealthIndicatorProperties) {
        threshold = 250 * 1024 * 1024
    }

    mongoHealthIndicator(MongoHealthIndicator)


}
