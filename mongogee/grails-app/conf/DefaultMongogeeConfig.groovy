/**
 * Default Mongogee configuration.
 * The settings below are subject to overriding per each property key from host application.
 */
defaultMongogee {
    /**
     * Change logs class path configuration
     * Choose either string (CSV) or list format to set scan paths.
     */
    changeLogsScanPackage = ''
    // changeLogsScanPackageList = ['a.b.c', 'x.y.z']
    /**
     * Boolean to enable or disable mongogee migration
     */
    changeEnabled = true
    /**
     * If true, the application boot-up will not be stopped by migration error
     */
    continueWithError = false
    /**
     * If true, migration locking will be retried, default to true
     */
    lockingRetryEnabled = true
    /**
     * Intervals between retrials, in milliseconds
     */
    lockingRetryIntervalMillis = 5000  // 5s
    /**
     * Max number of migration locking retries, default to 120.
     * eg. with 5s retry interval, the retry window is 10min
     */
    lockingRetryMax = 120
}

