# Reactive Example

## Best Practices and Observations
* current bundled version of `r2dbc-postgres`, `0.8.6.RELEASE` does not support relative or classpath values for `sslRootCert`.  This value must be an absolute path :(.  `sslRootCert=classpath:ca.crt` or `sslRootCert=ca.crt` will not work.  It appears this has been fixed in the `0.8.7.RELEASE`.  Update: in `0.8.7.RELEASE` you can use `sslRootCert=ca.crt` which will look in the root of the classpath.  In Spring this means the root of the `resources` directory.

* specifying the database/cluster name as part of `options=--cluster=${cluster_name}` does not work with CC free tier.  Must specify the database name prepended to `defaultdb` instead.